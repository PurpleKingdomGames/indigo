package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

case class FontInfo(charSize: Point, fontSpriteSheet: FontSpriteSheet, fontChar: FontChar, fontChars: List[FontChar]) {
  private val nonEmtpyChars: List[FontChar] = fontChar +: fontChars

  def addChar(fontChar: FontChar) = FontInfo(charSize, fontSpriteSheet, fontChar, nonEmtpyChars)

  def findByCharacter(character: String): FontChar = nonEmtpyChars.find(p => p.character == character).getOrElse(FontChar("?", Point(0, 0)))
}

object FontInfo {
  def apply(charWidth: Int, charHeight: Int, imageAssetRef: String, sheetWidth: Int, sheetHeight: Int, char: FontChar): FontInfo =
    FontInfo(
      Point(charWidth, charHeight),
      FontSpriteSheet(imageAssetRef, Point(sheetWidth, sheetHeight)),
      char,
      Nil
    )
}

case class FontSpriteSheet(imageAssetRef: String, size: Point)
case class FontChar(character: String, offset: Point)
object FontChar {
  def apply(character: String, x: Int, y: Int): FontChar = FontChar(character, Point(x, y))
}

sealed trait TextAlignment
case object AlignLeft extends TextAlignment
case object AlignCenter extends TextAlignment
case object AlignRight extends TextAlignment
