package indigo.shared.assets

import scala.annotation.tailrec

/** Parent type of the different kinds of assets Indigo understands.
  */
sealed trait AssetType derives CanEqual {
  def toList: List[AssetType]
}

/** Represents concrete, loadable asset types.
  */
sealed trait AssetTypePrimitive extends AssetType {
  val name: AssetName
  val path: AssetPath
}

object AssetType {

  given CanEqual[AssetType, AssetType]                 = CanEqual.derived
  given CanEqual[Option[AssetType], Option[AssetType]] = CanEqual.derived

  /** Flattens assets arranged in a Tagged hierarchy into a flat list of loadable assets, appropriately tagged.
    *
    * @param assets
    *   The potentially tagged hierarchy list.
    * @return
    *   List[AssetTypePrimitive]
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

  /** Tagged instance, the preferred constructor is the apply method: Tagged("my tag")(image1..imageN)
    *
    * Images can optionally be "tagged", this tells indigo that images with the same tag should be grouped together for
    * more efficient use. E.g. you might tag all the images for a given level with the same tag.
    *
    * @param tag
    *   The AssetTag to apply to all the images
    * @param images
    *   The image assets to be tagged
    */
  final case class Tagged(tag: AssetTag, images: List[Image]) extends AssetType derives CanEqual {
    def toList: List[Image] = images.map(_.withTag(tag))
  }
  object Tagged {

    /** Images can optionally be "tagged", this tells indigo that images with the same tag should be grouped together
      * for more efficient use. E.g. you might tag all the images for a given level with the same tag.
      *
      * @param tag
      *   A string tag name, which is converted to an AssetTag
      * @param images
      *   The image assets to be tagged
      * @return
      *   An instance of Tagged which can be nested along side your usual asset declarations
      */
    def apply(tag: String)(images: Image*): Tagged =
      Tagged(AssetTag(tag), images.toList)

    /** Extractor for Tagged
      *
      * @param tagged
      *   the tagged instance to extract
      * @return
      *   a list of tagged image assets
      */
    def unapply(tagged: Tagged): Option[List[Image]] =
      Some(tagged.toList)
  }

  /** Represents a text asset, Indigo does not care about the type of data held in the text file, it is up to the
    * programmer to parse it.
    *
    * @param name
    *   the asset name used to look up the loaded asset
    * @param path
    *   the path to the asset
    */
  final case class Text(name: AssetName, path: AssetPath) extends AssetTypePrimitive derives CanEqual {
    def toList: List[AssetType] = List(this)
  }

  /** Represents any browser compatible image asset
    *
    * @param name
    *   the asset name used to look up the loaded asset
    * @param path
    *   the path to the asset
    * @param tag
    *   Images can optionally be "tagged", this tells indigo that images with the same tag should be grouped together
    *   for more efficient use. E.g. you might tag all the images for a given level with the same tag.
    */
  final case class Image(name: AssetName, path: AssetPath, tag: Option[AssetTag]) extends AssetTypePrimitive
      derives CanEqual {
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

  /** Represents any browser compatible audio asset
    *
    * @param name
    *   the asset name used to look up the loaded asset
    * @param path
    *   the path to the asset
    */
  final case class Audio(name: AssetName, path: AssetPath) extends AssetTypePrimitive derives CanEqual {
    def toList: List[AssetType] = List(this)
  }

}
