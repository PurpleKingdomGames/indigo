package indigo.shared.assets

final class AssetList(val images: List[SimpleAssetType], val texts: List[SimpleAssetType]) {
  def toSet: Set[AssetType] = texts.map(_.toTextAsset).toSet ++ images.map(_.toImageAsset).toSet

  def withImage(name: AssetName, path: AssetPath): AssetList =
    AssetList(SimpleAssetType(name, path) :: images, texts)

  def withText(name: AssetName, path: AssetPath): AssetList =
    AssetList(images, SimpleAssetType(name, path) :: texts)
}

object AssetList {

  def apply(images: List[SimpleAssetType], texts: List[SimpleAssetType]): AssetList =
    new AssetList(images, texts)

  val empty: AssetList =
    AssetList(Nil, Nil)

}

final case class SimpleAssetType(name: AssetName, path: AssetPath) {
  def toTextAsset: AssetType.Text   = AssetType.Text(name, path)
  def toImageAsset: AssetType.Image = AssetType.Image(name, path)
}
