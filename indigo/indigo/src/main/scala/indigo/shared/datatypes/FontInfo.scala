package indigo.shared.datatypes

import indigo.shared.QuickCache
import indigo.shared.materials.Material

final case class FontInfo(
    fontKey: FontKey,
    fontSheetBounds: Point,
    unknownChar: FontChar,
    fontChars: List[FontChar],
    caseSensitive: Boolean
) derives CanEqual {
  import FontInfo.fontCharCache

  private val nonEmptyChars: List[FontChar] = unknownChar +: fontChars

  def addChar(fontChar: FontChar): FontInfo =
    this.copy(fontChars = nonEmptyChars ++ List(fontChar))

  def addChars(chars: List[FontChar]): FontInfo =
    this.copy(fontChars = fontChars ++ chars)

  def addChars(chars: FontChar*): FontInfo =
    addChars(chars.toList)

  def findByCharacter(character: String): FontChar =
    QuickCache("char-" + character + "-" + fontKey) {
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

opaque type FontKey = String
object FontKey:
  def apply(key: String): FontKey = key

final case class FontSpriteSheet(material: Material, size: Point) derives CanEqual

final case class FontChar(character: String, bounds: Rectangle) derives CanEqual
object FontChar {
  def apply(character: String, x: Int, y: Int, width: Int, height: Int): FontChar =
    FontChar(character, Rectangle(x, y, width, height))
}

enum TextAlignment derives CanEqual:
  case Left, Center, Right
