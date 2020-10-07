package indigo.platform.assets

import indigo.shared.IndigoLogger
import indigo.shared.assets.AssetType
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLImageElement
import org.scalajs.dom.{html, _}

import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.typedarray.ArrayBuffer
import indigo.platform.events.GlobalEventStream
import indigo.shared.events.AssetEvent
import indigo.shared.datatypes.BindingKey
import scala.util.Success
import scala.util.Failure
import indigo.platform.audio.AudioPlayer

object AssetLoader {

  def backgroundLoadAssets(rebuildGameLoop: AssetCollection => Unit, globalEventStream: GlobalEventStream, assets: Set[AssetType], key: BindingKey, makeAvailable: Boolean): Unit = {
    val assetList: List[AssetType] =
      assets.toList.flatMap(_.toList)

    IndigoLogger.info(s"Background loading ${assetList.length.toString()} assets with key: ${key.value}")

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
    val assetList: List[AssetType] =
      assets.toList.flatMap(_.toList)

    IndigoLogger.info(s"Loading ${assetList.length.toString()} assets")

    for {
      t <- loadTextAssets(filterOutTextAssets(assetList))
      i <- loadImageAssets(filterOutImageAssets(assetList))
      a <- loadAudioAssets(filterOutAudioAssets(assetList))
    } yield new AssetCollection(i, t, a)
  }

  def filterOutTextAssets(l: List[AssetType]): List[AssetType.Text] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Text => List(t)
        case _                 => Nil
      }
    }

  def filterOutImageAssets(l: List[AssetType]): List[AssetType.Image] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Image => List(t)
        case _                  => Nil
      }
    }

  def filterOutAudioAssets(l: List[AssetType]): List[AssetType.Audio] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Audio => List(t)
        case _                  => Nil
      }
    }

  val loadImageAssets: List[AssetType.Image] => Future[List[LoadedImageAsset]] =
    imageAssets => Future.sequence(imageAssets.map(loadImageAsset))

  def onLoadFuture(image: HTMLImageElement): Future[HTMLImageElement] =
    if (image.complete) Future.successful(image)
    else {
      val p = Promise[HTMLImageElement]()
      image.onload = { (_: Event) =>
        p.success(image)
      }
      image.addEventListener(
        "error",
        { (_: Event) =>
          p.failure(new Exception("Image load error"))
        },
        false
      )
      p.future
    }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def loadImageAsset(imageAsset: AssetType.Image): Future[LoadedImageAsset] = {
    IndigoLogger.info(s"[Image] Loading ${imageAsset.path.value}")

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path.value

    onLoadFuture(image).map { i =>
      IndigoLogger.info(s"[Image] Success ${imageAsset.path.value}")
      new LoadedImageAsset(imageAsset.name, i, imageAsset.tag)
    }
  }

  val loadTextAssets: List[AssetType.Text] => Future[List[LoadedTextAsset]] =
    textAssets => Future.sequence(textAssets.map(loadTextAsset))

  def loadTextAsset(textAsset: AssetType.Text): Future[LoadedTextAsset] = {
    IndigoLogger.info(s"[Text] Loading ${textAsset.path.value}")

    Ajax.get(textAsset.path.value, responseType = "text").map { xhr =>
      IndigoLogger.info(s"[Text] Success ${textAsset.path.value}")
      new LoadedTextAsset(textAsset.name, xhr.responseText)
    }
  }

  val loadAudioAssets: List[AssetType.Audio] => Future[List[LoadedAudioAsset]] =
    audioAssets => Future.sequence(audioAssets.map(loadAudioAsset))

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def loadAudioAsset(audioAsset: AssetType.Audio): Future[LoadedAudioAsset] = {
    IndigoLogger.info(s"[Audio] Loading ${audioAsset.path.value}")

    Ajax.get(audioAsset.path.value, responseType = "arraybuffer").flatMap { xhr =>
      IndigoLogger.info(s"[Audio] Success ${audioAsset.path.value}")
      val context = AudioPlayer.giveAudioContext()

      val p = context.decodeAudioData(
        xhr.response.asInstanceOf[ArrayBuffer],
        (audioBuffer: AudioBuffer) => audioBuffer,
        () => IndigoLogger.info("Error decoding audio from: " + audioAsset.path.value)
      )

      p.toFuture.map(audioBuffer => new LoadedAudioAsset(audioAsset.name, audioBuffer))
    }
  }

}
