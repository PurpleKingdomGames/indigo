package indigo.shared

sealed trait AssetType
object AssetType {
  final case class Text(name: String, path: String)  extends AssetType
  final case class Image(name: String, path: String) extends AssetType
  final case class Audio(name: String, path: String) extends AssetType
}
