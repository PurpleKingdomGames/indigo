package indigo.shared.assets

sealed trait AssetType {
  def toList: List[AssetType]
}

object AssetType {

  final class Tagged(private val assets: List[Image]) extends AssetType {
    def toList: List[AssetType] = assets
  }
  object Tagged {
    def apply(tag: String)(images: Image*): Tagged =
      new Tagged(images.toList.map(_.withTag(AssetTag(tag))))
  }

  final class Text(val name: AssetName, val path: AssetPath) extends AssetType {
    def toList: List[AssetType] = List(this)
  }
  object Text {
    def apply(name: AssetName, path: AssetPath): Text = new Text(name, path)
  }

  final class Image(val name: AssetName, val path: AssetPath, val tag: Option[AssetTag]) extends AssetType {
    def withTag(tag: AssetTag): Image =
      new Image(name, path, Option(tag))

    def noTag: Image =
      new Image(name, path, None)

    def toList: List[AssetType] = List(this)
  }
  object Image {
    def apply(name: AssetName, path: AssetPath): Image = new Image(name, path, None)
  }

  final class Audio(val name: AssetName, val path: AssetPath) extends AssetType {
    def toList: List[AssetType] = List(this)
  }
  object Audio {
    def apply(name: AssetName, path: AssetPath): Audio = new Audio(name, path)
  }

}
