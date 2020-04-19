package indigo.gameengine

import indigo.shared.events._
import indigo.shared.scenegraph.{SceneAudio, SceneUpdateFragment}
import indigo.shared.metrics._
import indigo.shared.config.GameConfig
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.platform.AudioPlayer

import indigo.shared.platform.AssetMapping
import indigo.shared.platform.Renderer
import indigo.shared.platform.GlobalEventStream

import indigo.shared.scenegraph.SceneGraphViewEvents
import indigo.shared.time.Seconds

class GameLoop[GameModel, ViewModel](
    gameEngine: GameEngine[_, _, GameModel, ViewModel],
    gameConfig: GameConfig,
    initialModel: GameModel,
    initialViewModel: ViewModel,
    frameProcessor: FrameProcessor[GameModel, ViewModel],
    callTick: (Long => Unit) => Unit
) {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameModelState: GameModel = initialModel

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var viewModelState: ViewModel = initialViewModel

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var inputState: InputState = InputState.default

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def loop(lastUpdateTime: Long): Long => Unit = { time =>
    val timeDelta: Long = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    // This is... confusing... aiming for 30 FPS:
    // (timeDelta: Long) >= (frameRateDeltaMillis: Long) (which has been round from 33.333 to 33) - 1
    // This seems to give us a solid 30 to 31 frames per second.
    // Without the -1, we get 27 to 28
    // ...probably because the timeDelta is also rounded down from Double to Long
    // By insisting time be measured in sensible units, I've made a rod for my own back...
    if (timeDelta >= gameConfig.frameRateDeltaMillis.toLong - 1) {

      gameEngine.metrics.record(FrameStartMetric)

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        gameEngine.metrics.record(UpdateStartMetric)

        val gameTime        = new GameTime(Millis(time).toSeconds, Seconds(timeDelta.toDouble / 1000d), GameTime.FPS(gameConfig.frameRate))
        val collectedEvents = gameEngine.globalEventStream.collect :+ FrameTick

        // Persist input state
        inputState = InputState.calculateNext(
          inputState,
          collectedEvents.collect { case e: InputEvent => e },
          gameEngine.gamepadInputCapture.giveGamepadState
        )

        val processedFrame: Outcome[(GameModel, ViewModel, Option[SceneUpdateFragment])] =
          GameLoop.runFrameProcessor(
            renderView = gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt,
            frameProcessor,
            gameModelState,
            viewModelState,
            gameTime,
            collectedEvents,
            inputState,
            Dice.default(gameTime.running.toMillis.value),
            gameEngine.metrics
          )

        // Persist frame state
        gameModelState = processedFrame.state._1
        viewModelState = processedFrame.state._2
        processedFrame.globalEvents.foreach(e => gameEngine.globalEventStream.pushGlobalEvent(e))

        GameLoop.processUpdatedView(processedFrame.state._3, collectedEvents, gameEngine.metrics, gameEngine.globalEventStream)
        GameLoop.drawScene(gameEngine.renderer, gameTime, processedFrame.state._3, gameEngine.assetMapping, gameEngine.metrics)
        GameLoop.playAudio(gameEngine.audioPlayer, processedFrame.state._3.map(_.audio), gameEngine.metrics)

        gameEngine.metrics.record(UpdateEndMetric)
      } else {
        gameEngine.metrics.record(SkippedModelUpdateMetric)
      }

      gameEngine.metrics.record(FrameEndMetric)

      callTick(loop(time))
    } else {
      callTick(loop(lastUpdateTime))
    }
  }

}

object GameLoop {

  def runFrameProcessor[GameModel, ViewModel](
      renderView: Boolean,
      frameProcessor: FrameProcessor[GameModel, ViewModel],
      gameModelState: GameModel,
      viewModelState: ViewModel,
      gameTime: GameTime,
      collectedEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      metrics: Metrics
  ): Outcome[(GameModel, ViewModel, Option[SceneUpdateFragment])] = {
    metrics.record(CallFrameProcessorStartMetric)

    val res: Outcome[(GameModel, ViewModel, Option[SceneUpdateFragment])] =
      if (renderView) {
        frameProcessor.run(gameModelState, viewModelState, gameTime, collectedEvents, inputState, dice)
      } else {
        metrics.record(UpdateEndMetric)
        frameProcessor.runSkipView(gameModelState, viewModelState, gameTime, collectedEvents, inputState, dice)
      }

    metrics.record(CallFrameProcessorEndMetric)

    res
  }

  def processUpdatedView(scene: Option[SceneUpdateFragment], collectedEvents: List[GlobalEvent], metrics: Metrics, globalEventStream: GlobalEventStream): Unit =
    scene match {
      case None =>
        ()

      case Some(s) =>
        metrics.record(ProcessViewStartMetric)
        GameLoop.persistGlobalViewEvents(metrics, globalEventStream, s)
        GameLoop.persistNodeViewEvents(collectedEvents, metrics, globalEventStream, s)
        metrics.record(ProcessViewEndMetric)
    }

  def persistGlobalViewEvents(metrics: Metrics, globalEventStream: GlobalEventStream, scene: SceneUpdateFragment): Unit = {
    metrics.record(PersistGlobalViewEventsStartMetric)
    scene.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))
    metrics.record(PersistGlobalViewEventsEndMetric)
  }

  def persistNodeViewEvents(gameEvents: List[GlobalEvent], metrics: Metrics, globalEventStream: GlobalEventStream, scene: SceneUpdateFragment): Unit = {
    metrics.record(PersistNodeViewEventsStartMetric)
    SceneGraphViewEvents.collectViewEvents(scene.gameLayer.nodes, gameEvents, globalEventStream.pushGlobalEvent)
    SceneGraphViewEvents.collectViewEvents(scene.lightingLayer.nodes, gameEvents, globalEventStream.pushGlobalEvent)
    SceneGraphViewEvents.collectViewEvents(scene.uiLayer.nodes, gameEvents, globalEventStream.pushGlobalEvent)
    metrics.record(PersistNodeViewEventsEndMetric)
  }

  def drawScene(renderer: Renderer, gameTime: GameTime, scene: Option[SceneUpdateFragment], assetMapping: AssetMapping, metrics: Metrics): Unit =
    scene match {
      case None =>
        ()

      case Some(s) =>
        metrics.record(RenderStartMetric)
        renderer.drawScene(gameTime, s, assetMapping, metrics)
        metrics.record(RenderEndMetric)
    }

  def playAudio(audioPlayer: AudioPlayer, sceneAudio: Option[SceneAudio], metrics: Metrics): Unit =
    sceneAudio match {
      case None =>
        ()

      case Some(s) =>
        metrics.record(AudioStartMetric)
        audioPlayer.playAudio(s)
        metrics.record(AudioEndMetric)
    }

}
