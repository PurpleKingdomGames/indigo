package indigo.gameengine

import indigo.shared.animation._
import indigo.shared.datatypes.FontInfo
import indigo.shared.config.GameConfig
import indigo.shared.assets.AssetType
import indigo.shared.IndigoLogger
import indigo.shared.Startup
import indigo.shared.Outcome
import indigo.platform.assets._
import indigo.platform.input.GamepadInputCaptureImpl
import indigo.platform.events.GlobalEventStream
import indigo.platform.Platform

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import indigo.platform.storage.Storage
import indigo.shared.input.GamepadInputCapture
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent

final class GameEngine[StartUpData, GameModel, ViewModel](
    fonts: Set[FontInfo],
    animations: Set[Animation],
    initialise: AssetCollection => Dice => Outcome[Startup[StartUpData]],
    initialModel: StartUpData => Outcome[GameModel],
    initialViewModel: StartUpData => GameModel => Outcome[ViewModel],
    frameProccessor: FrameProcessor[StartUpData, GameModel, ViewModel],
    initialisationEvents: List[GlobalEvent]
) {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var gameLoopInstance: GameLoop[StartUpData, GameModel, ViewModel] = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private[gameengine] var globalEventStream: GlobalEventStream = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private[gameengine] var gamepadInputCapture: GamepadInputCapture = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private[gameengine] var gameLoop: Long => Long => Unit = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private[gameengine] var startUpData: StartUpData = _
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private[gameengine] var platform: Platform = null

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def start(
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      bootEvents: List[GlobalEvent]
  ): Unit = {
    IndigoLogger.info("Starting Indigo")

    // Arrange config
    configAsync.map(_.getOrElse(config)).foreach { gameConfig =>
      // Intialisation / Boot events
      globalEventStream = new GlobalEventStream(rebuildGameLoop(false, gameConfig), Storage.default, platform)
      initialisationEvents.foreach(globalEventStream.pushGlobalEvent)
      bootEvents.foreach(globalEventStream.pushGlobalEvent)
      gamepadInputCapture = GamepadInputCaptureImpl()

      IndigoLogger.info("Configuration: " + gameConfig.asString)

      if ((gameConfig.viewport.width % 2 != 0) || (gameConfig.viewport.height % 2 != 0))
        IndigoLogger.info(
          "WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!"
        )

      // Arrange initial asset load
      IndigoLogger.info("Attempting to load assets")

      assetsAsync.flatMap(aa => AssetLoader.loadAssets(aa ++ assets)).foreach { assetCollection =>
        IndigoLogger.info("Asset load complete")

        if (platform == null) {
          platform = new Platform(gameConfig, globalEventStream)
        }

        rebuildGameLoop(true, gameConfig)(assetCollection)

        if (gameLoop != null)
          platform.tick(gameLoop(0))
      }

    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def rebuildGameLoop(firstRun: Boolean, gameConfig: GameConfig): AssetCollection => Unit =
    ac => {

      platform.purgeTextureAtlasCaches()

      val time = if (firstRun) 0 else gameLoopInstance.runningTimeReference

      platform.addAssetsToCollection(ac)

      val accumulatedAssetCollection = platform.giveAssetCollection

      platform.audioPlayer.addAudioAssets(accumulatedAssetCollection.sounds)

      initialise(accumulatedAssetCollection)(Dice.fromSeed(time)) match {
        case oe @ Outcome.Error(error, _) =>
          IndigoLogger.error(if (firstRun) "Error during first initialisation - Halting." else "Error during re-initialisation - Halting.")
          IndigoLogger.error("Crash report:")
          IndigoLogger.error(oe.reportCrash)
          throw error

        case Outcome.Result(startupData, globalEvents) =>
          globalEvents.foreach(globalEventStream.pushGlobalEvent)

          // Additive only. Adds new, does not replace existing.
          platform.registerAllAnimations(animations ++ startupData.additionalAnimations)
          platform.registerAllFonts(fonts ++ startupData.additionalFonts)

          def modelToUse(startUpSuccessData: => StartUpData): Outcome[GameModel] =
            if (firstRun) initialModel(startUpSuccessData)
            else Outcome(gameLoopInstance.gameModelState)

          def viewModelToUse(startUpSuccessData: => StartUpData, m: GameModel): Outcome[GameModel => ViewModel] =
            if (firstRun) initialViewModel(startUpSuccessData)(m).map(vm => (_: GameModel) => vm)
            else Outcome((_: GameModel) => gameLoopInstance.viewModelState)

          def gameLoopStart(m: GameModel, vm: GameModel => ViewModel): Outcome[GameLoop[StartUpData, GameModel, ViewModel]] =
            if (firstRun) {
              Outcome(
                new GameLoop[StartUpData, GameModel, ViewModel](
                  platform.giveBoundaryLocator,
                  this,
                  gameConfig,
                  m,
                  vm(m),
                  frameProccessor
                )
              )
            } else Outcome(gameLoopInstance)

          val loop: Outcome[Long => Long => Unit] =
            for {
              _                   <- if (firstRun) platform.initialise() else platform.reinitialise()
              startUpSuccessData  <- GameEngine.initialisedGame(startupData)
              m                   <- modelToUse(startUpSuccessData)
              vm                  <- viewModelToUse(startUpSuccessData, m)
              initialisedGameLoop <- gameLoopStart(m, vm)
            } yield {
              if (firstRun) {
                gameLoopInstance = initialisedGameLoop
              }

              startUpData = startUpSuccessData
              gameLoopInstance.loop
            }

          loop match {
            case Outcome.Result(firstTick, events) =>
              IndigoLogger.info("Starting main loop, there will be no more info log messages.")
              IndigoLogger.info("You may get first occurrence error logs.")

              events.foreach(globalEventStream.pushGlobalEvent)

              gameLoop = firstTick

              ()

            case oe @ Outcome.Error(e, _) =>
              IndigoLogger.error(if (firstRun) "Error during first engine start up" else "Error during engine restart")
              IndigoLogger.error(oe.reportCrash)
              throw e
          }

      }
    }

}

object GameEngine {

  def initialisedGame[StartUpData](startupData: Startup[StartUpData]): Outcome[StartUpData] =
    startupData match {
      case e: Startup.Failure =>
        IndigoLogger.info("Game initialisation failed")
        IndigoLogger.info(e.report)
        Outcome.raiseError(new Exception("Game aborted due to start up failure"))

      case x: Startup.Success[_] =>
        IndigoLogger.info("Game initialisation succeeded")
        Outcome(x.success)
    }

}
