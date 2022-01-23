package indigo.gameengine

import indigo.shared.BoundaryLocator
import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.config.GameConfig
import indigo.shared.dice.Dice
import indigo.shared.events._
import indigo.shared.platform.SceneProcessor
import indigo.shared.scenegraph.SceneGraphViewEvents
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.FPS
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

class GameLoop[StartUpData, GameModel, ViewModel](
    boundaryLocator: BoundaryLocator,
    sceneProcessor: SceneProcessor,
    gameEngine: GameEngine[StartUpData, GameModel, ViewModel],
    gameConfig: GameConfig,
    initialModel: GameModel,
    initialViewModel: ViewModel,
    frameProcessor: FrameProcessor[StartUpData, GameModel, ViewModel]
) {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var gameModelState: GameModel = initialModel

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var viewModelState: ViewModel = initialViewModel

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  var runningTimeReference: Long = 0

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var inputState: InputState = InputState.default

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def loop(lastUpdateTime: Long): Long => Unit = { time =>
    runningTimeReference = time
    val timeDelta: Long = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if (timeDelta >= gameConfig.frameRateDeltaMillis.toLong - 1) {

      val startUpData: StartUpData =
        gameEngine.startUpData

      val gameTime =
        new GameTime(Millis(time).toSeconds, Seconds(timeDelta.toDouble / 1000d), gameConfig.frameRate)
      val collectedEvents = gameEngine.globalEventStream.collect ++ List(FrameTick)
      val dice            = Dice.fromSeconds(gameTime.running)

      // Persist input state
      inputState = InputState.calculateNext(
        inputState,
        collectedEvents.collect { case e: InputEvent => e },
        gameEngine.gamepadInputCapture.giveGamepadState
      )

      val processedFrame: Outcome[(GameModel, ViewModel, SceneUpdateFragment)] =
        frameProcessor.run(
          startUpData,
          gameModelState,
          viewModelState,
          gameTime,
          collectedEvents,
          inputState,
          dice,
          boundaryLocator
        )

      // Persist frame state
      val scene =
        processedFrame match {
          case oe @ Outcome.Error(e, _) =>
            IndigoLogger.error("The game has crashed...")
            IndigoLogger.error(oe.reportCrash)
            throw e

          case Outcome.Result(state, globalEvents) =>
            gameModelState = state._1
            viewModelState = state._2
            globalEvents.foreach(e => gameEngine.globalEventStream.pushGlobalEvent(e))
            state._3
        }

      // Process events
      scene.layers.foreach { layer =>
        SceneGraphViewEvents.collectViewEvents(
          boundaryLocator,
          layer.nodes,
          collectedEvents,
          gameEngine.globalEventStream.pushGlobalEvent
        )
      }

      // Play audio
      gameEngine.audioPlayer.playAudio(scene.audio)

      // Prepare scene
      val sceneData = sceneProcessor.processScene(
        gameTime,
        scene,
        gameEngine.assetMapping,
        gameEngine.renderer.renderingTechnology,
        gameConfig.advanced.batchSize
      )

      // Render scene
      gameEngine.renderer.drawScene(sceneData, gameTime.running)

      // Tick
      gameEngine.platform.tick(gameEngine.gameLoop(time))
    } else gameEngine.platform.tick(loop(lastUpdateTime))
  }

}
