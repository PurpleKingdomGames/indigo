package indigo.gameengine

import indigo.shared.animation._
import indigo.shared.datatypes.FontInfo
import indigo.shared.metrics._
import indigo.shared.{AssetType, GameConfig}
import indigo.shared.IndigoLogger
import indigo.shared.Startup
import indigo.shared.GameContext
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.platform.assets._
import indigo.platform.audio.AudioPlayerImpl
import indigo.shared.platform.AudioPlayer
import indigo.shared.platform.GlobalEventStream
import indigo.shared.platform.GlobalSignals
import indigo.platform.events.GlobalEventStreamImpl
import indigo.shared.platform.Platform
import indigo.shared.platform.Renderer
import indigo.platform.PlatformImpl
import indigo.shared.platform.AssetMapping

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import indigo.shared.EqualTo._

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

        val metrics: Metrics =
          Metrics.getInstance(gameConfig.advanced.recordMetrics, gameConfig.advanced.logMetricsReportIntervalMs)

        val globalEventStream: GlobalEventStream =
          GlobalEventStreamImpl.default(audioPlayer)

        val globalSignals: GlobalSignals =
          GlobalSignals.default

        val startupData: Startup[StartupError, StartupData] = initialise(assetCollection)

        val platform: Platform =
          new PlatformImpl(assetCollection, globalEventStream)

        val gameLoop: GameContext[Long => Unit] =
          for {
            _ <- GameEngine.registerAnimations(animations ++ startupData.additionalAnimations)
            _ <- GameEngine.registerFonts(fonts ++ startupData.additionalFonts)

            // textureAtlas        <- GameEngine.createTextureAtlas(assetCollection)
            // loadedTextureAssets <- GameEngine.extractLoadedTextures(textureAtlas)
            // assetMapping        <- GameEngine.setupAssetMapping(textureAtlas)

            startUpSuccessData <- GameEngine.initialisedGame(startupData)

            // canvas   <- GameEngine.createCanvas(gameConfig)
            // _        <- GameEngine.listenToWorldEvents(canvas, gameConfig.magnification, globalEventStream)
            // renderer <- GameEngine.startRenderer(gameConfig, loadedTextureAssets, canvas)

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
              globalSignals,
              platform.tick
            )
          } yield gameLoopInstance.loop(0)

        gameLoop.attemptRun match {
          case Right(f) =>
            IndigoLogger.info("Starting main loop, there will be no more info log messages.")
            IndigoLogger.info("You may get first occurrence error logs.")
            platform.tick(f)

          case Left(e) =>
            IndigoLogger.error("Error during startup")
            IndigoLogger.error(e.getMessage)

            ()
        }
      }

    }
  }

  def registerAnimations(animations: Set[Animation]): GameContext[Unit] =
    GameContext.delay(animations.foreach(AnimationsRegister.register))

  def registerFonts(fonts: Set[FontInfo]): GameContext[Unit] =
    GameContext.delay(fonts.foreach(FontRegister.register))

  // def createTextureAtlas(assetCollection: AssetCollection): GameContext[TextureAtlas] =
  //   GameContext.delay(
  //     TextureAtlas.create(
  //       assetCollection.images.map(i => ImageRef(i.name.name, i.data.width, i.data.height)),
  //       (name: String) => assetCollection.images.find(_.name.name === name),
  //       TextureAtlasFunctions.createAtlasData
  //     )
  //   )

  // def extractLoadedTextures(textureAtlas: TextureAtlas): GameContext[List[LoadedTextureAsset]] =
  //   GameContext.delay(
  //     textureAtlas.atlases.toList
  //       .map(a => a._2.imageData.map(data => new LoadedTextureAsset(a._1.id, data)))
  //       .collect { case Some(s) => s }
  //   )

  // def setupAssetMapping(textureAtlas: TextureAtlas): GameContext[AssetMapping] =
  //   GameContext.delay(
  //     new AssetMapping(
  //       mappings = textureAtlas.legend
  //         .map { p =>
  //           p._1 -> new TextureRefAndOffset(
  //             atlasName = p._2.id.id,
  //             atlasSize = textureAtlas.atlases.get(p._2.id).map(_.size.value).map(Vector2.apply).getOrElse(Vector2.one),
  //             offset = p._2.offset
  //           )
  //         }
  //     )
  //   )

  def initialisedGame[StartupError, StartupData](startupData: Startup[StartupError, StartupData]): GameContext[StartupData] =
    startupData match {
      case e: Startup.Failure[_] =>
        IndigoLogger.info("Game initialisation failed")
        IndigoLogger.info(e.report)
        GameContext.raiseError[StartupData](new Exception("Game aborted due to start up failure"))

      case x: Startup.Success[StartupData] =>
        IndigoLogger.info("Game initialisation succeeded")
        GameContext.delay(x.success)
    }

  // def createCanvas(gameConfig: GameConfig): GameContext[Canvas] =
  //   Option(dom.document.getElementById("indigo-container")) match {
  //     case None =>
  //       GameContext.raiseError[Canvas](new Exception("""Parent element "indigo-container" could not be found on page."""))

  //     case Some(parent) =>
  //       GameContext.delay(Renderer.createCanvas(gameConfig.viewport.width, gameConfig.viewport.height, parent))
  //   }

  // def listenToWorldEvents(canvas: Canvas, magnification: Int, globalEventStream: GlobalEventStream): GameContext[Unit] = {
  //   IndigoLogger.info("Starting world events")
  //   GameContext.delay(WorldEvents.init(canvas, magnification, globalEventStream))
  // }

  // def startRenderer(gameConfig: GameConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: Canvas): GameContext[Renderer] =
  //   GameContext.delay {
  //     IndigoLogger.info("Starting renderer")
  //     Renderer(
  //       new RendererConfig(
  //         viewport = new Viewport(gameConfig.viewport.width, gameConfig.viewport.height),
  //         clearColor = gameConfig.clearColor,
  //         magnification = gameConfig.magnification
  //       ),
  //       loadedTextureAssets,
  //       canvas
  //     )
  //   }

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
      globalSignals: GlobalSignals,
      callTick: (Long => Unit) => Unit
  ): GameContext[GameLoop[GameModel, ViewModel]] =
    GameContext.delay(
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
        globalSignals,
        callTick
      )
    )

}
