package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

case class FontInfo(fontSpriteSheet: FontSpriteSheet, unknownChar: FontChar, fontChars: List[FontChar]) {
  private val nonEmtpyChars: List[FontChar] = unknownChar +: fontChars

  def addChar(fontChar: FontChar) = FontInfo(fontSpriteSheet, fontChar, nonEmtpyChars)

  def findByCharacter(character: String): FontChar = nonEmtpyChars.find(p => p.character == character).getOrElse(unknownChar)
  def findByCharacter(character: Char): FontChar = findByCharacter(character.toString)
}

object FontInfo {
  def apply(imageAssetRef: String, sheetWidth: Int, sheetHeight: Int, unknownChar: FontChar, chars: FontChar*): FontInfo =
    FontInfo(
      FontSpriteSheet(imageAssetRef, Point(sheetWidth, sheetHeight)),
      unknownChar,
      chars.toList
    )
}

case class FontSpriteSheet(imageAssetRef: String, size: Point)
case class FontChar(character: String, bounds: Rectangle)
object FontChar {
  def apply(character: String, x: Int, y: Int, width: Int, height: Int): FontChar = FontChar(character, Rectangle(x, y, width, height))
}

sealed trait TextAlignment
case object AlignLeft extends TextAlignment
case object AlignCenter extends TextAlignment
case object AlignRight extends TextAlignment
