package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.assets.AssetManager
import com.purplekingdomgames.indigo.runtime.IndigoLogger
import com.purplekingdomgames.shared.{AssetList, AssetType}

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
    AssetList.fromJson(json) match {
      case Right(al) =>
        Some(al)

      case Left(e) =>
        IndigoLogger.info(e)
        None
    }

}
