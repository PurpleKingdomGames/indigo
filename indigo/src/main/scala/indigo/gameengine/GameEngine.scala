package indigo.gameengine

import indigo.gameengine.assets._
import indigo.gameengine.audio.{AudioPlayer, IAudioPlayer}
import indigo.gameengine.events._
import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.datatypes.FontInfo
import indigo.renderer._
import indigo.runtime._
import indigo.runtime.metrics._
import indigo.shared.{AssetType, GameConfig}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class GameTime(running: Double, delta: Double, frameDuration: Double)

object GameTime {
  def now(frameDuration: Double): GameTime                                = GameTime(System.currentTimeMillis().toDouble, 0, frameDuration)
  def zero(frameDuration: Double): GameTime                               = GameTime(0, 0, frameDuration)
  def is(running: Double, delta: Double, frameDuration: Double): GameTime = GameTime(running, delta, frameDuration)
}

class GameEngine[StartupData, StartupError, GameModel, ViewModel](
    config: GameConfig,
    configAsync: Future[Option[GameConfig]],
    assets: Set[AssetType],
    assetsAsync: Future[Set[AssetType]],
    fonts: Set[FontInfo],
    animations: Set[Animations],
    initialise: AssetCollection => Startup[StartupError, StartupData],
    initialModel: StartupData => GameModel,
    updateModel: (GameTime, GameModel) => GameEvent => UpdatedModel[GameModel],
    initialViewModel: StartupData => GameModel => ViewModel,
    updateViewModel: (GameTime, GameModel, ViewModel, FrameInputEvents) => UpdatedViewModel[ViewModel],
    updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment
) {

  def registerAnimations(animations: Animations): Unit =
    AnimationsRegister.register(animations)

  def registerFont(fontInfo: FontInfo): Unit =
    FontRegister.register(fontInfo)

  def start(): Unit = {

    IndigoLogger.info("Starting Indigo")

    // Arrange config
    configAsync.map(_.getOrElse(config)).foreach { gameConfig =>
      IndigoLogger.info("Configuration: " + gameConfig.asString)

      if (gameConfig.viewport.width % 2 != 0 || gameConfig.viewport.height % 2 != 0)
        IndigoLogger.info(
          "WARNING: Setting a resolution that has a width and/or height that is not divisible by 2 could cause stretched graphics!"
        )

      // Arrange assets
      assetsAsync.flatMap(aa => AssetManager.loadAssets(aa ++ assets)).foreach { assetCollection =>
        IndigoLogger.info("Asset load complete")

        val audioPlayer: IAudioPlayer =
          GameEngine.startAudioPlayer(assetCollection.sounds)

        implicit val metrics: IMetrics =
          Metrics.getInstance(gameConfig.advanced.recordMetrics, gameConfig.advanced.logMetricsReportIntervalMs)

        implicit val globalEventStream: GlobalEventStream =
          GlobalEventStream.default(audioPlayer)

        val x: IIO[Double => Int] =
          for {
            _                   <- GameEngine.registerAnimations(animations)
            _                   <- GameEngine.registerFonts(fonts)
            textureAtlas        <- GameEngine.createTextureAtlas(assetCollection)
            loadedTextureAssets <- GameEngine.extractLoadedTextures(textureAtlas)
            assetMapping        <- GameEngine.setupAssetMapping(textureAtlas)
            startUpSuccessData  <- GameEngine.initialisedGame(initialise(assetCollection))
            canvas              <- GameEngine.createCanvas(gameConfig)
            _                   <- GameEngine.listenToWorldEvents(canvas, gameConfig.magnification)
            renderer            <- GameEngine.startRenderer(gameConfig, loadedTextureAssets, canvas)
            gameLoopInstance <- GameEngine.initialiseGameLoop(
              gameConfig,
              assetMapping,
              renderer,
              audioPlayer,
              initialModel(startUpSuccessData),
              updateModel,
              initialViewModel(startUpSuccessData),
              updateViewModel,
              updateView
            )
          } yield gameLoopInstance.loop(0)

        x.attemptRun match {
          case Right(f) =>
            IndigoLogger.info("Starting main loop, there will be no more info log messages.")
            IndigoLogger.info("You may get first occurrence error logs.")
            dom.window.requestAnimationFrame(f)

            ()

          case Left(e) =>
            IndigoLogger.error("Error during startup")
            IndigoLogger.error(e.getMessage)

            ()
        }
      }

    }

  }

}

