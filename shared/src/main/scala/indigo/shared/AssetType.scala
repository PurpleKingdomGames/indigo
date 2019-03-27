package indigo.shared

sealed trait AssetType {
  val name: String
  val path: String
}
object AssetType {
  final class Text(val name: String, val path: String)  extends AssetType
  object Text {
    def apply(name: String, path: String): Text = new Text(name, path)
  }

  final class Image(val name: String, val path: String) extends AssetType
  object Image {
    def apply(name: String, path: String): Image = new Image(name, path)
  }

  final class Audio(val name: String, val path: String) extends AssetType
  object Audio {
    def apply(name: String, path: String): Audio = new Audio(name, path)
  }

}
