package indigo.gameengine

import indigo.shared.BoundaryLocator
import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.config.GameConfig
import indigo.shared.dice.Dice
import indigo.shared.events.FrameTick
import indigo.shared.events.InputEvent
import indigo.shared.events.InputState
import indigo.shared.platform.SceneProcessor
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

import scala.scalajs.js.JSConverters._

final class GameLoop[StartUpData, GameModel, ViewModel](
    boundaryLocator: BoundaryLocator,
    sceneProcessor: SceneProcessor,
    gameEngine: GameEngine[StartUpData, GameModel, ViewModel],
    gameConfig: GameConfig,
    initialModel: GameModel,
    initialViewModel: ViewModel,
    frameProcessor: FrameProcessor[StartUpData, GameModel, ViewModel]
):

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _gameModelState: GameModel = initialModel
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _viewModelState: ViewModel = initialViewModel
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _runningTimeReference: Long = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _inputState: InputState = InputState.default
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _running: Boolean = true

  def gameModelState: GameModel  = _gameModelState
  def viewModelState: ViewModel  = _viewModelState
  def runningTimeReference: Long = _runningTimeReference

  private val runner: (Long, Long, Long) => Unit =
    gameConfig.frameRateLimit match
      case None =>
        (time, timeDelta, lastUpdateTime) =>
          runFrame(time, timeDelta)
          gameEngine.platform.tick(gameEngine.gameLoop(time))

      case Some(fps) =>
        (time, timeDelta, lastUpdateTime) =>
          if timeDelta >= gameConfig.frameRateDeltaMillis.toLong - 1 then
            runFrame(time, timeDelta)
            gameEngine.platform.tick(gameEngine.gameLoop(time))
          else gameEngine.platform.tick(loop(lastUpdateTime))

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def kill(): Unit =
    _running = false
    _gameModelState = null.asInstanceOf[GameModel]
    _viewModelState = null.asInstanceOf[ViewModel]
    _runningTimeReference = 0
    _inputState = null
    ()

  def loop(lastUpdateTime: Long): Long => Unit = { time =>
    _runningTimeReference = time
    val timeDelta: Long = time - lastUpdateTime
    if _running then runner(time, timeDelta, lastUpdateTime)
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runFrame(time: Long, timeDelta: Long): Unit =

    val gameTime = new GameTime(Millis(time).toSeconds, Millis(timeDelta).toSeconds, gameConfig.frameRateLimit)
    val events   = gameEngine.globalEventStream.collect ++ List(FrameTick)

    // Persist input state
    _inputState = InputState.calculateNext(
      _inputState,
      events.collect { case e: InputEvent => e },
      gameEngine.gamepadInputCapture.giveGamepadState
    )

    // Run the frame processor
    val processedFrame: Outcome[(GameModel, ViewModel, SceneUpdateFragment)] =
      frameProcessor.run(
        gameEngine.startUpData,
        _gameModelState,
        _viewModelState,
        gameTime,
        events,
        _inputState,
        Dice.fromSeconds(gameTime.running),
        boundaryLocator
      )

    // Persist frame state
    val scene =
      processedFrame match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("The game has crashed...")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result((gameModel, viewModel, sceneUpdateFragment), globalEvents) =>
          _gameModelState = gameModel
          _viewModelState = viewModel
          globalEvents.foreach(e => gameEngine.globalEventStream.pushGlobalEvent(e))
          sceneUpdateFragment

    // Play audio
    gameEngine.audioPlayer.playAudio(scene.audio)

    // Prepare scene
    val sceneData = sceneProcessor.processScene(
      gameTime,
      scene,
      gameEngine.assetMapping,
      gameEngine.renderer.renderingTechnology,
      gameConfig.advanced.batchSize,
      events.toJSArray,
      gameEngine.globalEventStream.pushGlobalEvent
    )

    // Render scene
    gameEngine.renderer.drawScene(sceneData, gameTime.running)