object GameEngine {

  def registerAnimations(animations: Set[Animations]): IIO[Unit] =
    IIO.delay(animations.foreach(AnimationsRegister.register))

  def registerFonts(fonts: Set[FontInfo]): IIO[Unit] =
    IIO.delay(fonts.foreach(FontRegister.register))

  def createTextureAtlas(assetCollection: AssetCollection): IIO[TextureAtlas] =
    IIO.delay(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height)),
        AssetManager.findByName(assetCollection),
        TextureAtlasFunctions.createAtlasData
      )
    )

  def extractLoadedTextures(textureAtlas: TextureAtlas): IIO[List[LoadedTextureAsset]] =
    IIO.delay(
      textureAtlas.atlases.toList
        .map(a => a._2.imageData.map(data => LoadedTextureAsset(a._1.id, data)))
        .collect { case Some(s) => s }
    )

  def setupAssetMapping(textureAtlas: TextureAtlas): IIO[AssetMapping] =
    IIO.delay(
      AssetMapping(
        mappings = textureAtlas.legend
          .map { p =>
            p._1 -> TextureRefAndOffset(
              atlasName = p._2.id.id,
              atlasSize = textureAtlas.atlases.get(p._2.id).map(_.size.value).map(Vector2.apply).getOrElse(Vector2.one),
              offset = p._2.offset
            )
          }
      )
    )

  def initialisedGame[StartupError, StartupData](startupData: Startup[StartupError, StartupData]): IIO[StartupData] =
    startupData match {
      case e: Startup.Failure[_] =>
        IndigoLogger.info("Game initialisation failed")
        IndigoLogger.info(e.report)
        IIO.raiseError(new Exception("Game aborted due to start up failure"))

      case x: Startup.Success[StartupData] =>
        IndigoLogger.info("Game initialisation succeeded")
        IIO.delay(x.success)
    }

  def createCanvas(gameConfig: GameConfig): IIO[Canvas] =
    IIO.delay(Renderer.createCanvas(gameConfig.viewport.width, gameConfig.viewport.height))

  def listenToWorldEvents(canvas: Canvas, magnification: Int)(implicit globalEventStream: GlobalEventStream): IIO[Unit] = {
    IndigoLogger.info("Starting world events")
    IIO.delay(WorldEvents(canvas, magnification))
  }

  def startRenderer(gameConfig: GameConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: Canvas): IIO[IRenderer] =
    IIO.delay {
      IndigoLogger.info("Starting renderer")
      Renderer(
        RendererConfig(
          viewport = Viewport(gameConfig.viewport.width, gameConfig.viewport.height),
          clearColor = gameConfig.clearColor,
          magnification = gameConfig.magnification
        ),
        loadedTextureAssets,
        canvas
      )
    }

  def startAudioPlayer(sounds: List[LoadedAudioAsset]): IAudioPlayer =
    AudioPlayer(sounds)

  def initialiseGameLoop[GameModel, ViewModel](
      gameConfig: GameConfig,
      assetMapping: AssetMapping,
      renderer: IRenderer,
      audioPlayer: IAudioPlayer,
      initialModel: GameModel,
      updateModel: (GameTime, GameModel) => GameEvent => UpdatedModel[GameModel],
      initialViewModel: GameModel => ViewModel,
      updateViewModel: (GameTime, GameModel, ViewModel, FrameInputEvents) => UpdatedViewModel[ViewModel],
      updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment
  )(implicit metrics: IMetrics, globalEventStream: GlobalEventStream): IIO[GameLoop[GameModel, ViewModel]] =
    IIO.delay(
      new GameLoop[GameModel, ViewModel](gameConfig, assetMapping, renderer, audioPlayer, initialModel, updateModel, initialViewModel(initialModel), updateViewModel, updateView)
    )

}
