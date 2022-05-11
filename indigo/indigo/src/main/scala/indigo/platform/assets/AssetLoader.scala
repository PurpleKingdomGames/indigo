package indigo.platform.assets

import indigo.facades.FontFace
import indigo.platform.audio.AudioPlayer
import indigo.platform.events.GlobalEventStream
import indigo.shared.IndigoLogger
import indigo.shared.assets.AssetName
import indigo.shared.assets.AssetType
import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
import indigo.shared.events.AssetEvent
import org.scalajs.dom
import org.scalajs.dom.HTMLImageElement
import org.scalajs.dom._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.typedarray.ArrayBuffer
import scala.util.Failure
import scala.util.Success

object AssetLoader {

  def backgroundLoadAssets(
      rebuildGameLoop: AssetCollection => Unit,
      globalEventStream: GlobalEventStream,
      assets: Set[AssetType],
      key: BindingKey,
      makeAvailable: Boolean
  ): Unit = {
    val assetBatch: Batch[AssetType] =
      Batch.fromSet(assets).flatMap(_.toBatch)

    IndigoLogger.info(s"Background loading ${assetBatch.size.toString()} assets with key: $key")

    loadAssets(assets)
      .onComplete {
        case Success(ac) if makeAvailable =>
          rebuildGameLoop(ac)
          globalEventStream.pushGlobalEvent(AssetEvent.AssetBatchLoaded(key, true))

        case Success(_) =>
          globalEventStream.pushGlobalEvent(AssetEvent.AssetBatchLoaded(key, false))

        case Failure(e) =>
          globalEventStream.pushGlobalEvent(AssetEvent.AssetBatchLoadError(key, e.getMessage()))
      }

    ()
  }

  def loadAssets(assets: Set[AssetType]): Future[AssetCollection] = {
    val assetBatch: Batch[AssetType] =
      Batch.fromSet(assets).flatMap(_.toBatch)

    IndigoLogger.info(s"Loading ${assetBatch.size.toString()} assets")

    for {
      t <- loadTextAssets(filterOutTextAssets(assetBatch))
      i <- loadImageAssets(filterOutImageAssets(assetBatch))
      a <- loadAudioAssets(filterOutAudioAssets(assetBatch))
      f <- loadFontAssets(filterOutFontAssets(assetBatch))
    } yield new AssetCollection(i, t, a, f)
  }

  def filterOutTextAssets(l: Batch[AssetType]): Batch[AssetType.Text] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Text => Batch(t)
        case _                 => Batch.Empty
      }
    }

  def filterOutImageAssets(l: Batch[AssetType]): Batch[AssetType.Image] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Image => Batch(t)
        case _                  => Batch.Empty
      }
    }

  def filterOutAudioAssets(l: Batch[AssetType]): Batch[AssetType.Audio] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Audio => Batch(t)
        case _                  => Batch.Empty
      }
    }

  def filterOutFontAssets(l: Batch[AssetType]): Batch[AssetType.Font] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Font => Batch(t)
        case _                 => Batch.Empty
      }
    }

  val loadImageAssets: Batch[AssetType.Image] => Future[Batch[LoadedImageAsset]] =
    imageAssets => Future.sequence(imageAssets.toList.map(loadImageAsset)).map(Batch.fromList)

  def onLoadImageFuture(image: HTMLImageElement): Future[HTMLImageElement] =
    if (image.complete) Future.successful(image)
    else {
      val p = Promise[HTMLImageElement]()
      image.onload = { (_: Event) =>
        p.success(image)
      }
      image.addEventListener(
        "error",
        (_: Event) => p.failure(new Exception("Image load error")),
        false
      )
      p.future
    }

  // Images

  def loadImageAsset(imageAsset: AssetType.Image): Future[LoadedImageAsset] = {
    IndigoLogger.info(s"[Image] Loading ${imageAsset.path}")

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path.toString

    onLoadImageFuture(image).map { i =>
      IndigoLogger.info(s"[Image] Success ${imageAsset.path}")
      new LoadedImageAsset(imageAsset.name, i, imageAsset.tag)
    }
  }

  // Text

  val loadTextAssets: Batch[AssetType.Text] => Future[Batch[LoadedTextAsset]] =
    textAssets => Future.sequence(textAssets.toList.map(loadTextAsset)).map(Batch.fromList)

  def loadTextAsset(textAsset: AssetType.Text): Future[LoadedTextAsset] = {
    IndigoLogger.info(s"[Text] Loading ${textAsset.path}")

    fetch(textAsset.path.toString).toFuture.flatMap { response =>
      IndigoLogger.info(s"[Text] Success ${textAsset.path}")
      response.text().toFuture.map(txt => new LoadedTextAsset(textAsset.name, txt))
    }
  }

  // Audio

  val loadAudioAssets: Batch[AssetType.Audio] => Future[Batch[LoadedAudioAsset]] =
    audioAssets => Future.sequence(audioAssets.toList.map(loadAudioAsset)).map(Batch.fromList)

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def loadAudioAsset(audioAsset: AssetType.Audio): Future[LoadedAudioAsset] = {
    IndigoLogger.info(s"[Audio] Loading ${audioAsset.path}")

    fetch(audioAsset.path.toString).toFuture.flatMap { response =>
      IndigoLogger.info(s"[Audio] Success ${audioAsset.path}")
      val context = AudioPlayer.giveAudioContext()

      response.arrayBuffer().toFuture.flatMap { ab =>
        context
          .decodeAudioData(
            ab,
            (audioBuffer: AudioBuffer) => audioBuffer,
            () => IndigoLogger.info("Error decoding audio from: " + audioAsset.path)
          )
          .toFuture
          .map(audioBuffer => new LoadedAudioAsset(audioAsset.name, audioBuffer))
      }
    }
  }

  // Fonts

  val loadFontAssets: Batch[AssetType.Font] => Future[Batch[LoadedFontAsset]] =
    fontAssets => Future.sequence(fontAssets.toList.map(loadFontAsset)).map(Batch.fromList)

  // @SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
  def loadFontAsset(fontAsset: AssetType.Font): Future[LoadedFontAsset] =
    IndigoLogger.info(s"[Font] Loading ${fontAsset.path}")

    val font = new FontFace(fontAsset.name.toString, s"url(${fontAsset.path.toString})")

    font.load().toFuture.map { fontFace =>
      IndigoLogger.info(s"[Font] Success ${fontAsset.path}")

      // add font to document
      js.Dynamic.global.document.fonts.add(font)
      // enable font with CSS class
      js.Dynamic.global.document.body.classBatch.add("indigo-fonts-loaded")

      LoadedFontAsset(AssetName(fontFace.family))
    }
}
