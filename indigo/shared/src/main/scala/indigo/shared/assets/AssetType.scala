package indigo.shared.assets

import scala.annotation.tailrec

/**
  * Parent type of the different kinds of assets Indigo understands.
  */
sealed trait AssetType {
  def toList: List[AssetType]
}

/**
  * Represents concrete, loadable asset types.
  */
sealed trait AssetTypePrimitive extends AssetType {
  val name: AssetName
  val path: AssetPath
}

object AssetType {

  /**
    * Flattens assets arranged in a Tagged hierarchy into a flat list of loadable assets, appropriately tagged.
    *
    * @param assets The potentially tagged hierarchy list.
    * @return List[AssetTypePrimitive]
    */
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

  final case class Text(name: AssetName, path: AssetPath) extends AssetTypePrimitive {
    def toList: List[AssetType] = List(this)
  }

  final case class Image(name: AssetName, path: AssetPath, tag: Option[AssetTag]) extends AssetTypePrimitive {
    def withTag(tag: AssetTag): Image =
      this.copy(tag = Option(tag))

    def noTag: Image =
      this.copy(tag = None)

    def toList: List[AssetType] =
      List(this)
  }
  object Image {
    def apply(name: AssetName, path: AssetPath): Image = Image(name, path, None)
  }

  final case class Audio(name: AssetName, path: AssetPath) extends AssetTypePrimitive {
    def toList: List[AssetType] = List(this)
  }

}
