package indigo.gameengine

import indigo.shared.IndigoLogger
import indigo.shared.AssetType
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.HTMLImageElement
import org.scalajs.dom.{html, _}

import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.typedarray.ArrayBuffer

import indigo.shared.EqualTo._

object AssetManager {

  private def filterOutTextAssets(l: List[AssetType]): List[AssetType.Text] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Text => List(t)
        case _                 => Nil
      }
    }

  private def filterOutImageAssets(l: List[AssetType]): List[AssetType.Image] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Image => List(t)
        case _                  => Nil
      }
    }

  private def filterOutAudioAssets(l: List[AssetType]): List[AssetType.Audio] =
    l.flatMap { at =>
      at match {
        case t: AssetType.Audio => List(t)
        case _                  => Nil
      }
    }

  def loadAssets(assets: Set[AssetType]): Future[AssetCollection] = {
    IndigoLogger.info(s"Loading ${assets.toList.length} assets")

    for {
      t <- loadTextAssets(filterOutTextAssets(assets.toList))
      i <- loadImageAssets(filterOutImageAssets(assets.toList))
      a <- loadAudioAssets(filterOutAudioAssets(assets.toList))
    } yield AssetCollection(i, t, a)
  }

  def findByName(assetCollection: AssetCollection): String => Option[LoadedImageAsset] =
    name => assetCollection.images.find(p => p.name === name)

  private val loadImageAssets: List[AssetType.Image] => Future[List[LoadedImageAsset]] = imageAssets => Future.sequence(imageAssets.map(loadImageAsset))

  private def onLoadFuture(image: HTMLImageElement): Future[HTMLImageElement] =
    if (image.complete) Future.successful(image)
    else {
      val p = Promise[HTMLImageElement]()
      image.onload = { _: Event =>
        p.success(image)
      }
      p.future
    }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  private def loadImageAsset(imageAsset: AssetType.Image): Future[LoadedImageAsset] = {
    IndigoLogger.info(s"[Image] Loading ${imageAsset.path}")

    val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    image.src = imageAsset.path

    onLoadFuture(image).map { i =>
      IndigoLogger.info(s"[Image] Success ${imageAsset.path}")
      LoadedImageAsset(imageAsset.name, i)
    }
  }

  private val loadTextAssets: List[AssetType.Text] => Future[List[LoadedTextAsset]] = textAssets => Future.sequence(textAssets.map(loadTextAsset))

  def loadTextAsset(textAsset: AssetType.Text): Future[LoadedTextAsset] = {
    IndigoLogger.info(s"[Text] Loading ${textAsset.path}")

    Ajax.get(textAsset.path, responseType = "text").map { xhr =>
      IndigoLogger.info(s"[Text] Success ${textAsset.path}")
      LoadedTextAsset(textAsset.name, xhr.responseText)
    }
  }

  private val loadAudioAssets: List[AssetType.Audio] => Future[List[LoadedAudioAsset]] = audioAssets => Future.sequence(audioAssets.map(loadAudioAsset))

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def loadAudioAsset(audioAsset: AssetType.Audio): Future[LoadedAudioAsset] = {
    IndigoLogger.info(s"[Audio] Loading ${audioAsset.path}")

    Ajax.get(audioAsset.path, responseType = "arraybuffer").flatMap { xhr =>
      IndigoLogger.info(s"[Audio] Success ${audioAsset.path}")
      val context = new AudioContext()
      val p = context.decodeAudioData(
        xhr.response.asInstanceOf[ArrayBuffer],
        (audioBuffer: AudioBuffer) => audioBuffer,
        () => IndigoLogger.info("Error decoding audio from: " + audioAsset.path)
      )

      p.toFuture.map(audioBuffer => LoadedAudioAsset(audioAsset.name, audioBuffer))
    }
  }

}

final case class LoadedTextAsset(name: String, contents: String)
final case class LoadedImageAsset(name: String, data: html.Image)
final case class LoadedAudioAsset(name: String, data: dom.AudioBuffer)

final case class AssetCollection(images: List[LoadedImageAsset], texts: List[LoadedTextAsset], sounds: List[LoadedAudioAsset])
