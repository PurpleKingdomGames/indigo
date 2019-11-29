package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js

import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.FontSpriteSheet
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.Point

@JSExportTopLevel("FontInfo")
final class FontInfoDelegate(
    val fontKey: String,
    val imageAssetRef: String,
    val sheetWidth: Int,
    val sheetHeight: Int,
    val unknownChar: FontCharDelegate,
    val fontChars: js.Array[FontCharDelegate],
    val caseSensitive: Boolean
) {
  def toInternal: FontInfo =
    FontInfo(
      FontKey(fontKey),
      FontSpriteSheet(imageAssetRef, Point(sheetWidth, sheetHeight)),
      unknownChar.toInternal,
      fontChars.map(_.toInternal).toList,
      caseSensitive
    )
}

@JSExportTopLevel("FontChar")
final class FontCharDelegate(val character: String, val bounds: RectangleDelegate) {
  def toInternal: FontChar =
    FontChar(character, bounds.toInternal)
}
