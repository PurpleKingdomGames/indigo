package indigo.platform

import indigo.platform.renderer.Renderer
import indigo.platform.events.GlobalEventStream
import indigo.shared.config.GameConfig
import indigo.shared.IndigoLogger
import indigo.shared.Outcome
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

import scala.scalajs.js
import org.scalajs.dom.raw.Worker
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import scala.util.Success
import scala.util.Failure
import indigo.facades.FullScreenElement
import indigo.shared.events.FullScreenEntered
import indigo.shared.events.FullScreenEnterError
import indigo.shared.events.FullScreenExited
import indigo.shared.events.FullScreenExitError
import indigo.shared.time.GameTime
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.datatypes.mutable.CheapMatrix4

import indigo.shared.platform.SceneProcessor
import indigo.platform.audio.AudioPlayer

class Platform(
    gameConfig: GameConfig,
    assetCollection: AssetCollection,
    globalEventStream: GlobalEventStream
) extends PlatformFullScreen {

  val rendererInit: RendererInitialiser =
    new RendererInitialiser(gameConfig.advanced.renderingTechnology, globalEventStream)

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.null",
      "scalafix:DisableSyntax.var"
    )
  )
  private var _canvas: Canvas = null

  @SuppressWarnings(
    Array(
      "scalafix:DisableSyntax.asInstanceOf"
    )
  )
  def initialise(): Outcome[(Renderer, AssetMapping)] = {

    val sceneWorker = new Worker("indigo-scene-worker.js")
    sceneWorker.postMessage(js.Dynamic.literal("operation" -> "echo", "data" -> "Hello, Scene Worker!"))

    val renderWorker = new Worker("indigo-render-worker.js")
    renderWorker.postMessage(js.Dynamic.literal("operation" -> "echo", "data" -> "Hello, Render Worker!"))

    sceneWorker.onmessage = (e: js.Any) => {
      val msg = e.asInstanceOf[dom.MessageEvent].data.asInstanceOf[String]
      println("Scene worker said: " + msg)
    }

    renderWorker.onmessage = (e: js.Any) => {
      val msg = e.asInstanceOf[dom.MessageEvent].data.asInstanceOf[String]
      println("Render worker said: " + msg)
    }

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
  }

  def tick(loop: Long => Unit): Unit = {
    dom.window.requestAnimationFrame(t => loop(t.toLong))
    ()
  }

  def createTextureAtlas(assetCollection: AssetCollection): Outcome[TextureAtlas] =
    Outcome(
      TextureAtlas.create(
        assetCollection.images.map(i => ImageRef(i.name, i.data.width, i.data.height, i.tag.map(_.value))),
        (name: String) => assetCollection.images.find(_.name.value == name),
        TextureAtlasFunctions.createAtlasData
      )
    )

  def extractLoadedTextures(textureAtlas: TextureAtlas): Outcome[List[LoadedTextureAsset]] =
    Outcome(
      textureAtlas.atlases.toList
        .map(a => a._2.imageData.map(data => new LoadedTextureAsset(a._1.id, data)))
        .collect { case Some(s) => s }
    )

  def setupAssetMapping(textureAtlas: TextureAtlas): Outcome[AssetMapping] =
    Outcome(
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

  def createCanvas(gameConfig: GameConfig): Outcome[Canvas] =
    Option(dom.document.getElementById("indigo-container")) match {
      case None =>
        Outcome.raiseError(new Exception("""Parent element "indigo-container" could not be found on page."""))

      case Some(parent) =>
        Outcome(
          rendererInit.createCanvas(
            gameConfig.viewport.width,
            gameConfig.viewport.height,
            parent
          )
        )
    }

  def listenToWorldEvents(canvas: Canvas, magnification: Int, globalEventStream: GlobalEventStream): Outcome[Unit] =
    Outcome {
      IndigoLogger.info("Starting world events")
      WorldEvents.init(canvas, magnification, globalEventStream)
      GamepadInputCaptureImpl.init()
    }

  def startRenderer(
      gameConfig: GameConfig,
      loadedTextureAssets: List[LoadedTextureAsset],
      canvas: Canvas
  ): Outcome[Renderer] =
    Outcome {
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

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def toggleFullScreen(): Unit =
    if (Option(dom.document.asInstanceOf[FullScreenElement].fullscreenElement).isEmpty)
      enterFullScreen()
    else
      exitFullScreen()

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def enterFullScreen(): Unit =
    _canvas.asInstanceOf[FullScreenElement].requestFullscreen().toFuture.onComplete {
      case Success(()) =>
        globalEventStream.pushGlobalEvent(FullScreenEntered)

      case Failure(_) =>
        globalEventStream.pushGlobalEvent(FullScreenEnterError)
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def exitFullScreen(): Unit =
    dom.document.asInstanceOf[FullScreenElement].exitFullscreen().toFuture.onComplete {
      case Success(()) =>
        globalEventStream.pushGlobalEvent(FullScreenExited)

      case Failure(_) =>
        globalEventStream.pushGlobalEvent(FullScreenExitError)
    }

  def presentScene(
      gameTime: GameTime,
      scene: SceneUpdateFragment,
      assetMapping: AssetMapping,
      screenWidth: Double,
      screenHeight: Double,
      orthographicProjectionMatrix: CheapMatrix4,
      audioPlayer: AudioPlayer,
      renderer: Renderer,
      sceneProcessor: SceneProcessor
  ): Unit = {

    // Play audio
    audioPlayer.playAudio(scene.audio)

    // Prepare scene
    val sceneData = sceneProcessor.processScene(
      gameTime,
      scene,
      assetMapping,
      screenWidth,
      screenHeight,
      orthographicProjectionMatrix
    )

    // Render scene
    renderer.drawScene(sceneData)
  }
}

trait PlatformFullScreen {
  def toggleFullScreen(): Unit
  def enterFullScreen(): Unit
  def exitFullScreen(): Unit
}
