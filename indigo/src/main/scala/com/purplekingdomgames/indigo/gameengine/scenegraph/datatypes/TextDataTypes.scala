package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

case class FontInfo(charSize: Point, fontSpriteSheet: FontSpriteSheet, fontChar: FontChar, fontChars: List[FontChar]) {
  private val nonEmtpyChars: List[FontChar] = fontChar +: fontChars

  def addChar(fontChar: FontChar) = FontInfo(charSize, fontSpriteSheet, fontChar, nonEmtpyChars)

  def findByCharacter(character: String): FontChar = nonEmtpyChars.find(p => p.character == character).getOrElse(FontChar("?", Point(0, 0)))
}
case class FontSpriteSheet(imageAssetRef: String, size: Point)
case class FontChar(character: String, offset: Point)

sealed trait TextAlignment
case object AlignLeft extends TextAlignment
case object AlignCenter extends TextAlignment
case object AlignRight extends TextAlignment
