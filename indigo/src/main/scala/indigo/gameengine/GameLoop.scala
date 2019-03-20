package indigo.gameengine

import indigo.gameengine.assets.AnimationsRegister
import indigo.gameengine.audio.AudioPlayer
import indigo.gameengine.events._
import indigo.gameengine.scenegraph.datatypes.AmbientLight
import indigo.gameengine.scenegraph.{SceneAudio, SceneGraphRootNode, SceneGraphRootNodeFlat, SceneUpdateFragment}
import indigo.gameengine.subsystems.SubSystemsRegister
import indigo.renderer.{AssetMapping, DisplayLayer, Displayable, IRenderer}
import indigo.runtime.GameContext
import indigo.runtime.metrics._
import indigo.shared.GameConfig
import org.scalajs.dom

import scala.annotation.tailrec

class GameLoop[GameModel, ViewModel](
    gameConfig: GameConfig,
    assetMapping: AssetMapping,
    renderer: IRenderer,
    audioPlayer: AudioPlayer,
    subSystemsRegister: SubSystemsRegister,
    initialModel: GameModel,
    updateModel: (GameTime, GameModel) => GlobalEvent => UpdatedModel[GameModel],
    initialViewModel: ViewModel,
    updateViewModel: (GameTime, GameModel, ViewModel, FrameInputEvents) => UpdatedViewModel[ViewModel],
    updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment
)(implicit metrics: Metrics, globalEventStream: GlobalEventStream, globalSignals: GlobalSignals) {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameModelState: Option[GameModel] = None
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var viewModelState: Option[ViewModel] = None
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var signalsState: Signals = Signals.default
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var subSystemsState: SubSystemsRegister = subSystemsRegister

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def loop(lastUpdateTime: Double): Double => Int = { time =>
    val timeDelta = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if (timeDelta > gameConfig.frameRateDeltaMillis) {

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        metrics.record(UpdateStartMetric)

        val gameTime: GameTime = new GameTime(time, timeDelta, gameConfig.frameRate)

        val collectedEvents: List[GlobalEvent] = globalEventStream.collect :+ FrameTick

        val signals = globalSignals.calculate(signalsState, collectedEvents)
        signalsState = signals

        metrics.record(CallUpdateGameModelStartMetric)

        val model: (GameModel, FrameInputEvents) = gameModelState match {
          case None =>
            (initialModel, FrameInputEvents.empty)

          case Some(previousModel) =>
            GameLoop.processModelUpdateEvents(gameTime, previousModel, collectedEvents, signals, updateModel)
        }

        gameModelState = Some(model._1)

        metrics.record(CallUpdateGameModelEndMetric)

        //
        metrics.record(CallUpdateSubSystemsStartMetric)

        val subSystems = GameLoop.processSubSystemUpdates(gameTime, subSystemsState, collectedEvents)

        subSystemsState = subSystems

        metrics.record(CallUpdateSubSystemsEndMetric)
        //

        metrics.record(CallUpdateViewModelStartMetric)

        val viewModel: (ViewModel, FrameInputEvents) = viewModelState match {
          case None =>
            (initialViewModel, FrameInputEvents.empty)

          case Some(previousModel) =>
            val next = updateViewModel(gameTime, model._1, previousModel, model._2)
            next.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))
            (next.model, FrameInputEvents(collectedEvents, next.inFrameEvents, signals))
        }

        viewModelState = Some(viewModel._1)

        metrics.record(CallUpdateViewModelEndMetric)
        metrics.record(UpdateEndMetric)

        // View updates cut off
        if (gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt) {

          val x = for {
            view          <- GameLoop.updateGameView(updateView, gameTime, model._1, viewModel._1, viewModel._2, subSystemsState)
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
      frameInputEvents: FrameInputEvents,
      subSystemsRegister: SubSystemsRegister
  )(implicit metrics: Metrics): GameContext[SceneUpdateFragment] =
    GameContext.delay {
      metrics.record(CallUpdateViewStartMetric)

      val view: SceneUpdateFragment = updateView(
        gameTime,
        model,
        viewModel,
        frameInputEvents
      ) |+| subSystemsRegister.render(gameTime)

      metrics.record(CallUpdateViewEndMetric)

      view
    }

  def processUpdatedView(view: SceneUpdateFragment, collectedEvents: List[GlobalEvent])(
      implicit metrics: Metrics,
      globalEventStream: GlobalEventStream
  ): GameContext[SceneGraphRootNodeFlat] =
    GameContext.delay {
      metrics.record(ProcessViewStartMetric)

      val processUpdatedView: SceneUpdateFragment => SceneGraphRootNodeFlat =
        GameLoop.persistGlobalViewEvents(metrics, globalEventStream) andThen
          GameLoop.flattenNodes andThen
          GameLoop.persistNodeViewEvents(collectedEvents, metrics, globalEventStream)

      val processedView: SceneGraphRootNodeFlat = processUpdatedView(view)

      metrics.record(ProcessViewEndMetric)

      processedView
    }

  def viewToDisplayable(gameTime: GameTime, processedView: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight)(implicit metrics: Metrics): GameContext[Displayable] =
    GameContext.delay {
      metrics.record(ToDisplayableStartMetric)

      val displayable: Displayable =
        GameLoop.convertSceneGraphToDisplayable(gameTime, processedView, assetMapping, ambientLight)

      metrics.record(ToDisplayableEndMetric)

      displayable
    }

  def persistAnimationStates()(implicit metrics: Metrics): GameContext[Unit] =
    GameContext.delay {
      metrics.record(PersistAnimationStatesStartMetric)

      AnimationsRegister.persistAnimationStates()

      metrics.record(PersistAnimationStatesEndMetric)
    }

  def processModelUpdateEvents[GameModel](
      gameTime: GameTime,
      model: GameModel,
      collectedEvents: List[GlobalEvent],
      signals: Signals,
      updateModel: (GameTime, GameModel) => GlobalEvent => UpdatedModel[GameModel]
  )(
      implicit globalEventStream: GlobalEventStream
  ): (GameModel, FrameInputEvents) = {
    val combine: (UpdatedModel[GameModel], UpdatedModel[GameModel]) => UpdatedModel[GameModel] =
      (a, b) => UpdatedModel(b.model, a.globalEvents ++ b.globalEvents, a.inFrameEvents ++ b.inFrameEvents)

    @tailrec
    def rec(remaining: List[GlobalEvent], last: UpdatedModel[GameModel]): UpdatedModel[GameModel] =
      remaining match {
        case Nil =>
          last

        case x :: xs =>
          rec(xs, combine(last, updateModel(gameTime, last.model)(x)))
      }

    val res = rec(collectedEvents, UpdatedModel(model))
    res.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))
    (res.model, FrameInputEvents(res.globalEvents, res.inFrameEvents, signals))
  }

  @tailrec
  def processSubSystemUpdates(gameTime: GameTime, register: SubSystemsRegister, collectedEvents: List[GlobalEvent])(implicit globalEventStream: GlobalEventStream): SubSystemsRegister =
    collectedEvents match {
      case Nil =>
        register

      case e :: es =>
        val res = register.update(gameTime)(e)
        res.events.foreach(e => globalEventStream.pushGlobalEvent(e))
        processSubSystemUpdates(
          gameTime,
          res.register,
          es
        )
    }

  def persistGlobalViewEvents(metrics: Metrics, globalEventStream: GlobalEventStream): SceneUpdateFragment => SceneGraphRootNode = update => {
    metrics.record(PersistGlobalViewEventsStartMetric)
    update.viewEvents.foreach(e => globalEventStream.pushGlobalEvent(e))
    metrics.record(PersistGlobalViewEventsEndMetric)
    SceneGraphRootNode.fromFragment(update)
  }

  val flattenNodes: SceneGraphRootNode => SceneGraphRootNodeFlat = root => root.flatten

  def persistNodeViewEvents(gameEvents: List[GlobalEvent], metrics: Metrics, globalEventStream: GlobalEventStream): SceneGraphRootNodeFlat => SceneGraphRootNodeFlat = rootNode => {
    metrics.record(PersistNodeViewEventsStartMetric)
    rootNode.collectViewEvents(gameEvents).foreach(globalEventStream.pushGlobalEvent)
    metrics.record(PersistNodeViewEventsEndMetric)
    rootNode
  }

  def convertSceneGraphToDisplayable(gameTime: GameTime, rootNode: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight)(implicit metrics: Metrics): Displayable =
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

  def drawScene(renderer: IRenderer, displayable: Displayable)(implicit metrics: Metrics): GameContext[Unit] =
    GameContext.delay {
      metrics.record(RenderStartMetric)

      renderer.drawScene(displayable)

      metrics.record(RenderEndMetric)
    }

  def playAudio(audioPlayer: AudioPlayer, sceneAudio: SceneAudio)(implicit metrics: Metrics): GameContext[Unit] =
    GameContext.delay {
      metrics.record(AudioStartMetric)

      audioPlayer.playAudio(sceneAudio)

      metrics.record(AudioEndMetric)
    }

}
