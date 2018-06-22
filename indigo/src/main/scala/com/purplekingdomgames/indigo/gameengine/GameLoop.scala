package com.purplekingdomgames.indigo.gameengine

import com.purplekingdomgames.indigo.gameengine.assets.AnimationsRegister
import com.purplekingdomgames.indigo.gameengine.audio.IAudioPlayer
import com.purplekingdomgames.indigo.gameengine.events._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.AmbientLight
import com.purplekingdomgames.indigo.gameengine.scenegraph.{
  SceneAudio,
  SceneGraphRootNode,
  SceneGraphRootNodeFlat,
  SceneUpdateFragment
}
import com.purplekingdomgames.indigo.renderer.{AssetMapping, DisplayLayer, Displayable, IRenderer}
import com.purplekingdomgames.indigo.runtime.IIO
import com.purplekingdomgames.indigo.runtime.metrics._
import com.purplekingdomgames.shared.GameConfig
import org.scalajs.dom

class GameLoop[StartupData, GameModel](
    gameConfig: GameConfig,
    startupData: StartupData,
    assetMapping: AssetMapping,
    renderer: IRenderer,
    audioPlayer: IAudioPlayer,
    initialModel: StartupData => GameModel,
    updateModel: (GameTime, GameModel) => GameEvent => GameModel,
    updateView: (GameTime, GameModel, FrameInputEvents) => SceneUpdateFragment
)(implicit metrics: IMetrics) {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var state: Option[GameModel] = None

  def loop(lastUpdateTime: Double): Double => Int = { time =>
    val timeDelta = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if (timeDelta > gameConfig.frameRateDeltaMillis) {

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        metrics.record(UpdateStartMetric)

        val gameTime: GameTime = GameTime(time, timeDelta, gameConfig.frameRateDeltaMillis.toDouble)

        val collectedEvents: List[GameEvent] = GlobalEventStream.collect

        GlobalSignalsManager.update(collectedEvents)

        metrics.record(CallUpdateGameModelStartMetric)

        val model: GameModel = state match {
          case None =>
            initialModel(startupData)

          case Some(previousModel) =>
            GameLoop.processModelUpdateEvents(gameTime, previousModel, collectedEvents, updateModel)
        }

        state = Some(model)

        metrics.record(CallUpdateGameModelEndMetric)
        metrics.record(UpdateEndMetric)

        // View updates cut off
        if (gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt) {

          for {
            view          <- GameLoop.updateGameView(updateView, gameTime, model, collectedEvents)
            processedView <- GameLoop.processUpdatedView(view, audioPlayer, collectedEvents)
            displayable   <- GameLoop.viewToDisplayable(gameTime, processedView, assetMapping, view.ambientLight)
            _             <- GameLoop.persistAnimationStates()
            _             <- GameLoop.drawScene(renderer, displayable)
            _             <- GameLoop.playAudio(audioPlayer, view.audio)
          } yield ()

        } else
          metrics.record(SkippedViewUpdateMetric)

      } else
        metrics.record(SkippedModelUpdateMetric)

      metrics.record(FrameEndMetric)

      dom.window.requestAnimationFrame(loop(time))
    } else
      dom.window.requestAnimationFrame(loop(lastUpdateTime))
  }

}

object GameLoop {

  def updateGameView[GameModel](updateView: (GameTime, GameModel, FrameInputEvents) => SceneUpdateFragment,
                                gameTime: GameTime,
                                model: GameModel,
                                collectedEvents: List[GameEvent])(implicit metrics: IMetrics): IIO[SceneUpdateFragment] =
    IIO.delay {
      metrics.record(CallUpdateViewStartMetric)

      val view: SceneUpdateFragment = updateView(
        gameTime,
        model,
        events.FrameInputEvents(collectedEvents.filterNot(_.isInstanceOf[ViewEvent]))
      )

      metrics.record(CallUpdateViewEndMetric)

      view
    }

