package indigo.shared.assets

import scala.annotation.tailrec

sealed trait AssetType {
  def toList: List[AssetType]
}

sealed trait AssetTypePrimitive extends AssetType {
  val name: AssetName
  val path: AssetPath
}

object AssetType {

  def flattenAssetList(assets: List[AssetType]): List[AssetTypePrimitive] = {
    @tailrec
    def rec(remaining: List[AssetType], acc: List[AssetTypePrimitive]): List[AssetTypePrimitive] =
      remaining match {
        case Nil =>
          acc.reverse

        case Tagged(imgs) :: xs =>
          rec(imgs ++ xs, acc)

        case (x: AssetTypePrimitive) :: xs =>
          rec(xs, x :: acc)

        case _ :: xs =>
          rec(xs, acc)
      }

    rec(assets, Nil)
  }

  final class Tagged(private val assets: List[Image]) extends AssetType {
    def toList: List[Image] = assets
  }
  object Tagged {
    def apply(tag: String)(images: Image*): Tagged =
      new Tagged(images.toList.map(_.withTag(AssetTag(tag))))

    def unapply(tagged: Tagged): Option[List[Image]] =
      Some(tagged.toList)
  }

  final class Text(val name: AssetName, val path: AssetPath) extends AssetTypePrimitive {
    def toList: List[AssetType] = List(this)

    override def toString: String =
      s"AssetType.Text(${name.toString()}, ${path.toString})"
  }
  object Text {
    def apply(name: AssetName, path: AssetPath): Text = new Text(name, path)
  }

  final class Image(val name: AssetName, val path: AssetPath, val tag: Option[AssetTag]) extends AssetTypePrimitive {
    def withTag(tag: AssetTag): Image =
      new Image(name, path, Option(tag))

    def noTag: Image =
      new Image(name, path, None)

    def toList: List[AssetType] = List(this)

    override def toString: String =
      s"AssetType.Image(${name.toString()}, ${path.toString})"
  }
  object Image {
    def apply(name: AssetName, path: AssetPath): Image = new Image(name, path, None)
  }

  final class Audio(val name: AssetName, val path: AssetPath) extends AssetTypePrimitive {
    def toList: List[AssetType] = List(this)

    override def toString: String =
      s"AssetType.Audio(${name.toString()}, ${path.toString})"
  }
  object Audio {
    def apply(name: AssetName, path: AssetPath): Audio = new Audio(name, path)
  }

}
