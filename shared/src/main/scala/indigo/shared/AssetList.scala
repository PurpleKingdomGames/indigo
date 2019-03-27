package indigo.shared

import io.circe.generic.auto._
import io.circe.parser._

final class AssetList(val images: List[SimpleAssetType], val texts: List[SimpleAssetType]) {
  def toSet: Set[AssetType] = texts.map(_.toTextAsset).toSet ++ images.map(_.toImageAsset).toSet

  def withImage(name: String, path: String): AssetList =
    AssetList(SimpleAssetType(name, path) :: images, texts)

  def withText(name: String, path: String): AssetList =
    AssetList(images, SimpleAssetType(name, path) :: texts)
}

object AssetList {

  def apply(images: List[SimpleAssetType], texts: List[SimpleAssetType]): AssetList =
    new AssetList(images, texts)

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  def fromJson(json: String): Either[String, AssetList] =
    decode[AssetList](json) match {
      case Right(al) =>
        Right[String, AssetList](al)

      case Left(e) =>
        Left[String, AssetList]("Failed to deserialise json into AssetList: " + e.getMessage)
    }

  val empty: AssetList =
    AssetList(Nil, Nil)

}

final case class SimpleAssetType(name: String, path: String) {
  def toTextAsset: AssetType.Text   = AssetType.Text(name, path)
  def toImageAsset: AssetType.Image = AssetType.Image(name, path)
}
