package indigo.platform

import indigo.platform.assets.AssetCollection
import indigo.platform.assets.AtlasId
import indigo.platform.assets.DynamicText
import indigo.platform.assets.ImageRef
import indigo.platform.assets.TextureAtlas
import indigo.platform.assets.TextureAtlasFunctions
import indigo.platform.events.GlobalEventStream
import indigo.platform.events.WorldEvents
import indigo.platform.input.GamepadInputCaptureImpl
import indigo.platform.renderer.Renderer
import indigo.platform.renderer.RendererInitialiser
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.assets.AssetName
import indigo.shared.config.GameConfig
import indigo.shared.datatypes.Vector2
import indigo.shared.events.FullScreenEnterError
import indigo.shared.events.FullScreenEntered
import indigo.shared.events.FullScreenExitError
import indigo.shared.events.FullScreenExited
import indigo.shared.platform.AssetMapping
import indigo.shared.platform.RendererConfig
import indigo.shared.platform.TextureRefAndOffset
import indigo.shared.shader.RawShaderCode
import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.Canvas
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

import scala.util.Failure
import scala.util.Success

class Platform(
    parentElementId: String,
    gameConfig: GameConfig,
    assetCollection: AssetCollection,
    globalEventStream: GlobalEventStream,
    dynamicText: DynamicText
) extends PlatformFullScreen {

  val rendererInit: RendererInitialiser =
    new RendererInitialiser(gameConfig.advanced.renderingTechnology, globalEventStream, dynamicText)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _canvas: Canvas = _
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _running: Boolean         = true
  private val _worldEvents: WorldEvents = new WorldEvents

  def initialise(shaders: Set[RawShaderCode]): Outcome[(Renderer, AssetMapping)] =
    for {
      textureAtlas        <- createTextureAtlas(assetCollection)
      loadedTextureAssets <- extractLoadedTextures(textureAtlas)
      assetMapping        <- setupAssetMapping(textureAtlas)
      canvas              <- createCanvas(parentElementId, gameConfig)
      _                   <- listenToWorldEvents(canvas, gameConfig, globalEventStream)
      renderer            <- startRenderer(gameConfig, loadedTextureAssets, canvas, shaders)
      _ = _canvas = canvas
    } yield (renderer, assetMapping)

  def tick(loop: Long => Unit): Unit = {
    if _running then dom.window.requestAnimationFrame(t => loop(t.toLong))
    ()
  }

  def kill(): Unit =
    _running = false
    _worldEvents.kill()
    ()

  def createTextureAtlas(assetCollection: AssetCollection): Outcome[TextureAtlas] =
    Outcome(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height, i.tag)),
        (name: AssetName) => assetCollection.images.find(_.name == name),
        TextureAtlasFunctions.createAtlasData
      )
    )

  def extractLoadedTextures(textureAtlas: TextureAtlas): Outcome[List[LoadedTextureAsset]] =
    Outcome(
      textureAtlas.atlases.toList
        .map { case (atlasId, atlas) => atlas.imageData.map(data => new LoadedTextureAsset(AtlasId(atlasId), data)) }
        .collect { case Some(s) => s }
    )

  def setupAssetMapping(textureAtlas: TextureAtlas): Outcome[AssetMapping] =
    Outcome(
      new AssetMapping(
        mappings = textureAtlas.legend
          .map { case (name, atlasIndex) =>
            name -> TextureRefAndOffset(
              atlasName = atlasIndex.id,
              atlasSize = textureAtlas.atlases
                .get(atlasIndex.id.toString)
                .map(_.size.value)
                .map(i => Vector2(i.toDouble))
                .getOrElse(Vector2.one),
              offset = atlasIndex.offset.toVector,
              size = atlasIndex.size.toVector
            )
          }
      )
    )

  private given CanEqual[Option[Element], Option[Element]] = CanEqual.derived

  def createCanvas(parentElementId: String, gameConfig: GameConfig): Outcome[Canvas] =
    Option(dom.document.getElementById(parentElementId)) match {
      case None =>
        Outcome.raiseError(new Exception(s"""Parent element with ID '$parentElementId' could not be found on page."""))

      case Some(parent) =>
        IndigoLogger.info(s"Attaching to element with ID: '$parentElementId'")
        Outcome(
          rendererInit.createCanvas(
            gameConfig.viewport.width,
            gameConfig.viewport.height,
            parentElementId,
            parent
          )
        )
    }

  def listenToWorldEvents(canvas: Canvas, gameConfig: GameConfig, globalEventStream: GlobalEventStream): Outcome[Unit] =
    Outcome {
      IndigoLogger.info("Starting world events")
      _worldEvents.init(canvas, gameConfig.magnification, gameConfig.advanced.disableContextMenu, globalEventStream)
      GamepadInputCaptureImpl.init()
    }

  def startRenderer(
      gameConfig: GameConfig,
      loadedTextureAssets: List[LoadedTextureAsset],
      canvas: Canvas,
      shaders: Set[RawShaderCode]
  ): Outcome[Renderer] =
    Outcome {
      IndigoLogger.info("Starting renderer")
      rendererInit.setup(
        new RendererConfig(
          renderingTechnology = gameConfig.advanced.renderingTechnology,
          clearColor = gameConfig.clearColor,
          magnification = gameConfig.magnification,
          maxBatchSize = gameConfig.advanced.batchSize,
          antiAliasing = gameConfig.advanced.antiAliasing,
          premultipliedAlpha = gameConfig.advanced.premultipliedAlpha,
          transparentBackground = gameConfig.transparentBackground
        ),
        loadedTextureAssets,
        canvas,
        shaders
      )
    }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def toggleFullScreen(): Unit =
    if (Option(dom.document.fullscreenElement).isEmpty)
      enterFullScreen()
    else
      exitFullScreen()

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def enterFullScreen(): Unit =
    _canvas.requestFullscreen().toFuture.onComplete {
      case Success(()) =>
        globalEventStream.pushGlobalEvent(FullScreenEntered)

      case Failure(_) =>
        globalEventStream.pushGlobalEvent(FullScreenEnterError)
    }

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def exitFullScreen(): Unit =
    dom.document.exitFullscreen().toFuture.onComplete {
      case Success(()) =>
        globalEventStream.pushGlobalEvent(FullScreenExited)

      case Failure(_) =>
        globalEventStream.pushGlobalEvent(FullScreenExitError)
    }
}

trait PlatformFullScreen {
  def toggleFullScreen(): Unit
  def enterFullScreen(): Unit
  def exitFullScreen(): Unit
}