  def processUpdatedView(view: SceneUpdateFragment, audioPlayer: IAudioPlayer, collectedEvents: List[GameEvent])(
      implicit metrics: IMetrics
  ): IIO[SceneGraphRootNodeFlat] =
    IIO.delay {
      metrics.record(ProcessViewStartMetric)

      val processUpdatedView: SceneUpdateFragment => SceneGraphRootNodeFlat =
        GameLoop.persistGlobalViewEvents(audioPlayer)(metrics) andThen
          GameLoop.flattenNodes andThen
          GameLoop.persistNodeViewEvents(metrics)(collectedEvents)

      val processedView: SceneGraphRootNodeFlat = processUpdatedView(view)

      metrics.record(ProcessViewEndMetric)

      processedView
    }

  def viewToDisplayable(gameTime: GameTime,
                        processedView: SceneGraphRootNodeFlat,
                        assetMapping: AssetMapping,
                        ambientLight: AmbientLight)(implicit metrics: IMetrics): IIO[Displayable] =
    IIO.delay {
      metrics.record(ToDisplayableStartMetric)

      val displayable: Displayable =
        GameLoop.convertSceneGraphToDisplayable(gameTime, processedView, assetMapping, ambientLight)

      metrics.record(ToDisplayableEndMetric)

      displayable
    }

  def persistAnimationStates()(implicit metrics: IMetrics): IIO[Unit] =
    IIO.delay {
      metrics.record(PersistAnimationStatesStartMetric)

      AnimationsRegister.persistAnimationStates()

      metrics.record(PersistAnimationStatesEndMetric)
    }

  def processModelUpdateEvents[GameModel](gameTime: GameTime,
                                          previousModel: GameModel,
                                          remaining: List[GameEvent],
                                          updateModel: (GameTime, GameModel) => GameEvent => GameModel): GameModel =
    remaining match {
      case Nil =>
        updateModel(gameTime, previousModel)(FrameTick)

      case x :: xs =>
        processModelUpdateEvents(gameTime, updateModel(gameTime, previousModel)(x), xs, updateModel)
    }

  val persistGlobalViewEvents: IAudioPlayer => IMetrics => SceneUpdateFragment => SceneGraphRootNode = audioPlayer =>
    metrics =>
      update => {
        metrics.record(PersistGlobalViewEventsStartMetric)
        update.viewEvents.foreach(e => GlobalEventStream.pushViewEvent(audioPlayer, e))
        metrics.record(PersistGlobalViewEventsEndMetric)
        SceneGraphRootNode.fromFragment(update)
  }

  val flattenNodes: SceneGraphRootNode => SceneGraphRootNodeFlat = root => root.flatten

  val persistNodeViewEvents: IMetrics => List[GameEvent] => SceneGraphRootNodeFlat => SceneGraphRootNodeFlat = metrics =>
    gameEvents =>
      rootNode => {
        metrics.record(PersistNodeViewEventsStartMetric)
        rootNode.collectViewEvents(gameEvents).foreach(GlobalEventStream.pushGameEvent)
        metrics.record(PersistNodeViewEventsEndMetric)
        rootNode
  }

  def convertSceneGraphToDisplayable(gameTime: GameTime,
                                     rootNode: SceneGraphRootNodeFlat,
                                     assetMapping: AssetMapping,
                                     ambientLight: AmbientLight)(implicit metrics: IMetrics): Displayable =
    Displayable(
      DisplayLayer(
        rootNode.game.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping))
      ),
      DisplayLayer(
        rootNode.lighting.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping))
      ),
      DisplayLayer(
        rootNode.ui.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping))
      ),
      ambientLight
    )

  def drawScene(renderer: IRenderer, displayable: Displayable)(implicit metrics: IMetrics): IIO[Unit] =
    IIO.delay {
      metrics.record(RenderStartMetric)

      renderer.drawScene(displayable)

      metrics.record(RenderEndMetric)
    }

  def playAudio(audioPlayer: IAudioPlayer, sceneAudio: SceneAudio)(implicit metrics: IMetrics): IIO[Unit] =
    IIO.delay {
      metrics.record(AudioStartMetric)

      audioPlayer.playAudio(sceneAudio)

      metrics.record(AudioEndMetric)
    }

}
