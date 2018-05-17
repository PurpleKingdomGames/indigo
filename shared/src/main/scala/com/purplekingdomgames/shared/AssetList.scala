package com.purplekingdomgames.shared

import io.circe.generic.auto._
import io.circe.parser._

case class AssetList(images: List[SimpleAssetType], texts: List[SimpleAssetType]) {
  def toSet: Set[AssetType] = texts.map(_.toTextAsset).toSet ++ images.map(_.toImageAsset).toSet

  def withImage(name: String, path: String): AssetList =
    this.copy(images = SimpleAssetType(name, path) :: images)

  def withText(name: String, path: String): AssetList =
    this.copy(texts = SimpleAssetType(name, path) :: texts)
}

object AssetList {

  def fromJson(json: String): Either[String, AssetList] =
    decode[AssetList](json) match {
      case Right(al) =>
        Right(al)

      case Left(e) =>
        Left("Failed to deserialise json into AssetList: " + e.getMessage)
    }

  val empty: AssetList =
    AssetList(Nil, Nil)

}

//upickle (which we should deprecate) won't deserialise ADT's without faff, so this is a cheap workaround.
case class SimpleAssetType(name: String, path: String) {
  def toTextAsset: TextAsset   = TextAsset(name, path)
  def toImageAsset: ImageAsset = ImageAsset(name, path)
}
