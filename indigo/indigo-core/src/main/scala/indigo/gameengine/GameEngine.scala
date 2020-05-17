package indigo.gameengine

import indigo.shared.animation._
import indigo.shared.datatypes.FontInfo
import indigo.shared.config.GameConfig
import indigo.shared.assets.AssetType
import indigo.shared.IndigoLogger
import indigo.shared.Startup
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.platform.assets._
import indigo.platform.audio.AudioPlayerImpl
import indigo.platform.input.GamepadInputCaptureImpl
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
import indigo.platform.DisplayObjectConversions
import indigo.shared.BoundaryLocator
import indigo.shared.dice.Dice

final class GameEngine[StartupData, StartupError, GameModel, ViewModel](
    fonts: Set[FontInfo],
    animations: Set[Animation],
    initialise: AssetCollection => Dice => Map[String, String] => Startup[StartupError, StartupData],
    initialModel: StartupData => GameModel,
    initialViewModel: StartupData => GameModel => ViewModel,
    frameProccessor: FrameProcessor[GameModel, ViewModel]
) {

  val animationsRegister: AnimationsRegister =
    new AnimationsRegister()
  val fontRegister: FontRegister =
    new FontRegister()
  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(animationsRegister, fontRegister)

  val audioPlayer: AudioPlayerImpl =
    AudioPlayerImpl.init

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gameConfig: GameConfig = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var storage: Storage = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var globalEventStream: GlobalEventStream = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gamepadInputCapture: GamepadInputCapture = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gameLoop: Try[() => Unit] = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gameLoopInstance: GameLoop[GameModel, ViewModel] = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var accumulatedAssetCollection: AssetCollection = AssetCollection.empty
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var assetMapping: AssetMapping = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var renderer: Renderer = null

  def start(
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]]
  )(flags: Map[String, String]): Unit = {

    IndigoLogger.info("Starting Indigo")

    PlatformWindow.windowSetup(config)

    storage = PlatformStorage.default
    globalEventStream = GlobalEventStreamImpl.default(rebuildGameLoop(false, flags), audioPlayer, storage)
    gamepadInputCapture = GamepadInputCaptureImpl()

    // Arrange config
    configAsync.map(_.getOrElse(config)).foreach { gc =>
      gameConfig = gc

      IndigoLogger.info("Configuration: " + gameConfig.asString)

      if ((gameConfig.viewport.width % 2 !== 0) || (gameConfig.viewport.height % 2 !== 0))
        IndigoLogger.info(
          "WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!"
        )

      // Arrange initial asset load
      IndigoLogger.info("Attempting to load assets")

      assetsAsync.flatMap(aa => AssetLoader.loadAssets(aa ++ assets)).foreach { assetCollection =>
        IndigoLogger.info("Asset load complete")

        rebuildGameLoop(true, flags)(assetCollection)

        gameLoop match {
          case Success(firstTick) =>
            IndigoLogger.info("Starting main loop, there will be no more info log messages.")
            IndigoLogger.info("You may get first occurrence error logs.")
            firstTick()

          case Failure(e) =>
            IndigoLogger.error("Error during startup")
            IndigoLogger.error(e.getMessage)

            ()
        }
      }

    }
  }

  def rebuildGameLoop(firstRun: Boolean, flags: Map[String, String]): AssetCollection => Unit = ac => {

    fontRegister.clearRegister()
    DisplayObjectConversions.purgeCaches()
    boundaryLocator.purgeCache()

    accumulatedAssetCollection = accumulatedAssetCollection |+| ac

    audioPlayer.addAudioAssets(accumulatedAssetCollection.sounds)

    val platform: Platform =
      new PlatformImpl(accumulatedAssetCollection, globalEventStream, boundaryLocator, animationsRegister, fontRegister)

    val startupData: Startup[StartupError, StartupData] = initialise(accumulatedAssetCollection)(Dice.fromSeed(0))(flags)

    GameEngine.registerAnimations(animationsRegister, animations ++ startupData.additionalAnimations)

    GameEngine.registerFonts(fontRegister, fonts ++ startupData.additionalFonts)

    val loop: Try[Long => Unit] =
      for {
        rendererAndAssetMapping <- platform.initialiseRenderer(gameConfig)
        startUpSuccessData      <- GameEngine.initialisedGame(startupData)
        initialisedGameLoop <- GameEngine.initialiseGameLoop(
          this,
          boundaryLocator,
          gameConfig,
          if (firstRun) initialModel(startUpSuccessData) else gameLoopInstance.gameModelState,
          if (firstRun) initialViewModel(startUpSuccessData) else (_: GameModel) => gameLoopInstance.viewModelState,
          frameProccessor,
          platform.tick
        )
      } yield {
        renderer = rendererAndAssetMapping._1
        assetMapping = rendererAndAssetMapping._2
        val time = if (firstRun) 0 else gameLoopInstance.runningTimeReference
        gameLoopInstance = initialisedGameLoop
        initialisedGameLoop.loop(time)
      }
    gameLoop = loop.map(f => (() => platform.tick(f)))

    ()
  }

}

object GameEngine {

  def registerAnimations(animationsRegister: AnimationsRegister, animations: Set[Animation]): Unit =
    animations.foreach(animationsRegister.register)

  def registerFonts(fontRegister: FontRegister, fonts: Set[FontInfo]): Unit =
    fonts.foreach(fontRegister.register)

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

  def initialiseGameLoop[StartupData, StartupError, GameModel, ViewModel](
      gameEngine: GameEngine[StartupData, StartupError, GameModel, ViewModel],
      boundaryLocator: BoundaryLocator,
      gameConfig: GameConfig,
      initialModel: GameModel,
      initialViewModel: GameModel => ViewModel,
      frameProccessor: FrameProcessor[GameModel, ViewModel],
      callTick: (Long => Unit) => Unit
  ): Try[GameLoop[GameModel, ViewModel]] =
    Success(
      new GameLoop[GameModel, ViewModel](
        boundaryLocator,
        gameEngine,
        gameConfig,
        initialModel,
        initialViewModel(initialModel),
        frameProccessor,
        callTick
      )
    )

}
