package indigo.platform.assets

import indigo.shared.IndigoLogger
import indigo.shared.assets.AssetType
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.{Try, Success, Failure}
import indigo.shared.platform.GlobalEventStream
import indigo.shared.datatypes.BindingKey

object AssetLoader {

  def backgroundLoadAssets(rebuildGameLoop: AssetCollection => Unit, globalEventStream: GlobalEventStream, assets: Set[AssetType], key: BindingKey, makeAvailable: Boolean): Unit = {
    val assetList: List[AssetType] =
      assets.toList.flatMap(_.toList)

    IndigoLogger.info(s"Background loading ${assetList.length.toString()} assets")
    println(globalEventStream) // Nonsense to let the code compile...
    println(key)               // Nonsense to let the code compile...
    println(makeAvailable)     // Nonsense to let the code compile...

    if (1 == 2) {
      rebuildGameLoop(AssetCollection.empty)
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

  // def onLoadFuture(image: HTMLImageElement): Future[HTMLImageElement] =
  //   if (image.complete) Future.successful(image)
  //   else {
  //     val p = Promise[HTMLImageElement]()
  //     image.onload = { _: Event =>
  //       p.success(image)
  //     }
  //     p.future
  //   }

  // @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def loadImageAsset(imageAsset: AssetType.Image): Future[LoadedImageAsset] = {
    IndigoLogger.info(s"[Image] Loading ${imageAsset.path.value}")

    // val image: html.Image = dom.document.createElement("img").asInstanceOf[html.Image]
    // image.src = imageAsset.path

    // onLoadFuture(image).map { i =>
    //   IndigoLogger.info(s"[Image] Success ${imageAsset.path}")
    //   new LoadedImageAsset(AssetName(imageAsset.name), i)
    // }
    Future(new LoadedImageAsset(imageAsset.name, "", None))
  }

  val loadTextAssets: List[AssetType.Text] => Future[List[LoadedTextAsset]] =
    textAssets => Future.sequence(textAssets.map(loadTextAsset))

  def loadTextAsset(textAsset: AssetType.Text): Future[LoadedTextAsset] = {
    IndigoLogger.info(s"[Text] Loading ${textAsset.path.value}")

    Try {
      val buffer = Source.fromFile(textAsset.path.value)
      val text   = buffer.getLines().mkString("\n")
      buffer.close()
      text
    } match {
      case Success(value) =>
        IndigoLogger.info(s"[Text] Success ${textAsset.path.value}")
        Future(new LoadedTextAsset(textAsset.name, value))

      case Failure(exception) =>
        IndigoLogger.info(s"[Text] Failure ${exception.getMessage()}")
        Future.failed[LoadedTextAsset](exception)
    }
  }

  val loadAudioAssets: List[AssetType.Audio] => Future[List[LoadedAudioAsset]] =
    audioAssets => Future.sequence(audioAssets.map(loadAudioAsset))

  // @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def loadAudioAsset(audioAsset: AssetType.Audio): Future[LoadedAudioAsset] = {
    IndigoLogger.info(s"[Audio] Loading ${audioAsset.path.value}")

    // Ajax.get(audioAsset.path, responseType = "arraybuffer").flatMap { xhr =>
    //   IndigoLogger.info(s"[Audio] Success ${audioAsset.path}")
    //   val context = new AudioContext()
    //   val p = context.decodeAudioData(
    //     xhr.response.asInstanceOf[ArrayBuffer],
    //     (audioBuffer: AudioBuffer) => audioBuffer,
    //     () => IndigoLogger.info("Error decoding audio from: " + audioAsset.path)
    //   )

    //   p.toFuture.map(audioBuffer => new LoadedAudioAsset(AssetName(audioAsset.name), audioBuffer))

    // }
    Future(new LoadedAudioAsset(audioAsset.name, ""))
  }

}
