package indigo.gameengine

import indigo.shared.animation._
import indigo.shared.datatypes.FontInfo
import indigo.shared.metrics._
import indigo.shared.config.GameConfig
import indigo.shared.AssetType
import indigo.shared.IndigoLogger
import indigo.shared.Startup
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.platform.assets._
import indigo.platform.audio.AudioPlayerImpl
import indigo.platform.input.GamepadInputCaptureImpl
import indigo.shared.platform.AudioPlayer
import indigo.shared.platform.GlobalEventStream
import indigo.platform.events.GlobalEventStreamImpl
import indigo.shared.platform.Platform
import indigo.shared.platform.Renderer
import indigo.platform.PlatformImpl
import indigo.platform.PlatformWindow
import indigo.shared.platform.AssetMapping

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import indigo.shared.EqualTo._
import indigo.shared.platform.Storage
import indigo.platform.storage.PlatformStorage
import indigo.shared.input.GamepadInputCapture
import scala.util.Try
import scala.util.Success
import scala.util.Failure

final class GameEngine[StartupData, StartupError, GameModel, ViewModel](
    config: GameConfig,
    configAsync: Future[Option[GameConfig]],
    assets: Set[AssetType],
    assetsAsync: Future[Set[AssetType]],
    fonts: Set[FontInfo],
    animations: Set[Animation],
    initialise: AssetCollection => Startup[StartupError, StartupData],
    initialModel: StartupData => GameModel,
    initialViewModel: StartupData => GameModel => ViewModel,
    frameProccessor: FrameProcessor[GameModel, ViewModel]
) {

  def start(): Unit =
    GameEngine.start(
      config,
      configAsync,
      assets,
      assetsAsync,
      fonts,
      animations,
      initialise,
      initialModel,
      initialViewModel,
      frameProccessor
    )

}

object GameEngine {

  def start[StartupData, StartupError, GameModel, ViewModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      initialViewModel: StartupData => GameModel => ViewModel,
      frameProccessor: FrameProcessor[GameModel, ViewModel]
  ): Unit = {

    IndigoLogger.info("Starting Indigo")

    PlatformWindow.windowSetup(config)

    // Arrange config
    configAsync.map(_.getOrElse(config)).foreach { gameConfig =>
      IndigoLogger.info("Configuration: " + gameConfig.asString)

      if ((gameConfig.viewport.width % 2 !== 0) || (gameConfig.viewport.height % 2 !== 0))
        IndigoLogger.info(
          "WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!"
        )

      // Arrange assets
      IndigoLogger.info("Attempting to load assets")

      assetsAsync.flatMap(aa => AssetLoader.loadAssets(aa ++ assets)).foreach { assetCollection =>
        IndigoLogger.info("Asset load complete")

        val audioPlayer: AudioPlayer =
          AudioPlayerImpl(assetCollection)

        val storage: Storage =
          PlatformStorage.default

        val metrics: Metrics =
          Metrics.getInstance(gameConfig.advanced.recordMetrics, gameConfig.advanced.logMetricsReportIntervalMs)

        val globalEventStream: GlobalEventStream =
          GlobalEventStreamImpl.default(audioPlayer, storage)

        val gamepadInputCapture: GamepadInputCapture =
          GamepadInputCaptureImpl()

        val startupData: Startup[StartupError, StartupData] = initialise(assetCollection)

        val platform: Platform =
          new PlatformImpl(assetCollection, globalEventStream)

        val gameLoop: Try[Long => Unit] =
          for {
            _                       <- GameEngine.registerAnimations(animations ++ startupData.additionalAnimations)
            _                       <- GameEngine.registerFonts(fonts ++ startupData.additionalFonts)
            startUpSuccessData      <- GameEngine.initialisedGame(startupData)
            rendererAndAssetMapping <- platform.initialiseRenderer(gameConfig)
            gameLoopInstance <- GameEngine.initialiseGameLoop(
              gameConfig,
              rendererAndAssetMapping._2,
              rendererAndAssetMapping._1,
              audioPlayer,
              initialModel(startUpSuccessData),
              initialViewModel(startUpSuccessData),
              frameProccessor,
              metrics,
              globalEventStream,
              gamepadInputCapture,
              platform.tick
            )
          } yield gameLoopInstance.loop(0)

        gameLoop match {
          case Success(f) =>
            IndigoLogger.info("Starting main loop, there will be no more info log messages.")
            IndigoLogger.info("You may get first occurrence error logs.")
            platform.tick(f)

          case Failure(e) =>
            IndigoLogger.error("Error during startup")
            IndigoLogger.error(e.getMessage)

            ()
        }
      }

    }
  }

  def registerAnimations(animations: Set[Animation]): Try[Unit] =
    Success(animations.foreach(AnimationsRegister.register))

  def registerFonts(fonts: Set[FontInfo]): Try[Unit] =
    Success(fonts.foreach(FontRegister.register))

  def initialisedGame[StartupError, StartupData](startupData: Startup[StartupError, StartupData]): Try[StartupData] =
    startupData match {
      case e: Startup.Failure[_] =>
        IndigoLogger.info("Game initialisation failed")
        IndigoLogger.info(e.report)
        Failure[StartupData](new Exception("Game aborted due to start up failure"))

      case x: Startup.Success[StartupData] =>
        IndigoLogger.info("Game initialisation succeeded")
        Success(x.success)
    }

  def initialiseGameLoop[GameModel, ViewModel](
      gameConfig: GameConfig,
      assetMapping: AssetMapping,
      renderer: Renderer,
      audioPlayer: AudioPlayer,
      initialModel: GameModel,
      initialViewModel: GameModel => ViewModel,
      frameProccessor: FrameProcessor[GameModel, ViewModel],
      metrics: Metrics,
      globalEventStream: GlobalEventStream,
      gamepadInputCapture: GamepadInputCapture,
      callTick: (Long => Unit) => Unit
  ): Try[GameLoop[GameModel, ViewModel]] =
    Success(
      new GameLoop[GameModel, ViewModel](
        gameConfig,
        assetMapping,
        renderer,
        audioPlayer,
        initialModel,
        initialViewModel(initialModel),
        frameProccessor,
        metrics,
        globalEventStream,
        gamepadInputCapture,
        callTick
      )
    )

}
