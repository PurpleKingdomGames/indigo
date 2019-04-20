package indigoframework

import indigo.gameengine.AssetManager
import indigo.shared.IndigoLogger
import indigo.shared.{AssetList, AssetType}
import indigo.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AssetsHelper {

  def assets: Set[AssetType] = Set()

  def assetsAsync: Future[Set[AssetType]] =
    AssetManager
      .loadTextAsset(AssetType.Text("assetsList", "assets/assets.json"))
      .map { p =>
        fromJson(p.contents).map(_.toSet) match {
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
