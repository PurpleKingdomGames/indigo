package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js

import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.FontSpriteSheet
import indigo.shared.datatypes.FontChar

@JSExportTopLevel("FontInfo")
final class FontInfoDelegate(
    val fontKey: FontKeyDelegate,
    val fontSpriteSheet: FontSpriteSheetDelegate,
    val unknownChar: FontCharDelegate,
    val fontChars: js.Array[FontCharDelegate],
    val caseSensitive: Boolean
) {
  def toInternal: FontInfo =
    FontInfo(
      fontKey.toInternal,
      fontSpriteSheet.toInternal,
      unknownChar.toInternal,
      fontChars.map(_.toInternal).toList,
      caseSensitive
    )
}

@JSExportTopLevel("FontKey")
final class FontKeyDelegate(val key: String) {
  def toInternal: FontKey =
    FontKey(key)
}

@JSExportTopLevel("FontSpriteSheet")
final class FontSpriteSheetDelegate(val imageAssetRef: String, val size: PointDelegate) {
  def toInternal: FontSpriteSheet =
    FontSpriteSheet(imageAssetRef, size.toInternal)
}

@JSExportTopLevel("FontChar")
final class FontCharDelegate(val character: String, val bounds: RectangleDelegate) {
  def toInternal: FontChar =
    FontChar(character, bounds.toInternal)
}
