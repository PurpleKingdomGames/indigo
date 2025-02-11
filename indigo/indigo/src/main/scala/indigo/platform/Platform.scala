package indigo.platform

import indigo.platform.assets.AssetCollection
import indigo.platform.assets.AtlasId
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
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.annotation.nowarn
import scala.util.Failure
import scala.util.Success

class Platform(
    parentElement: Element,
    gameConfig: GameConfig,
    globalEventStream: GlobalEventStream
) extends PlatformFullScreen {

  val rendererInit: RendererInitialiser =
    new RendererInitialiser(gameConfig.advanced.renderingTechnology, globalEventStream)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  private var _canvas: Canvas = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var _running: Boolean         = true
  private val _worldEvents: WorldEvents = new WorldEvents

  def initialise(
      firstRun: Boolean,
      shaders: Set[RawShaderCode],
      assetCollection: AssetCollection
  ): Outcome[(Renderer, AssetMapping)] =
    for {
      textureAtlas        <- createTextureAtlas(assetCollection)
      loadedTextureAssets <- extractLoadedTextures(textureAtlas)
      assetMapping        <- setupAssetMapping(textureAtlas)
      canvas              <- createCanvas(firstRun, parentElement, gameConfig)
      _                   <- listenToWorldEvents(firstRun, canvas, gameConfig, globalEventStream)
      renderer            <- startRenderer(gameConfig, loadedTextureAssets, canvas, shaders)
      _ = _canvas = canvas
    } yield (renderer, assetMapping)

  @nowarn("msg=discarded")
  def tick(loop: Double => Unit): Unit =
    if _running then dom.window.requestAnimationFrame(loop)
    ()

  @nowarn("msg=discarded")
  def delay(amount: Double, thunk: () => Unit): Unit =
    dom.window.setTimeout(thunk, amount)

  def kill(): Unit =
    _running = false
    _worldEvents.kill()
    GamepadInputCaptureImpl.kill()
    ()

  def createTextureAtlas(assetCollection: AssetCollection): Outcome[TextureAtlas] =
    Outcome(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height, i.tag)).toList,
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

  def createCanvas(firstRun: Boolean, parentElement: Element, gameConfig: GameConfig): Outcome[Canvas] =
    if firstRun then
      Outcome(
        rendererInit.createCanvas(
          gameConfig.viewport.width,
          gameConfig.viewport.height,
          parentElement
        )
      )
    else Outcome(_canvas)

  def listenToWorldEvents(
      firstRun: Boolean,
      canvas: Canvas,
      gameConfig: GameConfig,
      globalEventStream: GlobalEventStream
  ): Outcome[Unit] =
    Outcome {
      if firstRun then
        IndigoLogger.info("Starting world events")
        _worldEvents.init(
          canvas,
          gameConfig.resizePolicy,
          gameConfig.magnification,
          gameConfig.advanced.disableContextMenu,
          globalEventStream,
          gameConfig.advanced.clickTime.toLong
        )
        GamepadInputCaptureImpl.init()
      else IndigoLogger.info("Re-using existing world events")
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
