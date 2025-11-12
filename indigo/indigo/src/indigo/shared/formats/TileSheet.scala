package indigo.shared.formats

import indigo.*

/** Utility for working with generic sprite sheets. The sheet must be uniform, with each sprite having the same width
  * and height, and a (possibly zero-pixel) inner margin.
  */
final case class TileSheet(
    assetName: AssetName,
    imageSize: Size,
    tileSize: Size,
    margin: Int
):
  val maxRows: Int    = (imageSize.height + margin) / (tileSize.height + margin)
  val maxColumns: Int = (imageSize.width + margin) / (tileSize.width + margin)
  val maxIndex: Int   = maxRows * maxColumns - 1
  val length: Int     = maxIndex

  def rectangleForIndex(index: Int): Option[Rectangle] =
    rectangleForCoords(column = index % maxColumns, row = index / maxColumns)

  def rectangleForCoords(column: Int, row: Int): Option[Rectangle] =
    if column < 0 || column >= maxColumns || row < 0 || row >= maxRows then None
    else
      val x = column * (tileSize.width + margin)
      val y = row * (tileSize.height + margin)
      Some(Rectangle(Point(x, y), tileSize))

  def graphicFromRectangle(boundsOpt: Option[Rectangle]): Option[Graphic[Material.Bitmap]] =
    boundsOpt.map { bounds =>
      Graphic(
        bounds = bounds,
        material = Material.Bitmap(assetName)
      )
    }

  /** @param index
    *   a zero-based index that begins at the upper right of the sheet and increases as it goes right and down
    * @return
    *   an optional Graphic, None if the given index is out of bounds
    */
  def fromIndex(index: Int): Option[Graphic[Material.Bitmap]] =
    graphicFromRectangle(rectangleForIndex(index))

  /** @see #fromIndex */
  def apply(index: Int): Option[Graphic[Material.Bitmap]] = fromIndex(index)

  /** @param column
    *   zero-based
    * @param row
    *   zero-based
    * @return
    *   an optional Graphic, None if the given coordinates are out of bounds
    */
  def fromCoords(column: Int, row: Int): Option[Graphic[Material.Bitmap]] =
    graphicFromRectangle(rectangleForCoords(column, row))

  /** @see #fromCoords */
  def apply(column: Int, row: Int): Option[Graphic[Material.Bitmap]] =
    fromCoords(column, row)

  /** @param pt
    *   zero-based x and y grid location of tile
    * @return
    *   an optional Graphic, None if the given Point is out of bounds
    */
  def fromPoint(pt: Point): Option[Graphic[Material.Bitmap]] =
    fromCoords(pt.x, pt.y)

  /** @see #fromPoint */
  def apply(pt: Point): Option[Graphic[Material.Bitmap]] =
    fromPoint(pt)

object TileSheet:
  def apply(
      assetName: AssetName,
      width: Int,
      height: Int,
      tileWidth: Int,
      tileHeight: Int,
      margin: Int
  ): TileSheet =
    TileSheet(
      assetName = assetName,
      imageSize = Size(width, height),
      tileSize = Size(tileWidth, tileHeight),
      margin = margin
    )
