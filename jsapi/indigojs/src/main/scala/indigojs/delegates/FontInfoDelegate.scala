package indigojs.delegates

import scala.scalajs.js.JSConverters._
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
    _material: MaterialDelegate,
    _charSheetWidth: Int,
    _charSheetHeight: Int,
    _unknownChar: FontCharDelegate,
    _fontChars: js.Array[FontCharDelegate],
    _caseSensitive: Boolean
) {

  @JSExport
  val fontKey = _fontKey
  @JSExport
  val material = _material
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

  @JSExport
  def addChar(fontChar: FontCharDelegate): FontInfoDelegate =
    fromInternal(toInternal.addChar(fontChar.toInternal))

  @JSExport
  def addChars(chars: List[FontCharDelegate]): FontInfoDelegate =
    fromInternal(toInternal.addChars(chars.map{ c => c.toInternal }))

  @JSExport
  def addChars(chars: FontCharDelegate*): FontInfoDelegate =
    addChars(chars.toList)

  @JSExport
  def findByCharacter(character: String): FontCharDelegate = {
    val charObj = toInternal.findByCharacter(character);

    new FontCharDelegate(
        charObj.character,
        new RectangleDelegate(charObj.bounds.x, charObj.bounds.y, charObj.bounds.width, charObj.bounds.height)
    )
  }

  @JSExport
  def findByCharacter(character: Char): FontCharDelegate =
    findByCharacter(character.toString)

  @JSExport
  def makeCaseSensitive(sensitive: Boolean): FontInfoDelegate =
    fromInternal(toInternal.makeCaseSensitive(sensitive))

  @JSExport
  def isCaseSensitive: FontInfoDelegate =
    fromInternal(toInternal.isCaseSensitive)

  @JSExport
  def isCaseInSensitive: FontInfoDelegate =
    fromInternal(toInternal.isCaseInSensitive)

  def fromInternal(orig: FontInfo): FontInfoDelegate =
    new FontInfoDelegate(
        orig.fontKey.toString,
        MaterialDelegate.fromInternal(orig.fontSpriteSheet.material),
        orig.fontSpriteSheet.size.x,
        orig.fontSpriteSheet.size.y,
        new FontCharDelegate(
          orig.unknownChar.character,
          new RectangleDelegate(orig.unknownChar.bounds.x, orig.unknownChar.bounds.y, orig.unknownChar.bounds.width, orig.unknownChar.bounds.height)
        ),
        orig.fontChars.map{ f =>
          new FontCharDelegate(
            f.character,
            new RectangleDelegate(f.bounds.x, f.bounds.y, f.bounds.width, f.bounds.height)
          )
        }.toJSArray,
        orig.caseSensitive
    )

  def toInternal: FontInfo =
    FontInfo(
      FontKey(fontKey),
      FontSpriteSheet(material.toInternal, Point(charSheetWidth, charSheetHeight)),
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
