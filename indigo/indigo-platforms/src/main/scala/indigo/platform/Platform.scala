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
import indigo.facades.worker.SceneProcessor
import indigo.platform.audio.AudioPlayer
import indigo.shared.audio.Volume
import indigo.shared.assets.AssetName
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.FontInfo
import indigo.shared.animation.Animation
import indigo.shared.platform.SceneFrameData
import indigo.facades.worker.WorkerConversions
import indigo.facades.worker.ProcessedSceneData

import scalajs.js.JSConverters._

class Platform(gameConfig: GameConfig, globalEventStream: GlobalEventStream) extends PlatformAPI {

  private val sceneWorker  = new Worker("indigo-scene-worker.js")
  private val renderWorker = new Worker("indigo-render-worker.js")

  private val animationsRegister: AnimationsRegister =
    new AnimationsRegister()
  private val fontRegister: FontRegister =
    new FontRegister()
  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(animationsRegister, fontRegister)

  val audioPlayer: AudioPlayer =
    AudioPlayer.init

  val rendererInit: RendererInitialiser =
    new RendererInitialiser(gameConfig.advanced.renderingTechnology, globalEventStream)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  private var sceneProcessor: SceneProcessor = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  private var _canvas: Canvas = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var assetCollection: AssetCollection = AssetCollection.empty
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var assetMapping: AssetMapping = null
  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var renderer: Renderer = null

  def addAssetsToCollection(newAssets: AssetCollection): Unit =
    assetCollection = assetCollection |+| newAssets

  def giveAssetCollection: AssetCollection =
    assetCollection

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def purgeTextureAtlasCaches(): Unit = {
    sceneWorker.postMessage(
      js.Dynamic.literal(
        "operation" -> "purge"
      )
    )

    if (sceneProcessor != null) {
      sceneProcessor.purgeTextureAtlasCaches()
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def initialise(): Outcome[Unit] = {

    sceneWorker.postMessage(js.Dynamic.literal("operation" -> "echo", "data" -> "Hello, Scene Worker!"))
    sceneWorker.onmessage = (e: js.Any) => {
      val msg = e.asInstanceOf[dom.MessageEvent].data.asInstanceOf[WorkerMessage]

      msg match {
        case m if m._type.isDefined && m._type.get == "message" =>
          println("Scene Worker: " + msg.data.asInstanceOf[String])

        case m if m._type.isDefined && m._type.get == "echo" =>
          println("Scene Worker Echo: " + msg.data.asInstanceOf[String])

        case m if m._type.isDefined && m._type.get == "processed scene" =>
          // Render scene
          renderer.drawScene(msg.data.asInstanceOf[ProcessedSceneData])
      }

    }

    renderWorker.postMessage(js.Dynamic.literal("operation" -> "echo", "data" -> "Hello, Render Worker!"))
    renderWorker.onmessage = (e: js.Any) => {
      val msg = e.asInstanceOf[dom.MessageEvent].data.asInstanceOf[String]
      println("Render worker said: " + msg)
    }

    sceneProcessor = new SceneProcessor(boundaryLocator, animationsRegister, fontRegister)

    for {
      _ <- createCanvas(gameConfig)
      _ <- listenToWorldEvents(_canvas, gameConfig.magnification, globalEventStream)
      _ <- reinitialise()
    } yield ()
  }

  def reinitialise(): Outcome[Unit] =
    for {
      textureAtlas        <- createTextureAtlas(assetCollection)
      loadedTextureAssets <- extractLoadedTextures(textureAtlas)
      am                  <- setupAssetMapping(textureAtlas)
      r                   <- startRenderer(gameConfig, loadedTextureAssets)
    } yield {
      assetMapping = am
      renderer = r
      ()
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

  def createCanvas(gameConfig: GameConfig): Outcome[Unit] =
    Option(dom.document.getElementById("indigo-container")) match {
      case None =>
        Outcome.raiseError(new Exception("""Parent element "indigo-container" could not be found on page."""))

      case Some(parent) =>
        _canvas = rendererInit.createCanvas(
          gameConfig.viewport.width,
          gameConfig.viewport.height,
          parent
        )

        Outcome(())
    }

  def listenToWorldEvents(canvas: Canvas, magnification: Int, globalEventStream: GlobalEventStream): Outcome[Unit] =
    Outcome {
      IndigoLogger.info("Starting world events")
      WorldEvents.init(canvas, magnification, globalEventStream)
      GamepadInputCaptureImpl.init()
    }

  def startRenderer(
      gameConfig: GameConfig,
      loadedTextureAssets: List[LoadedTextureAsset]
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
        _canvas
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
      scene: SceneUpdateFragment
  ): Unit = {

    // Play audio
    audioPlayer.playAudio(scene.audio)

    // worker
    val sceneFrameData =
      SceneFrameData(
        gameTime,
        scene,
        assetMapping,
        renderer.screenWidth.toDouble,
        renderer.screenHeight.toDouble,
        renderer.orthographicProjectionMatrix
      )

    sceneWorker.postMessage(
      js.Dynamic.literal(
        "operation" -> "processScene",
        "data"      -> WorkerConversions.writeSceneFrameData(sceneFrameData)
      )
    )

    // val x =
    //   WorkerConversions.readSceneFrameData(WorkerConversions.writeSceneFrameData(sceneFrameData))

    // // Prepare scene
    // val sceneData = sceneProcessor.processScene(
    //   x.gameTime,
    //   x.scene,
    //   x.assetMapping,
    //   x.screenWidth,
    //   x.screenHeight,
    //   x.orthographicProjectionMatrix
    // )

    // // Render scene
    // renderer.drawScene(sceneData)
  }

  def playSound(assetName: AssetName, volume: Volume): Unit =
    audioPlayer.playSound(assetName, volume)

  def registerAllFonts(fontInfos: Set[FontInfo]): Unit = {
    sceneWorker.postMessage(
      js.Dynamic.literal(
        "operation" -> "addFonts",
        "data"      -> fontInfos.toJSArray.map(WorkerConversions.writeFontInfo)
      )
    )

    fontRegister.registerAll(fontInfos)
  }

  def registerAllAnimations(animations: Set[Animation]): Unit = {
    sceneWorker.postMessage(
      js.Dynamic.literal(
        "operation" -> "addAnimations",
        "data"      -> animations.toJSArray.map(WorkerConversions.writeAnimation)
      )
    )

    animationsRegister.registerAll(animations)
  }
}

trait PlatformAPI {
  def toggleFullScreen(): Unit
  def enterFullScreen(): Unit
  def exitFullScreen(): Unit
  def playSound(assetName: AssetName, volume: Volume): Unit
  def purgeTextureAtlasCaches(): Unit
}

trait WorkerMessage extends js.Object {
  val _type: js.UndefOr[String]
  val data: js.Object
}
