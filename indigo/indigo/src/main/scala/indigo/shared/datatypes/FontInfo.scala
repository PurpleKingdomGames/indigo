package indigo.shared.datatypes

import indigo.shared.QuickCache
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyBatch

final case class FontInfo(
    fontKey: FontKey,
    fontSheetBounds: Size,
    unknownChar: FontChar,
    fontChars: Batch[FontChar],
    caseSensitive: Boolean
) derives CanEqual:
  import FontInfo.fontCharCache

  private val nonEmptyChars: NonEmptyBatch[FontChar] = NonEmptyBatch(unknownChar, fontChars)

  def addChar(fontChar: FontChar): FontInfo =
    this.copy(fontChars = nonEmptyChars.toBatch ++ Batch(fontChar))

  def addChars(chars: Batch[FontChar]): FontInfo =
    this.copy(fontChars = fontChars ++ chars)

  def addChars(chars: FontChar*): FontInfo =
    addChars(Batch.fromSeq(chars))

  def findByCharacter(character: String): FontChar =
    QuickCache("char-" + character + "-" + fontKey.toString) {
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

object FontInfo:

  implicit val fontCharCache: QuickCache[FontChar] = QuickCache.empty

  def apply(fontKey: FontKey, sheetWidth: Int, sheetHeight: Int, unknownChar: FontChar, chars: FontChar*): FontInfo =
    FontInfo(
      fontKey = fontKey,
      fontSheetBounds = Size(sheetWidth, sheetHeight),
      unknownChar = unknownChar,
      fontChars = Batch.fromSeq(chars),
      caseSensitive = false
    )

opaque type FontKey = String
object FontKey:
  inline def apply(key: String): FontKey = key

  extension (f: FontKey) def toString: String = f

final case class FontChar(character: String, bounds: Rectangle) derives CanEqual
object FontChar:
  def apply(character: String, x: Int, y: Int, width: Int, height: Int): FontChar =
    FontChar(character, Rectangle(x, y, width, height))

enum TextAlignment derives CanEqual:
  case Left, Center, Right
