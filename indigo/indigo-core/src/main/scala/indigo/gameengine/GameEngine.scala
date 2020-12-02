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
import indigo.platform.audio.AudioPlayer
import indigo.platform.input.GamepadInputCaptureImpl
import indigo.platform.events.GlobalEventStream
import indigo.platform.renderer.Renderer
import indigo.platform.Platform
import indigo.shared.platform.AssetMapping

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import indigo.shared.EqualTo._
import indigo.platform.storage.Storage
import indigo.shared.input.GamepadInputCapture
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import indigo.shared.BoundaryLocator
import indigo.shared.platform.SceneProcessor
import indigo.shared.dice.Dice

final class GameEngine[StartUpData, GameModel, ViewModel](
    fonts: Set[FontInfo],
    animations: Set[Animation],
    initialise: AssetCollection => Dice => Startup[StartUpData],
    initialModel: StartUpData => GameModel,
    initialViewModel: StartUpData => GameModel => ViewModel,
    frameProccessor: FrameProcessor[StartUpData, GameModel, ViewModel]
) {

  val animationsRegister: AnimationsRegister =
    new AnimationsRegister()
  val fontRegister: FontRegister =
    new FontRegister()
  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(animationsRegister, fontRegister)
  val sceneProcessor: SceneProcessor =
    new SceneProcessor(boundaryLocator, animationsRegister, fontRegister)

  val audioPlayer: AudioPlayer =
    AudioPlayer.init

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gameConfig: GameConfig = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var storage: Storage = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var globalEventStream: GlobalEventStream = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gamepadInputCapture: GamepadInputCapture = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gameLoop: Long => Long => Unit = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var gameLoopInstance: GameLoop[StartUpData, GameModel, ViewModel] = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var accumulatedAssetCollection: AssetCollection = AssetCollection.empty
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var assetMapping: AssetMapping = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var renderer: Renderer = null
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var startUpData: StartUpData = _
  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var platform: Platform = null

  @SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.GlobalExecutionContext"))
  def start(
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]]
  ): Unit = {

    IndigoLogger.info("Starting Indigo")

    storage = Storage.default
    globalEventStream = new GlobalEventStream(rebuildGameLoop(false), audioPlayer, storage, platform)
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

        rebuildGameLoop(true)(assetCollection)

        if (gameLoop != null)
          platform.tick(gameLoop(0))
      }

    }
  }

  def rebuildGameLoop(firstRun: Boolean): AssetCollection => Unit =
    ac => {

      fontRegister.clearRegister()
      boundaryLocator.purgeCache()
      sceneProcessor.purgeCaches()

      accumulatedAssetCollection = accumulatedAssetCollection |+| ac

      audioPlayer.addAudioAssets(accumulatedAssetCollection.sounds)

      val time = if (firstRun) 0 else gameLoopInstance.runningTimeReference

      platform = new Platform(gameConfig, accumulatedAssetCollection, globalEventStream)

      val startupData: Startup[StartUpData] =
        initialise(accumulatedAssetCollection)(Dice.fromSeed(time))

      startupData.startUpEvents.foreach(globalEventStream.pushGlobalEvent)

      GameEngine.registerAnimations(animationsRegister, animations ++ startupData.additionalAnimations)

      GameEngine.registerFonts(fontRegister, fonts ++ startupData.additionalFonts)

      val loop: Try[Long => Long => Unit] =
        for {
          rendererAndAssetMapping <- platform.initialise()
          startUpSuccessData      <- GameEngine.initialisedGame(startupData)
          initialisedGameLoop <- GameEngine.initialiseGameLoop(
            this,
            boundaryLocator,
            sceneProcessor,
            gameConfig,
            if (firstRun) initialModel(startUpSuccessData) else gameLoopInstance.gameModelState,
            if (firstRun) initialViewModel(startUpSuccessData) else (_: GameModel) => gameLoopInstance.viewModelState,
            frameProccessor
          )
        } yield {
          renderer = rendererAndAssetMapping._1
          assetMapping = rendererAndAssetMapping._2
          gameLoopInstance = initialisedGameLoop
          startUpData = startUpSuccessData
          initialisedGameLoop.loop
        }

      loop match {
        case Success(firstTick) =>
          IndigoLogger.info("Starting main loop, there will be no more info log messages.")
          IndigoLogger.info("You may get first occurrence error logs.")

          gameLoop = firstTick

          ()

        case Failure(e) =>
          IndigoLogger.error("Error during startup")
          IndigoLogger.error(e.getMessage)

          ()
      }
    }

}

object GameEngine {

  def registerAnimations(animationsRegister: AnimationsRegister, animations: Set[Animation]): Unit =
    animations.foreach(animationsRegister.register)

  def registerFonts(fontRegister: FontRegister, fonts: Set[FontInfo]): Unit =
    fonts.foreach(fontRegister.register)

  def initialisedGame[StartUpData](startupData: Startup[StartUpData]): Try[StartUpData] =
    startupData match {
      case e: Startup.Failure =>
        IndigoLogger.info("Game initialisation failed")
        IndigoLogger.info(e.report)
        Failure[StartUpData](new Exception("Game aborted due to start up failure"))

      case x: Startup.Success[_] =>
        IndigoLogger.info("Game initialisation succeeded")
        Success(x.success)
    }

  def initialiseGameLoop[StartUpData, GameModel, ViewModel](
      gameEngine: GameEngine[StartUpData, GameModel, ViewModel],
      boundaryLocator: BoundaryLocator,
      sceneProcessor: SceneProcessor,
      gameConfig: GameConfig,
      initialModel: GameModel,
      initialViewModel: GameModel => ViewModel,
      frameProccessor: FrameProcessor[StartUpData, GameModel, ViewModel]
  ): Try[GameLoop[StartUpData, GameModel, ViewModel]] =
    Success(
      new GameLoop[StartUpData, GameModel, ViewModel](
        boundaryLocator,
        sceneProcessor,
        gameEngine,
        gameConfig,
        initialModel,
        initialViewModel(initialModel),
        frameProccessor
      )
    )

}
