package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.assets.{AssetManager, AssetType, ImageAsset, TextAsset}
import com.purplekingdomgames.indigo.util.Logger
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AssetsHelper {

  def assets: Set[AssetType] = Set()

  def assetsAsync: Future[Set[AssetType]] =
    AssetManager
      .loadTextAsset(TextAsset("assetsList", "assets/assets.json"))
      .map { p =>
        fromJson(p.contents).map(_.toSet) match {
          case Some(as) => as
          case None =>
            Logger.info("No assets loaded")
            Set[AssetType]()
        }
      }

  def fromJson(json: String): Option[AssetList] =
    decode[AssetList](json) match {
      case Right(al) => Some(al)
      case Left(e) =>
        Logger.info("Failed to deserialise json into AssetList: " + e.getMessage)
        None
    }

}

case class AssetList(images: List[SimpleAssetType], texts: List[SimpleAssetType]) {
  def toSet: Set[AssetType] = texts.map(_.toTextAsset).toSet ++ images.map(_.toImageAsset).toSet
}

//upickle (which we should deprecate) won't deserialise ADT's without faff, so this is a cheap workaround.
case class SimpleAssetType(name: String, path: String) {
  def toTextAsset: TextAsset = TextAsset(name, path)
  def toImageAsset: ImageAsset = ImageAsset(name, path)
}