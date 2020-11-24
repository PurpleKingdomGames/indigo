package indigo.json.core

import indigo.shared.datatypes.FontChar

final case class GlyphWrapper(glyphs: List[Glyph])
final case class Glyph(char: String, x: Int, y: Int, w: Int, h: Int) {
  def toFontChar: FontChar =
    FontChar(char, x, y, w, h)
}
