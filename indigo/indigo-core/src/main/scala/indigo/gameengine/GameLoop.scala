package indigo.gameengine

import indigo.shared.events._
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.config.GameConfig
import indigo.shared.Outcome
import indigo.shared.dice.Dice
import indigo.shared.time.GameTime
import indigo.shared.time.Millis

import indigo.shared.scenegraph.SceneGraphViewEvents
import indigo.shared.time.Seconds
import indigo.shared.BoundaryLocator
import indigo.shared.platform.SceneProcessor

class GameLoop[StartUpData, GameModel, ViewModel](
    boundaryLocator: BoundaryLocator,
    sceneProcessor: SceneProcessor,
    gameEngine: GameEngine[StartUpData, GameModel, ViewModel],
    gameConfig: GameConfig,
    initialModel: GameModel,
    initialViewModel: ViewModel,
    frameProcessor: FrameProcessor[StartUpData, GameModel, ViewModel]
) {

  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var gameModelState: GameModel = initialModel

  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var viewModelState: ViewModel = initialViewModel

  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var runningTimeReference: Long = 0

  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var inputState: InputState = InputState.default

  // @SuppressWarnings(Array("org.wartremover.warts.Recursion", "org.wartremover.warts.OptionPartial"))
  def loop(lastUpdateTime: Long): Long => Unit = { time =>
    runningTimeReference = time
    val timeDelta: Long = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if (timeDelta >= gameConfig.frameRateDeltaMillis.toLong - 1) {

      val startUpData: StartUpData =
        gameEngine.startUpData

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        val gameTime        = new GameTime(Millis(time).toSeconds, Seconds(timeDelta.toDouble / 1000d), GameTime.FPS(gameConfig.frameRate))
        val collectedEvents = gameEngine.globalEventStream.collect ++ List(FrameTick)
        val dice            = Dice.fromSeconds(gameTime.running)

        // Persist input state
        inputState = InputState.calculateNext(
          inputState,
          collectedEvents.collect { case e: InputEvent => e },
          gameEngine.gamepadInputCapture.giveGamepadState
        )

        if (gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt) {
          val processedFrame: Outcome[(GameModel, ViewModel, SceneUpdateFragment)] =
            frameProcessor.run(startUpData, gameModelState, viewModelState, gameTime, collectedEvents, inputState, dice, boundaryLocator)

          // Persist frame state
          gameModelState = processedFrame.state._1
          viewModelState = processedFrame.state._2
          processedFrame.globalEvents.foreach(e => gameEngine.globalEventStream.pushGlobalEvent(e))

          val scene = processedFrame.state._3

          // Process events
          SceneGraphViewEvents.collectViewEvents(boundaryLocator, scene.gameLayer.nodes, collectedEvents, gameEngine.globalEventStream.pushGlobalEvent)
          SceneGraphViewEvents.collectViewEvents(boundaryLocator, scene.lightingLayer.nodes, collectedEvents, gameEngine.globalEventStream.pushGlobalEvent)
          SceneGraphViewEvents.collectViewEvents(boundaryLocator, scene.uiLayer.nodes, collectedEvents, gameEngine.globalEventStream.pushGlobalEvent)

          // Play audio
          gameEngine.audioPlayer.playAudio(scene.audio)

          // Prepare scene
          val sceneData = sceneProcessor.processScene(
            gameTime,
            scene,
            gameEngine.assetMapping,
            gameEngine.renderer.screenWidth.toDouble,
            gameEngine.renderer.screenHeight.toDouble,
            gameEngine.renderer.orthographicProjectionMatrix
          )

          // Render scene
          gameEngine.renderer.drawScene(sceneData)
        } else {
          val processedFrame: Outcome[(GameModel, ViewModel)] =
            frameProcessor.runSkipView(startUpData, gameModelState, viewModelState, gameTime, collectedEvents, inputState, dice, boundaryLocator)

          // Persist frame state
          gameModelState = processedFrame.state._1
          viewModelState = processedFrame.state._2
          processedFrame.globalEvents.foreach(e => gameEngine.globalEventStream.pushGlobalEvent(e))
        }

      }

      gameEngine.platform.tick(gameEngine.gameLoop(time))
    } else
      gameEngine.platform.tick(loop(lastUpdateTime))
  }

}
