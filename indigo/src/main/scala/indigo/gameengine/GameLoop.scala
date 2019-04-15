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
import indigo.dice.Dice
import org.scalajs.dom
import indigo.time.GameTime
import indigo.time.Millis

import scala.annotation.tailrec

class GameLoop[GameModel, ViewModel](
    gameConfig: GameConfig,
    assetMapping: AssetMapping,
    renderer: IRenderer,
    audioPlayer: AudioPlayer,
    subSystemsRegister: SubSystemsRegister,
    initialModel: GameModel,
    // updateModel: (GameTime, GameModel) => GlobalEvent => Outcome[GameModel],
    initialViewModel: ViewModel,
    // updateViewModel: (GameTime, GameModel, ViewModel, FrameInputEvents) => Outcome[ViewModel],
    // updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment,
    frameProcessor: FrameProcessor[GameModel, ViewModel],
    metrics: Metrics,
    globalEventStream: GlobalEventStream,
    globalSignals: GlobalSignals
) {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameModelState: GameModel = initialModel

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var viewModelState: ViewModel = initialViewModel

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var signalsState: Signals = Signals.default

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var subSystemsState: SubSystemsRegister = subSystemsRegister

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def loop(lastUpdateTime: Long): Long => Int = { time =>
    val timeDelta: Long = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if (timeDelta > gameConfig.frameRateDeltaMillis) {

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        metrics.record(UpdateStartMetric)

        val gameTime: GameTime =
          new GameTime(Millis(time), Millis(timeDelta), GameTime.FPS(gameConfig.frameRate))

        val dice: Dice =
          Dice.default(gameTime.running.value)

        val collectedEvents: List[GlobalEvent] =
          globalEventStream.collect :+ FrameTick

        signalsState = globalSignals.calculate(signalsState, collectedEvents)

        //
        metrics.record(CallUpdateSubSystemsStartMetric)

        val subSystems =
          GameLoop.processSubSystemUpdates(gameTime, dice, subSystemsState, collectedEvents, globalEventStream)

        subSystemsState = subSystems

        metrics.record(CallUpdateSubSystemsEndMetric)
        //

        // View updates cut off
        if (gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt) {

          metrics.record(CallFrameProcessorStartMetric)

          val (next, view): (Outcome[(GameModel, ViewModel)], SceneUpdateFragment) =
            frameProcessor.run(gameModelState, viewModelState)(gameTime, collectedEvents, signalsState, dice)

          metrics.record(CallFrameProcessorEndMetric)

          // Persist everything!
          gameModelState = next.state._1
          viewModelState = next.state._2
          next.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))

          metrics.record(UpdateEndMetric)

          val frameSideEffects = for {
            processedView <- GameLoop.processUpdatedView(view, collectedEvents, metrics, globalEventStream)
            displayable   <- GameLoop.viewToDisplayable(gameTime, processedView, assetMapping, view.ambientLight, metrics)
            _             <- GameLoop.persistAnimationStates(metrics)
            _             <- GameLoop.drawScene(renderer, displayable, metrics)
            _             <- GameLoop.playAudio(audioPlayer, view.audio, metrics)
          } yield ()

          frameSideEffects.unsafeRun()

        } else {
          metrics.record(CallFrameProcessorStartMetric)

          val next: Outcome[(GameModel, ViewModel)] =
            frameProcessor.runSkipView(gameModelState, viewModelState)(gameTime, collectedEvents, signalsState, dice)

          metrics.record(CallFrameProcessorEndMetric)

          // Persist everything!
          gameModelState = next.state._1
          viewModelState = next.state._2
          next.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))

          metrics.record(UpdateEndMetric)
          metrics.record(SkippedViewUpdateMetric)
        }

      } else {
        metrics.record(SkippedModelUpdateMetric)
      }

      metrics.record(FrameEndMetric)

      dom.window.requestAnimationFrame(t => loop(time)(t.toLong))
    } else {
      dom.window.requestAnimationFrame(t => loop(lastUpdateTime)(t.toLong))
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
      subSystemsRegister: SubSystemsRegister,
      metrics: Metrics
  ): SceneUpdateFragment = {
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

  def processUpdatedView(view: SceneUpdateFragment, collectedEvents: List[GlobalEvent], metrics: Metrics, globalEventStream: GlobalEventStream): GameContext[SceneGraphRootNodeFlat] =
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

  def viewToDisplayable(gameTime: GameTime, processedView: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight, metrics: Metrics): GameContext[Displayable] =
    GameContext.delay {
      metrics.record(ToDisplayableStartMetric)

      val displayable: Displayable =
        GameLoop.convertSceneGraphToDisplayable(gameTime, processedView, assetMapping, ambientLight, metrics)

      metrics.record(ToDisplayableEndMetric)

      displayable
    }

  def persistAnimationStates(metrics: Metrics): GameContext[Unit] =
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
      updateModel: (GameTime, GameModel) => GlobalEvent => Outcome[GameModel],
      globalEventStream: GlobalEventStream
  ): (GameModel, FrameInputEvents) = {
    val combine: (Outcome[GameModel], Outcome[GameModel]) => Outcome[GameModel] =
      (a, b) => new Outcome(b.state, a.globalEvents ++ b.globalEvents)

    @tailrec
    def rec(remaining: List[GlobalEvent], last: Outcome[GameModel]): Outcome[GameModel] =
      remaining match {
        case Nil =>
          last

        case x :: xs =>
          rec(xs, combine(last, updateModel(gameTime, last.state)(x)))
      }

    val res = rec(collectedEvents, Outcome(model))
    res.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))
    (res.state, FrameInputEvents(res.globalEvents, signals))
  }

  @tailrec
  def processSubSystemUpdates(gameTime: GameTime, dice: Dice, register: SubSystemsRegister, collectedEvents: List[GlobalEvent], globalEventStream: GlobalEventStream): SubSystemsRegister =
    collectedEvents match {
      case Nil =>
        register

      case e :: es =>
        val res = register.update(gameTime, dice)(e)

        res.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))

        processSubSystemUpdates(
          gameTime,
          dice,
          res.state,
          es,
          globalEventStream
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

  def convertSceneGraphToDisplayable(gameTime: GameTime, rootNode: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight, metrics: Metrics): Displayable =
    Displayable(
      DisplayLayer(
        rootNode.game.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping, metrics))
      ),
      DisplayLayer(
        rootNode.lighting.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping, metrics))
      ),
      DisplayLayer(
        rootNode.ui.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping, metrics))
      ),
      ambientLight
    )

  def drawScene(renderer: IRenderer, displayable: Displayable, metrics: Metrics): GameContext[Unit] =
    GameContext.delay {
      metrics.record(RenderStartMetric)

      renderer.drawScene(displayable, metrics)

      metrics.record(RenderEndMetric)
    }

  def playAudio(audioPlayer: AudioPlayer, sceneAudio: SceneAudio, metrics: Metrics): GameContext[Unit] =
    GameContext.delay {
      metrics.record(AudioStartMetric)

      audioPlayer.playAudio(sceneAudio)

      metrics.record(AudioEndMetric)
    }

}
