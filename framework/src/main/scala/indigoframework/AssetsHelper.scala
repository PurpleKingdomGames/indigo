package indigoframework

import indigo.platform.assets.AssetLoader
import indigo.shared.IndigoLogger
import indigo.shared.{AssetList, AssetType}
import indigo.json._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future

object AssetsHelper {

  def assets: Set[AssetType] = Set()

  def assetsAsync: Future[Set[AssetType]] =
    AssetLoader
      .loadTextAsset(AssetType.Text("assetsList", "assets/assets.json"))
      .map { p =>
        fromJson(p.data).map(_.toSet) match {
          case Some(as) =>
            as

          case None =>
            IndigoLogger.info("No assets loaded")
            Set[AssetType]()
        }
      }

  def fromJson(json: String): Option[AssetList] =
    assetListFromJson(json) match {
      case Right(al) =>
        Some(al)

      case Left(e) =>
        IndigoLogger.info(e)
        None
    }

}
