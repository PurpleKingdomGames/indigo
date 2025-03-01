package indigoplugin

sealed trait FontLayout
object FontLayout {

  /** Each glyph is placed one after another until the `maxCharactersPerLine` limit is reached (the line is full), then
    * the next glyph is placed on the next line. The size of the final font sheet image is dynamically calculated based
    * on the size of the largest glyph and the `maxCharactersPerLine` value.
    *
    * The size of the largest glyph is used to ensure that the inclusion of non-prinable control characters (e.g. in the
    * `CharSet.ASCII` set) does not break the layout, even though this results in slightly larger texture sizes.
    */
  final case class Normal(maxCharactersPerLine: Int) extends FontLayout {
    def withMaxCharactersPerLine(newMaxCharactersPerLine: Int): FontLayout.Normal =
      this.copy(maxCharactersPerLine = newMaxCharactersPerLine)
  }

  /** Each glyph is placed in a grid, with each cell being a fixed size. There are no guarantees given that your choice
    * of font will work nicely. Characters are placed one after another. The size of the final font sheet image is
    * dynamically calculated based on the `cellWidth` and the `maxCharactersPerLine` value.
    */
  final case class Monospace(maxCharactersPerLine: Int, cellWidth: Int, cellHeight: Int) extends FontLayout {
    def withMaxCharactersPerLine(newMaxCharactersPerLine: Int): FontLayout.Monospace =
      this.copy(maxCharactersPerLine = newMaxCharactersPerLine)

    def withCellWidth(newCellWidth: Int): FontLayout.Monospace =
      this.copy(cellWidth = newCellWidth)
    def withCellHeight(newCellHeight: Int): FontLayout.Monospace =
      this.copy(cellHeight = newCellHeight)
    def withCellSize(square: Int): FontLayout.Monospace =
      this.copy(cellWidth = square, cellHeight = square)

    val fontSheetWidth: Int = maxCharactersPerLine * cellWidth
    def fontSheetHeight(charCount: Int): Int = {
      val lines = charCount / maxCharactersPerLine
      val extra = if (charCount % maxCharactersPerLine == 0) 0 else 1
      (lines + extra) * cellHeight
    }
  }

  /** Each glyph is placed in a grid with each cell being a fixed size. There are no guarantees given that your choice
    * of font will work nicely. Characters are placed at their index (char integer value). The size of the final font
    * sheet image is fixed.
    */
  final case class IndexedGrid(maxCharactersPerLine: Int, cellWidth: Int, cellHeight: Int) extends FontLayout {
    def withMaxCharactersPerLine(newMaxCharactersPerLine: Int): FontLayout.IndexedGrid =
      this.copy(maxCharactersPerLine = newMaxCharactersPerLine)

    def withCellWidth(newCellWidth: Int): FontLayout.IndexedGrid =
      this.copy(cellWidth = newCellWidth)
    def withCellHeight(newCellHeight: Int): FontLayout.IndexedGrid =
      this.copy(cellHeight = newCellHeight)
    def withCellSize(square: Int): FontLayout.IndexedGrid =
      this.copy(cellWidth = square, cellHeight = square)

    val fontSheetWidth: Int = maxCharactersPerLine * cellWidth
    def fontSheetHeight(charCodes: List[Int]): Int = {
      val lines = charCodes.max / maxCharactersPerLine
      val extra = if (charCodes.max % maxCharactersPerLine == 0) 0 else 1
      (lines + extra) * cellHeight
    }

  }

  val normal: FontLayout  = Normal(16)
  val default: FontLayout = normal

  def monospace(cellWidth: Int, cellHeight: Int): FontLayout = Monospace(16, cellWidth, cellHeight)
  def monospace(square: Int): FontLayout                     = Monospace(16, square, square)

  def indexedGrid(cellWidth: Int, cellHeight: Int): FontLayout =
    IndexedGrid(16, cellWidth, cellHeight)
  def indexedGrid(square: Int): FontLayout =
    IndexedGrid(16, square, square)

  def indexedGrid1x1: FontLayout   = indexedGrid(1)
  def indexedGrid4x6: FontLayout   = indexedGrid(4, 6)
  def indexedGrid5x5: FontLayout   = indexedGrid(5)
  def indexedGrid5x6: FontLayout   = indexedGrid(5, 6)
  def indexedGrid6x6: FontLayout   = indexedGrid(6)
  def indexedGrid6x8: FontLayout   = indexedGrid(6, 8)
  def indexedGrid6x9: FontLayout   = indexedGrid(6, 9)
  def indexedGrid6x10: FontLayout  = indexedGrid(6, 10)
  def indexedGrid7x7: FontLayout   = indexedGrid(7)
  def indexedGrid8x8: FontLayout   = indexedGrid(8)
  def indexedGrid8x12: FontLayout  = indexedGrid(8, 12)
  def indexedGrid8x14: FontLayout  = indexedGrid(8, 14)
  def indexedGrid8x15: FontLayout  = indexedGrid(8, 15)
  def indexedGrid8x16: FontLayout  = indexedGrid(8, 16)
  def indexedGrid9x9: FontLayout   = indexedGrid(9)
  def indexedGrid9x12: FontLayout  = indexedGrid(9, 12)
  def indexedGrid9x14: FontLayout  = indexedGrid(9, 14)
  def indexedGrid9x16: FontLayout  = indexedGrid(9, 16)
  def indexedGrid10x10: FontLayout = indexedGrid(10)
  def indexedGrid10x12: FontLayout = indexedGrid(10, 12)
  def indexedGrid10x16: FontLayout = indexedGrid(10, 16)
  def indexedGrid11x11: FontLayout = indexedGrid(11)
  def indexedGrid12x12: FontLayout = indexedGrid(12)
  def indexedGrid12x20: FontLayout = indexedGrid(12, 20)
  def indexedGrid13x13: FontLayout = indexedGrid(13)
  def indexedGrid14x14: FontLayout = indexedGrid(14)
  def indexedGrid14x16: FontLayout = indexedGrid(14, 16)
  def indexedGrid15x15: FontLayout = indexedGrid(15)
  def indexedGrid16x16: FontLayout = indexedGrid(16)
  def indexedGrid16x20: FontLayout = indexedGrid(16, 20)
  def indexedGrid16x24: FontLayout = indexedGrid(16, 24)
  def indexedGrid16x32: FontLayout = indexedGrid(16, 32)
  def indexedGrid17x17: FontLayout = indexedGrid(17)
  def indexedGrid18x18: FontLayout = indexedGrid(18)
  def indexedGrid20x20: FontLayout = indexedGrid(20)
  def indexedGrid20x32: FontLayout = indexedGrid(20, 32)
  def indexedGrid24x24: FontLayout = indexedGrid(24)
  def indexedGrid24x32: FontLayout = indexedGrid(24, 32)
  def indexedGrid24x36: FontLayout = indexedGrid(24, 36)
  def indexedGrid32x32: FontLayout = indexedGrid(32)
  def indexedGrid48x48: FontLayout = indexedGrid(48)
  def indexedGrid48x72: FontLayout = indexedGrid(48, 72)
  def indexedGrid64x64: FontLayout = indexedGrid(64)
}
