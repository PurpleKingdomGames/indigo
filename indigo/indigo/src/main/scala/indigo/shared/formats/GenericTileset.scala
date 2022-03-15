package indigo.shared.formats

import indigo._

/** Utility for working with generic sprite sheets. The
  * sheet must be uniform, with each sprite having the same
  * width and height, and a (possibly zero-pixel) margin.
  */
final case class GenericTileset(
    assetName: AssetName,
    width: Int,
    height: Int,
    tileWidth: Int,
    tileHeight: Int,
    margin: Int
) {
  private val rectangles =
    for {
      y <- 0 until height by tileHeight + margin
      x <- 0 until width by tileWidth + margin
      rectangle = Rectangle(x, y, tileWidth, tileHeight)
    } yield rectangle

  protected[formats] def lookupRectangle(idx: Int): Option[Rectangle] =
    rectangles.lift(idx)

/** @param idx a zero-based index that begins at the upper right of the sheet
  * and increases as it goes right and down
  * @return an optional Graphic, None if the given index was out of bounds.
  */
  def apply(idx: Int): Option[Graphic[Material.Bitmap]] =
    lookupRectangle(idx).map { rectangle =>
      Graphic(
        Rectangle(0, 0, tileWidth, tileHeight),
        Material.Bitmap(assetName)
      )
        .withCrop(rectangle)
    }
}
