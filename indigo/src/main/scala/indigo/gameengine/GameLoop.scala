package indigo.gameengine

import indigo.gameengine.assets.AnimationsRegister
import indigo.gameengine.audio.IAudioPlayer
import indigo.gameengine.events._
import indigo.gameengine.scenegraph.datatypes.AmbientLight
import indigo.gameengine.scenegraph.{SceneAudio, SceneGraphRootNode, SceneGraphRootNodeFlat, SceneUpdateFragment}
import indigo.renderer.{AssetMapping, DisplayLayer, Displayable, IRenderer}
import indigo.runtime.IIO
import indigo.runtime.metrics._
import indigo.shared.GameConfig
import org.scalajs.dom

class GameLoop[GameModel, ViewModel](
    gameConfig: GameConfig,
    assetMapping: AssetMapping,
    renderer: IRenderer,
    audioPlayer: IAudioPlayer,
    initialModel: GameModel,
    updateModel: (GameTime, GameModel) => GameEvent => UpdatedModel[GameModel],
    initialViewModel: ViewModel,
    updateViewModel: (GameTime, GameModel, ViewModel, FrameInputEvents) => UpdatedViewModel[ViewModel],
    updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment
)(implicit metrics: IMetrics, globalEventStream: GlobalEventStream) {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameModelState: Option[GameModel] = None
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var viewModelState: Option[ViewModel] = None

  def loop(lastUpdateTime: Double): Double => Int = { time =>
    val timeDelta = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if (timeDelta > gameConfig.frameRateDeltaMillis) {

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        metrics.record(UpdateStartMetric)

        val gameTime: GameTime = GameTime(time, timeDelta, gameConfig.frameRateDeltaMillis.toDouble)

        val collectedEvents: List[GameEvent]   = globalEventStream.collect
        val frameInputEvents: FrameInputEvents = events.FrameInputEvents(collectedEvents.filter(_.isGameEvent))

        GlobalSignalsManager.update(collectedEvents)

        metrics.record(CallUpdateGameModelStartMetric)

        val model: GameModel = gameModelState match {
          case None =>
            initialModel

          case Some(previousModel) =>
            GameLoop.processModelUpdateEvents(gameTime, previousModel, collectedEvents, updateModel)
        }

        gameModelState = Some(model)

        metrics.record(CallUpdateGameModelEndMetric)
        metrics.record(CallUpdateViewModelStartMetric)

        val viewModel: ViewModel = viewModelState match {
          case None =>
            initialViewModel

          case Some(previousModel) =>
            val next = updateViewModel(gameTime, model, previousModel, frameInputEvents)
            next.events.foreach(e => globalEventStream.pushViewEvent(e))
            next.model
        }

        viewModelState = Some(viewModel)

        metrics.record(CallUpdateViewModelEndMetric)
        metrics.record(UpdateEndMetric)

        // View updates cut off
        if (gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt) {

          val x = for {
            view          <- GameLoop.updateGameView(updateView, gameTime, model, viewModel, frameInputEvents)
            processedView <- GameLoop.processUpdatedView(view, collectedEvents)
            displayable   <- GameLoop.viewToDisplayable(gameTime, processedView, assetMapping, view.ambientLight)
            _             <- GameLoop.persistAnimationStates()
            _             <- GameLoop.drawScene(renderer, displayable)
            _             <- GameLoop.playAudio(audioPlayer, view.audio)
          } yield ()

          x.unsafeRun()

        } else {
          metrics.record(SkippedViewUpdateMetric)
        }

      } else {
        metrics.record(SkippedModelUpdateMetric)
      }

      metrics.record(FrameEndMetric)

      dom.window.requestAnimationFrame(loop(time))
    } else {
      dom.window.requestAnimationFrame(loop(lastUpdateTime))
    }
  }

}

object GameLoop {

  def updateGameView[GameModel, ViewModel](
      updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment,
      gameTime: GameTime,
      model: GameModel,
      viewModel: ViewModel,
      frameInputEvents: FrameInputEvents
  )(implicit metrics: IMetrics): IIO[SceneUpdateFragment] =
    IIO.delay {
      metrics.record(CallUpdateViewStartMetric)

      val view: SceneUpdateFragment = updateView(
        gameTime,
        model,
        viewModel,
        frameInputEvents
      )

      metrics.record(CallUpdateViewEndMetric)

      view
    }

  def processUpdatedView(view: SceneUpdateFragment, collectedEvents: List[GameEvent])(
      implicit metrics: IMetrics,
      globalEventStream: GlobalEventStream
  ): IIO[SceneGraphRootNodeFlat] =
    IIO.delay {
      metrics.record(ProcessViewStartMetric)

      val processUpdatedView: SceneUpdateFragment => SceneGraphRootNodeFlat =
        GameLoop.persistGlobalViewEvents(metrics, globalEventStream) andThen
          GameLoop.flattenNodes andThen
          GameLoop.persistNodeViewEvents(collectedEvents, metrics, globalEventStream)

      val processedView: SceneGraphRootNodeFlat = processUpdatedView(view)

      metrics.record(ProcessViewEndMetric)

      processedView
    }

  def viewToDisplayable(gameTime: GameTime, processedView: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight)(implicit metrics: IMetrics): IIO[Displayable] =
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

  def processModelUpdateEvents[GameModel](gameTime: GameTime, previousModel: GameModel, remaining: List[GameEvent], updateModel: (GameTime, GameModel) => GameEvent => UpdatedModel[GameModel])(
      implicit globalEventStream: GlobalEventStream
  ): GameModel =
    remaining match {
      case Nil =>
        val next = updateModel(gameTime, previousModel)(FrameTick)
        next.events.foreach(e => globalEventStream.pushViewEvent(e))
        next.model

      case x :: xs =>
        val next = updateModel(gameTime, previousModel)(x)
        next.events.foreach(e => globalEventStream.pushViewEvent(e))
        processModelUpdateEvents(gameTime, next.model, xs, updateModel)
    }

  def persistGlobalViewEvents(metrics: IMetrics, globalEventStream: GlobalEventStream): SceneUpdateFragment => SceneGraphRootNode = update => {
    metrics.record(PersistGlobalViewEventsStartMetric)
    update.viewEvents.foreach(e => globalEventStream.pushViewEvent(e))
    metrics.record(PersistGlobalViewEventsEndMetric)
    SceneGraphRootNode.fromFragment(update)
  }

  val flattenNodes: SceneGraphRootNode => SceneGraphRootNodeFlat = root => root.flatten

  def persistNodeViewEvents(gameEvents: List[GameEvent], metrics: IMetrics, globalEventStream: GlobalEventStream): SceneGraphRootNodeFlat => SceneGraphRootNodeFlat = rootNode => {
    metrics.record(PersistNodeViewEventsStartMetric)
    rootNode.collectViewEvents(gameEvents).foreach(globalEventStream.pushGameEvent)
    metrics.record(PersistNodeViewEventsEndMetric)
    rootNode
  }

  def convertSceneGraphToDisplayable(gameTime: GameTime, rootNode: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight)(implicit metrics: IMetrics): Displayable =
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
