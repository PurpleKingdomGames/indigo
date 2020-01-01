package indigo.gameengine

import indigo.shared.events._
import indigo.shared.scenegraph.{SceneAudio, SceneUpdateFragment}
import indigo.shared.GameContext
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
import indigo.shared.platform.GlobalSignals

import indigo.shared.scenegraph.SceneGraphViewEvents

class GameLoop[GameModel, ViewModel](
    gameConfig: GameConfig,
    assetMapping: AssetMapping,
    renderer: Renderer,
    audioPlayer: AudioPlayer,
    initialModel: GameModel,
    initialViewModel: ViewModel,
    frameProcessor: FrameProcessor[GameModel, ViewModel],
    metrics: Metrics,
    globalEventStream: GlobalEventStream,
    globalSignals: GlobalSignals,
    callTick: (Long => Unit) => Unit
) {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameModelState: GameModel = initialModel

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var viewModelState: ViewModel = initialViewModel

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var signalsState: Signals = Signals.default

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

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        metrics.record(UpdateStartMetric)

        val frameSideEffects = for {
          gameTime        <- GameContext { new GameTime(Millis(time), Millis(timeDelta), GameTime.FPS(gameConfig.frameRate)) }
          collectedEvents <- GameContext { globalEventStream.collect :+ FrameTick }
          _               <- persistSignalsState(collectedEvents)
          renderTheView   <- GameContext { gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt }

          processedFrame <- GameLoop.runFrameProcessor(
            renderTheView,
            frameProcessor,
            gameModelState,
            viewModelState,
            gameTime,
            collectedEvents,
            signalsState,
            Dice.default(gameTime.running.value),
            metrics
          )

          state <- GameContext { processedFrame.mapState(p => (p._1, p._2)) }
          scene <- GameContext { processedFrame.state._3 }
          _     <- persistFrameState(state)
          _     <- GameLoop.processUpdatedView(scene, collectedEvents, metrics, globalEventStream)
          _     <- GameLoop.drawScene(renderer, gameTime, scene, assetMapping, metrics)
          _     <- GameLoop.playAudio(audioPlayer, scene.map(_.audio), metrics)
        } yield ()

        frameSideEffects.unsafeRun()

        metrics.record(UpdateEndMetric)
      } else {
        metrics.record(SkippedModelUpdateMetric)
      }

      metrics.record(FrameEndMetric)

      callTick(loop(time))
    } else {
      callTick(loop(lastUpdateTime))
    }
  }

  private def persistFrameState(next: Outcome[(GameModel, ViewModel)]): GameContext[Unit] =
    GameContext {
      gameModelState = next.state._1
      viewModelState = next.state._2
      next.globalEvents.foreach(e => globalEventStream.pushGlobalEvent(e))
    }

  private def persistSignalsState(collectedEvents: List[GlobalEvent]): GameContext[Unit] =
    GameContext {
      signalsState = globalSignals.calculate(signalsState, collectedEvents)
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
      signalsState: Signals,
      dice: Dice,
      metrics: Metrics
  ): GameContext[Outcome[(GameModel, ViewModel, Option[SceneUpdateFragment])]] =
    GameContext {
      metrics.record(CallFrameProcessorStartMetric)

      val res: Outcome[(GameModel, ViewModel, Option[SceneUpdateFragment])] =
        if (renderView) {
          frameProcessor.run(gameModelState, viewModelState, gameTime, collectedEvents, signalsState, dice)
        } else {
          metrics.record(UpdateEndMetric)
          frameProcessor.runSkipView(gameModelState, viewModelState, gameTime, collectedEvents, signalsState, dice)
        }

      metrics.record(CallFrameProcessorEndMetric)

      res
    }

  def processUpdatedView(scene: Option[SceneUpdateFragment], collectedEvents: List[GlobalEvent], metrics: Metrics, globalEventStream: GlobalEventStream): GameContext[Unit] =
    GameContext {
      scene match {
        case None =>
          ()

        case Some(s) =>
          metrics.record(ProcessViewStartMetric)
          GameLoop.persistGlobalViewEvents(metrics, globalEventStream, s)
          GameLoop.persistNodeViewEvents(collectedEvents, metrics, globalEventStream, s)
          metrics.record(ProcessViewEndMetric)
      }
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

  def drawScene(renderer: Renderer, gameTime: GameTime, scene: Option[SceneUpdateFragment], assetMapping: AssetMapping, metrics: Metrics): GameContext[Unit] =
    GameContext {
      scene match {
        case None =>
          ()

        case Some(s) =>
          metrics.record(RenderStartMetric)
          renderer.drawScene(gameTime, s, assetMapping, metrics)
          metrics.record(RenderEndMetric)
      }
    }

  def playAudio(audioPlayer: AudioPlayer, sceneAudio: Option[SceneAudio], metrics: Metrics): GameContext[Unit] =
    GameContext {
      sceneAudio match {
        case None =>
          ()

        case Some(s) =>
          metrics.record(AudioStartMetric)
          audioPlayer.playAudio(s)
          metrics.record(AudioEndMetric)
      }
    }

}
