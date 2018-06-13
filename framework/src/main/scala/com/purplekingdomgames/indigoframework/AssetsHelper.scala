package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.assets.AssetManager
import com.purplekingdomgames.indigo.runtime.Logger
import com.purplekingdomgames.shared.{AssetList, AssetType, TextAsset}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AssetsHelper {

  def assets: Set[AssetType] = Set()

  def assetsAsync: Future[Set[AssetType]] =
    AssetManager
      .loadTextAsset(TextAsset("assetsList", "assets/assets.json"))
      .map { p =>
        fromJson(p.contents).map(_.toSet) match {
          case Some(as) =>
            as

          case None =>
            Logger.info("No assets loaded")
            Set[AssetType]()
        }
      }

  def fromJson(json: String): Option[AssetList] =
    AssetList.fromJson(json) match {
      case Right(al) =>
        Some(al)

      case Left(e) =>
        Logger.info(e)
        None
    }

}