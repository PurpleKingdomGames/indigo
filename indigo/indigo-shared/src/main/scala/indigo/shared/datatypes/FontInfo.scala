package indigo.shared.datatypes

import indigo.shared.QuickCache
import indigo.shared.materials.StandardMaterial

final case class FontInfo(fontKey: FontKey, fontSheetBounds: Point, unknownChar: FontChar, fontChars: List[FontChar], caseSensitive: Boolean) {
  import FontInfo.fontCharCache

  private val nonEmptyChars: List[FontChar] = unknownChar +: fontChars

  def addChar(fontChar: FontChar): FontInfo =
    this.copy(fontChars = nonEmptyChars ++ List(fontChar))

  def addChars(chars: List[FontChar]): FontInfo =
    this.copy(fontChars = fontChars ++ chars)

  def addChars(chars: FontChar*): FontInfo =
    addChars(chars.toList)

  def findByCharacter(character: String): FontChar =
    QuickCache("char-" + character + "-" + fontKey.key) {
      nonEmptyChars
        .find { p =>
          if (caseSensitive) p.character == character else p.character.toLowerCase == character.toLowerCase
        }
        .getOrElse(unknownChar)
    }
  def findByCharacter(character: Char): FontChar =
    findByCharacter(character.toString)

  def makeCaseSensitive(sensitive: Boolean): FontInfo =
    this.copy(caseSensitive = sensitive)

  def isCaseSensitive: FontInfo =
    makeCaseSensitive(true)
  def isCaseInSensitive: FontInfo =
    makeCaseSensitive(false)
}

object FontInfo {

  implicit val fontCharCache: QuickCache[FontChar] = QuickCache.empty

  def apply(fontKey: FontKey, sheetWidth: Int, sheetHeight: Int, unknownChar: FontChar, chars: FontChar*): FontInfo =
    FontInfo(
      fontKey = fontKey,
      fontSheetBounds = Point(sheetWidth, sheetHeight),
      unknownChar = unknownChar,
      fontChars = chars.toList,
      caseSensitive = false
    )
}

final case class FontKey(key: String) extends AnyVal

final case class FontSpriteSheet(material: StandardMaterial, size: Point)

final case class FontChar(character: String, bounds: Rectangle)
object FontChar {
  def apply(character: String, x: Int, y: Int, width: Int, height: Int): FontChar =
    FontChar(character, Rectangle(x, y, width, height))
}

sealed trait TextAlignment
object TextAlignment {
  case object Left   extends TextAlignment
  case object Center extends TextAlignment
  case object Right  extends TextAlignment
}
