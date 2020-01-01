package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js

import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.FontSpriteSheet
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.Point

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("FontInfo")
final class FontInfoDelegate(
    _fontKey: String,
    _imageAssetRef: String,
    _charSheetWidth: Int,
    _charSheetHeight: Int,
    _unknownChar: FontCharDelegate,
    _fontChars: js.Array[FontCharDelegate],
    _caseSensitive: Boolean
) {

  @JSExport
  val fontKey = _fontKey
  @JSExport
  val imageAssetRef = _imageAssetRef
  @JSExport
  val charSheetWidth = _charSheetWidth
  @JSExport
  val charSheetHeight = _charSheetHeight
  @JSExport
  val unknownChar = _unknownChar
  @JSExport
  val fontChars = _fontChars
  @JSExport
  val caseSensitive = _caseSensitive

  def toInternal: FontInfo =
    FontInfo(
      FontKey(fontKey),
      FontSpriteSheet(imageAssetRef, Point(charSheetWidth, charSheetHeight)),
      unknownChar.toInternal,
      fontChars.map(_.toInternal).toList,
      caseSensitive
    )
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("FontChar")
final class FontCharDelegate(_character: String, _bounds: RectangleDelegate) {

  @JSExport
  val character = _character
  @JSExport
  val bounds = _bounds

  def toInternal: FontChar =
    FontChar(character, bounds.toInternal)
}
