package indigo.gameengine

import indigo.platform.assets.AssetCollection
import indigo.platform.renderer.Renderer
import indigo.shared.BoundaryLocator
import indigo.shared.Context
import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.config.GameConfig
import indigo.shared.config.GameViewport
import indigo.shared.dice.Dice
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.events.IndigoSystemEvent
import indigo.shared.events.InputEvent
import indigo.shared.events.InputState
import indigo.shared.platform.SceneProcessor
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.time.GameTime
import indigo.shared.time.Millis

import scala.annotation.nowarn
import scala.collection.mutable
import scala.scalajs.js.JSConverters.*

final class GameLoop[StartUpData, GameModel, ViewModel](
    rebuildGameLoop: AssetCollection => Unit,
    boundaryLocator: BoundaryLocator,
    sceneProcessor: SceneProcessor,
    gameEngine: GameEngine[StartUpData, GameModel, ViewModel],
    gameConfig: GameConfig,
    initialModel: GameModel,
    initialViewModel: ViewModel,
    frameProcessor: FrameProcessor[StartUpData, GameModel, ViewModel],
    startFrameLocked: Boolean,
    renderer: => Renderer
):

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _gameModelState: GameModel = initialModel
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _viewModelState: ViewModel = initialViewModel
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _runningTimeReference: Double = 0
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _inputState: InputState = InputState.default
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _running: Boolean = true
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _frameLocked: Boolean = startFrameLocked

  private val systemActions: mutable.Queue[IndigoSystemEvent] =
    new mutable.Queue[IndigoSystemEvent]()

  private val frameDeltaRecord: scala.scalajs.js.Array[Double] = scala.scalajs.js.Array(0.0d, 0.0d, 0.0d, 0.0d, 0.0d)

  private val _randomInstance: scala.util.Random = new scala.util.Random()

  private lazy val _services: Context.Services =
    Context.Services(boundaryLocator, _randomInstance, renderer.captureScreen)

  def gameModelState: GameModel    = _gameModelState
  def viewModelState: ViewModel    = _viewModelState
  def runningTimeReference: Double = _runningTimeReference
  def lock(): Unit                 = _frameLocked = true
  def unlock(): Unit               = _frameLocked = false

  @nowarn("msg=unused")
  private val runner: (Double, Double, Double) => Unit =
    gameConfig.frameRateLimit match
      case None =>
        (time, timeDelta, lastUpdateTime) =>
          runFrame(time, timeDelta)
          gameEngine.platform.tick(gameEngine.gameLoop(time))

      case Some(fps) =>
        (time, timeDelta, lastUpdateTime) =>
          frameDeltaRecord.shift()
          frameDeltaRecord.push(timeDelta)

          val meanDelta = frameDeltaRecord.sum / 5.0d     // Same as number of inital entries
          val target    = gameConfig.frameRateDeltaMillis // E.g. 16.7ms for 60fps

          if timeDelta >= target then
            runFrame(time, timeDelta)
            gameEngine.platform.tick(gameEngine.gameLoop(time))
          else if timeDelta + meanDelta >= target then
            val diff = target - timeDelta
            val t    = time + diff

            gameEngine.platform.delay(
              diff,
              () => runFrame(t, timeDelta + diff)
            )
            gameEngine.platform.tick(gameEngine.gameLoop(t))
          else gameEngine.platform.tick(loop(lastUpdateTime))

  def kill(): Unit =
    _running = false
    ()

  def loop(lastUpdateTime: Double): Double => Unit = { time =>
    _runningTimeReference = time
    val timeDelta: Double = time - lastUpdateTime
    if _running then runner(time, timeDelta, lastUpdateTime)
  }

  private def runFrame(time: Double, timeDelta: Double): Unit =
    if _frameLocked then ()
    else if systemActions.size > 0 then performSystemActions(systemActions.dequeueAll(_ => true).toList)
    else runFrameNormal(time, timeDelta)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runFrameNormal(time: Double, timeDelta: Double): Unit =
    val gameTime =
      new GameTime(Millis(time.toLong).toSeconds, Millis(timeDelta.toLong).toSeconds, gameConfig.frameRateLimit)
    val events = gameEngine.globalEventStream.collect ++ Batch(FrameTick)

    // Persist input state
    _inputState = InputState.calculateNext(
      _inputState,
      events.collect { case e: InputEvent => e },
      gameEngine.gamepadInputCapture.giveGamepadState
    )

    val context =
      new Context[StartUpData](
        gameEngine.startUpData,
        Context.Frame(
          Dice.fromSeconds(gameTime.running),
          gameTime,
          _inputState,
          GameViewport(renderer.screenWidth, renderer.screenHeight),
          gameConfig.magnification
        ),
        _services
      )

    // Run the frame processor
    val processedFrame: Outcome[(GameModel, ViewModel, SceneUpdateFragment)] =
      frameProcessor.run(
        _gameModelState,
        _viewModelState,
        events,
        context
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

    // Process system events
    events
      .collect { case e: IndigoSystemEvent => e }
      .foreach(systemActions.enqueue)

  def performSystemActions(systemEvents: List[IndigoSystemEvent]): Unit =
    systemEvents.foreach { case IndigoSystemEvent.Rebuild(assetCollection, nextEvent) =>
      IndigoLogger.info("Rebuilding game loop from new asset collection.")
      rebuildGameLoop(assetCollection)
      gameEngine.globalEventStream.pushGlobalEvent(nextEvent)
    }
