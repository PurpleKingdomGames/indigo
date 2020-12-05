package indigo.platform

import indigo.platform.renderer.Renderer
import indigo.platform.events.GlobalEventStream
import indigo.shared.config.GameConfig
import indigo.shared.IndigoLogger
import indigo.shared.datatypes.Vector2
import indigo.platform.renderer.RendererInitialiser
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.shared.platform.RendererConfig
import indigo.shared.platform.AssetMapping
import indigo.shared.platform.TextureRefAndOffset
import indigo.platform.events.WorldEvents
import indigo.platform.input.GamepadInputCaptureImpl
import indigo.platform.assets.AssetCollection
import indigo.platform.assets.TextureAtlas
import indigo.platform.assets.TextureAtlasFunctions
import indigo.platform.assets.ImageRef

import indigo.shared.EqualTo._

import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import indigo.facades.FullScreenElement
import indigo.shared.events.FullScreenEntered
import indigo.shared.events.FullScreenEnterError
import indigo.shared.events.FullScreenExited
import indigo.shared.events.FullScreenExitError

class Platform(
    gameConfig: GameConfig,
    assetCollection: AssetCollection,
    globalEventStream: GlobalEventStream
) extends PlatformFullScreen {

  val rendererInit: RendererInitialiser =
    new RendererInitialiser(gameConfig.advanced.renderingTechnology, globalEventStream)

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  private var _canvas: Canvas = null

  def initialise(): Try[(Renderer, AssetMapping)] =
    for {
      textureAtlas        <- createTextureAtlas(assetCollection)
      loadedTextureAssets <- extractLoadedTextures(textureAtlas)
      assetMapping        <- setupAssetMapping(textureAtlas)
      canvas              <- createCanvas(gameConfig)
      _                   <- listenToWorldEvents(canvas, gameConfig.magnification, globalEventStream)
      renderer            <- startRenderer(gameConfig, loadedTextureAssets, canvas)
    } yield {
      _canvas = canvas

      (renderer, assetMapping)
    }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def tick(loop: Long => Unit): Unit = {
    dom.window.requestAnimationFrame(t => loop(t.toLong))
    ()
  }

  def createTextureAtlas(assetCollection: AssetCollection): Try[TextureAtlas] =
    Success(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height, i.tag.map(_.value))),
        (name: String) => assetCollection.images.find(_.name.value === name),
        TextureAtlasFunctions.createAtlasData
      )
    )

  def extractLoadedTextures(textureAtlas: TextureAtlas): Try[List[LoadedTextureAsset]] =
    Success(
      textureAtlas.atlases.toList
        .map(a => a._2.imageData.map(data => new LoadedTextureAsset(a._1.id, data)))
        .collect { case Some(s) => s }
    )

  def setupAssetMapping(textureAtlas: TextureAtlas): Try[AssetMapping] =
    Success(
      new AssetMapping(
        mappings = textureAtlas.legend
          .map { p =>
            p._1 -> new TextureRefAndOffset(
              atlasName = p._2.id.id,
              atlasSize = textureAtlas.atlases.get(p._2.id).map(_.size.value).map(i => Vector2(i.toDouble)).getOrElse(Vector2.one),
              offset = p._2.offset
            )
          }
      )
    )

  def createCanvas(gameConfig: GameConfig): Try[Canvas] =
    Option(dom.document.getElementById("indigo-container")) match {
      case None =>
        Failure[Canvas](new Exception("""Parent element "indigo-container" could not be found on page."""))

      case Some(parent) =>
        Success(
          rendererInit.createCanvas(
            gameConfig.viewport.width,
            gameConfig.viewport.height,
            parent
          )
        )
    }

  def listenToWorldEvents(canvas: Canvas, magnification: Int, globalEventStream: GlobalEventStream): Try[Unit] =
    Success {
      IndigoLogger.info("Starting world events")
      WorldEvents.init(canvas, magnification, globalEventStream)
      GamepadInputCaptureImpl.init()
    }

  def startRenderer(
      gameConfig: GameConfig,
      loadedTextureAssets: List[LoadedTextureAsset],
      canvas: Canvas
  ): Try[Renderer] =
    Success {
      IndigoLogger.info("Starting renderer")
      rendererInit.setup(
        new RendererConfig(
          renderingTechnology = gameConfig.advanced.renderingTechnology,
          clearColor = gameConfig.clearColor,
          magnification = gameConfig.magnification,
          maxBatchSize = gameConfig.advanced.batchSize,
          antiAliasing = gameConfig.advanced.antiAliasing
        ),
        loadedTextureAssets,
        canvas
      )
    }

  implicit private val ec: scala.concurrent.ExecutionContext = scalajs.concurrent.JSExecutionContext.queue

  def toggleFullScreen(): Unit =
    if (Option(dom.document.asInstanceOf[FullScreenElement].fullscreenElement).isEmpty)
      enterFullScreen()
    else
      exitFullScreen()

  def enterFullScreen(): Unit =
    _canvas.asInstanceOf[FullScreenElement].requestFullscreen().toFuture.onComplete {
      case Success(()) =>
        globalEventStream.pushGlobalEvent(FullScreenEntered)

      case Failure(_) =>
        globalEventStream.pushGlobalEvent(FullScreenEnterError)
    }

  def exitFullScreen(): Unit =
    dom.document.asInstanceOf[FullScreenElement].exitFullscreen().toFuture.onComplete {
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
