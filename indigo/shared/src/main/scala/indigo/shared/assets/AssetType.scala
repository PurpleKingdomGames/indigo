package indigo.shared.assets

sealed trait AssetType {
  val name: AssetName
  val path: AssetPath
}
object AssetType {
  final class Text(val name: AssetName, val path: AssetPath)  extends AssetType
  object Text {
    def apply(name: AssetName, path: AssetPath): Text = new Text(name, path)
  }

  final class Image(val name: AssetName, val path: AssetPath) extends AssetType
  object Image {
    def apply(name: AssetName, path: AssetPath): Image = new Image(name, path)
  }

  final class Audio(val name: AssetName, val path: AssetPath) extends AssetType
  object Audio {
    def apply(name: AssetName, path: AssetPath): Audio = new Audio(name, path)
  }

}
