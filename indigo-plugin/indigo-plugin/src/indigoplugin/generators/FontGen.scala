package indigoplugin.generators

import scala.annotation.tailrec

/** Provides functionality for generating font images and associated FontInfo instances.
  */
object FontGen {

  def generate(
      moduleName: String,
      fullyQualifiedPackage: String,
      fontFilePath: os.Path,
      fontOptions: FontOptions,
      imageOut: os.Path
  ): os.Path => Seq[os.Path] = outDir => {

    // Some director sanity checking...
    if (!os.exists(imageOut)) {
      throw new Exception(
        s"The supplied path to the output directory for the font-sheet image does not exist: ${imageOut.toString}"
      )
    } else if (!os.isDir(imageOut)) {
      throw new Exception(
        s"The supplied path to the output directory for the font-sheet image is not a directory: ${imageOut.toString}"
      )
    } else {
      //
    }

    val wd = outDir / Generators.OutputDirName

    os.makeDir.all(wd)

    val file = wd / s"$moduleName.scala"

    val helper = FontAWTHelper.makeHelper(fontFilePath.toIO, fontOptions.fontSize)

    // Process into laid out details
    val initialCharDetails =
      fontOptions.charSet.toCharacterCodes.map { c =>
        val (w, h, a) = helper.getCharBounds(c.toChar)
        CharDetail(c.toChar, c, 0, 0, w, h, a)
      }.toList

    val filteredCharDetails =
      initialCharDetails
        .filter(cd => filterUnsupportedChars(cd.char, cd.code))

    if (initialCharDetails.length > filteredCharDetails.length)
      println("WARNING: Some unsupported characters were filtered out.")

    val charDetails =
      layout(
        filteredCharDetails,
        helper.getBaselineOffset,
        fontOptions.maxCharactersPerLine
      )

    // Write out FontInfo
    val default =
      charDetails
        .find(_.char == fontOptions.charSet.default)
        .getOrElse(throw new Exception(s"Couldn't find default character '${fontOptions.charSet.default.toString}'"))

    val (sheetWidth, sheetHeight) = findBounds(charDetails)
    val fontInfo =
      genFontInfo(moduleName, fullyQualifiedPackage, fontOptions.fontKey, sheetWidth, sheetHeight, default, charDetails)

    os.write.over(file, fontInfo)

    // Write out font image
    val outImageFileName = s"$moduleName.png"

    helper.drawFontSheet((imageOut / outImageFileName).toIO, charDetails, sheetWidth, sheetHeight, fontOptions)

    Seq(
      file,
      imageOut / outImageFileName
    )
  }

  def sanitiseName(name: String, ext: String): String = {
    val noExt = if (ext.nonEmpty && name.endsWith(ext)) name.dropRight(ext.length) else name
    noExt.replaceAll("[^A-Za-z0-9]", "-").split("-").map(_.capitalize).mkString
  }

  // TODO: Does nothing
  def layout(unplacedChars: List[CharDetail], lineSpacing: Int, maxCharsPerLine: Int): List[CharDetail] = {
    @tailrec
    def rec(
        remaining: List[CharDetail],
        lineCount: Int,
        charCount: Int,
        nextX: Int,
        acc: List[CharDetail]
    ): List[CharDetail] =
      remaining match {
        case Nil =>
          acc.reverse

        case c :: cs if charCount == maxCharsPerLine =>
          rec(c :: cs, lineCount + 1, 0, 0, acc)

        case c :: cs =>
          val x    = nextX
          val y    = lineCount * lineSpacing
          val newC = c.copy(x = x, y = y)

          rec(cs, lineCount, charCount + 1, nextX + c.width, newC :: acc)
      }

    rec(unplacedChars, 0, 0, 0, Nil)
  }

  def findBounds(charDetails: List[CharDetail]): (Int, Int) =
    charDetails.foldLeft((0, 0)) { case ((w, h), c) =>
      (
        if (c.x + c.width > w) c.x + c.width else w,
        if (c.y + c.height > h) c.y + c.height else h
      )
    }

  def genFontInfo(
      moduleName: String,
      fullyQualifiedPackage: String,
      name: String,
      sheetWidth: Int,
      sheetHeight: Int,
      default: CharDetail,
      chars: List[CharDetail]
  ): String = {

    val charString = chars
      .map(cd => "          " + toFontChar(cd) + ",")
      .mkString("\n")
      .dropRight(1) // Drops the last ','

    val dx = default.x.toString
    val dy = default.y.toString
    val dw = default.width.toString
    val dh = default.height.toString

    s"""package $fullyQualifiedPackage
    |
    |import indigo.*
    |
    |// DO NOT EDIT: Generated by Indigo.
    |object $moduleName {
    |
    |  val fontKey: FontKey = FontKey("$name")
    |
    |  val fontInfo: FontInfo =
    |    FontInfo(
    |      fontKey,
    |      $sheetWidth,
    |      $sheetHeight,
    |      FontChar("${default.char.toString()}", $dx, $dy, $dw, $dh)
    |    ).isCaseSensitive
    |      .addChars(
    |        Batch(
    |$charString
    |        )
    |      )
    |
    |}
    |""".stripMargin
  }

  def toFontChar(
      charDetail: CharDetail
  ): String = {
    val c = escapeChar(charDetail.char)
    val x = charDetail.x.toString()
    val y = charDetail.y.toString()
    val w = charDetail.width.toString()
    val h = charDetail.height.toString()

    s"""FontChar("$c", $x, $y, $w, $h)"""
  }

  def filterUnsupportedChars(c: Char, code: Int): Boolean =
    c match {
      case '\n'            => false
      case '\t'            => false
      case '\b'            => false
      case '\r'            => false
      case '\f'            => false
      case _ if code == 0  => false
      case _ if code == 26 => false
      case _               => true
    }

  def escapeChar(c: Char): String =
    c match {
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case '\'' => "\\'"
      case _    => c.toString
    }

  def charCodesToRanges(charCodes: List[Int]): List[FromTo] = {
    val codes = charCodes.distinct.sorted

    codes.headOption match {
      case None =>
        List.empty[FromTo]

      case Some(start) =>
        codes.tail.foldLeft(List(FromTo(start))) { case (acc, code) =>
          acc.headOption match {
            case Some(FromTo(from, to)) if code == to + 1 => FromTo(from, code) :: acc.tail
            case _                                        => FromTo(code) :: acc
          }
        }
    }
  }

  final case class FromTo(from: Int, to: Int)
  object FromTo {
    def apply(code: Int): FromTo =
      FromTo(code, code)
  }
}

object FontAWTHelper {

  import java.awt._
  import java.awt.image._
  import java.io._
  import javax.imageio.ImageIO

  def makeHelper(fontFile: File, fontSize: Int): Helper = {
    val font =
      Font
        .createFont(Font.TRUETYPE_FONT, fontFile)
        .deriveFont(fontSize.toFloat)

    Helper(font)
  }

  final case class Helper(font: Font) {

    def getCharBounds(char: Char): (Int, Int, Int) = {
      val tmpBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)

      val tmpG2d = tmpBuffer.createGraphics()
      tmpG2d.setFont(font)

      val fontMetrics = tmpG2d.getFontMetrics()

      val w = fontMetrics.charWidth(char)
      val h = fontMetrics.getHeight()
      val a = fontMetrics.getAscent()

      tmpG2d.dispose()

      (w, h, a)
    }

    def getBaselineOffset: Int = {
      val tmpBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
      val tmpG2d    = tmpBuffer.createGraphics()
      tmpG2d.setFont(font)
      val fontMetrics = tmpG2d.getFontMetrics()

      fontMetrics.getLeading() + fontMetrics.getAscent() + fontMetrics.getDescent()
    }

    def drawFontSheet(
        outFile: File,
        charDetails: scala.collection.immutable.List[CharDetail],
        sheetWidth: Int,
        sheetHeight: Int,
        fontOptions: FontOptions
    ): Unit = {
      val bufferedImage = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB)

      val g2d = bufferedImage.createGraphics()

      if (fontOptions.antiAlias) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      }

      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

      g2d.setFont(font)
      g2d.setColor(fontOptions.color.toColor)

      charDetails.foreach { c =>
        g2d.drawString(c.char.toString, c.x, c.y + c.ascent)
      }

      g2d.dispose()

      ImageIO.write(bufferedImage, "PNG", outFile)

      ()
    }

  }

}

final case class CharDetail(char: Char, code: Int, x: Int, y: Int, width: Int, height: Int, ascent: Int)

final case class FontOptions(
    fontKey: String,
    fontSize: Int,
    charSet: CharSet,
    color: RGB,
    antiAlias: Boolean,
    maxCharactersPerLine: Int
) {

  def withFontKey(newFontKey: String): FontOptions =
    this.copy(fontKey = newFontKey)

  def withFontSize(newFontSize: Int): FontOptions =
    this.copy(fontSize = newFontSize)

  def withChars(newCharSet: CharSet): FontOptions =
    this.copy(charSet = newCharSet)

  def withColor(newColor: RGB): FontOptions =
    this.copy(color = newColor)

  def withAntiAlias(newAntiAlias: Boolean): FontOptions =
    this.copy(antiAlias = newAntiAlias)
  def useAntiAliasing: FontOptions =
    withAntiAlias(true)
  def noAntiAliasing: FontOptions =
    withAntiAlias(false)

  def withMaxCharactersPerLine(newMaxCharactersPerLine: Int): FontOptions =
    this.copy(maxCharactersPerLine = newMaxCharactersPerLine)

}

object FontOptions {

  def apply(fontKey: String, fontSize: Int): FontOptions =
    FontOptions(fontKey, fontSize, CharSet.ASCII, RGB.White, false, 16)

  def apply(fontKey: String, fontSize: Int, charSet: CharSet): FontOptions =
    FontOptions(fontKey, fontSize, charSet, RGB.White, false, 16)

}

final case class RGB(r: Double, g: Double, b: Double) {

  def toColor: java.awt.Color =
    new java.awt.Color((r * 255).toInt, (g * 255).toInt, (b * 255).toInt)

  def +(other: RGB): RGB =
    RGB.combine(this, other)

  def withRed(newRed: Double): RGB =
    this.copy(r = newRed)

  def withGreen(newGreen: Double): RGB =
    this.copy(g = newGreen)

  def withBlue(newBlue: Double): RGB =
    this.copy(b = newBlue)

  def mix(other: RGB, amount: Double): RGB = {
    val mix = Math.min(1.0, Math.max(0.0, amount))
    RGB(
      (r * (1.0 - mix)) + (other.r * mix),
      (g * (1.0 - mix)) + (other.g * mix),
      (b * (1.0 - mix)) + (other.b * mix)
    )
  }
  def mix(other: RGB): RGB =
    mix(other, 0.5)

}

object RGB {

  val Red: RGB       = RGB(1, 0, 0)
  val Green: RGB     = RGB(0, 1, 0)
  val Blue: RGB      = RGB(0, 0, 1)
  val Yellow: RGB    = RGB(1, 1, 0)
  val Magenta: RGB   = RGB(1, 0, 1)
  val Cyan: RGB      = RGB(0, 1, 1)
  val White: RGB     = RGB(1, 1, 1)
  val Black: RGB     = RGB(0, 0, 0)
  val Coral: RGB     = fromHexString("#FF7F50")
  val Crimson: RGB   = fromHexString("#DC143C")
  val DarkBlue: RGB  = fromHexString("#00008B")
  val Indigo: RGB    = fromHexString("#4B0082")
  val Olive: RGB     = fromHexString("#808000")
  val Orange: RGB    = fromHexString("#FFA500")
  val Pink: RGB      = fromHexString("#FFC0CB")
  val Plum: RGB      = fromHexString("#DDA0DD")
  val Purple: RGB    = fromHexString("#A020F0")
  val Salmon: RGB    = fromHexString("#FA8072")
  val SeaGreen: RGB  = fromHexString("#2E8B57")
  val Silver: RGB    = fromHexString("#C0C0C0")
  val SlateGray: RGB = fromHexString("#708090")
  val SteelBlue: RGB = fromHexString("#4682B4")
  val Teal: RGB      = fromHexString("#008080")
  val Thistle: RGB   = fromHexString("#D8BFD8")
  val Tomato: RGB    = fromHexString("#FF6347")

  val Normal: RGB = White
  val Zero: RGB   = RGB(0, 0, 0)

  def combine(a: RGB, b: RGB): RGB =
    (a, b) match {
      case (RGB.White, x) =>
        x
      case (x, RGB.White) =>
        x
      case (x, y) =>
        RGB(x.r + y.r, x.g + y.g, x.b + y.b)
    }

  def fromHexString(hex: String): RGB =
    hex match {
      case h if h.startsWith("0x") && h.length == 8 =>
        fromColorInts(
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6, 8), 16)
        )

      case h if h.startsWith("#") && h.length == 7 =>
        fromColorInts(
          Integer.parseInt(hex.substring(1, 3), 16),
          Integer.parseInt(hex.substring(3, 5), 16),
          Integer.parseInt(hex.substring(5, 7), 16)
        )

      case h if h.length == 6 =>
        fromColorInts(
          Integer.parseInt(hex.substring(0, 2), 16),
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4), 16)
        )

      case _ =>
        RGB.White
    }

  def fromColorInts(r: Int, g: Int, b: Int): RGB =
    RGB((1.0 / 255) * r, (1.0 / 255) * g, (1.0 / 255) * b)

}

/** Represents a set of characters used for generating fonts.
  *
  * @param characters
  *   The string containing the characters in the set.
  */
final case class CharSet(characters: String, default: Char) {

  def toCharacterCodes: Array[Int] =
    characters.toCharArray.map(_.toInt)

}

object CharSet {

  val DefaultCharacter: Char = ' '

  def fromString(characters: String, default: Char): CharSet =
    CharSet(characters, default)
  def fromString(characters: String): CharSet =
    fromString(characters + DefaultCharacter, DefaultCharacter)

  def fromSeq(chars: Seq[Char], default: Char): CharSet =
    CharSet(chars.mkString, default)
  def fromSeq(chars: Seq[Char]): CharSet =
    fromSeq(chars :+ DefaultCharacter, DefaultCharacter)

  def fromCharCodeRange(from: Int, to: Int, default: Char): CharSet =
    CharSet((from to to).map(_.toChar).mkString, default)
  def fromCharCodeRange(from: Int, to: Int): CharSet =
    CharSet(((from to to).map(_.toChar) :+ DefaultCharacter).mkString, DefaultCharacter)

  def fromCharRange(start: Char, end: Char, default: Char): CharSet =
    fromCharCodeRange(start.toInt, end.toInt, default)
  def fromCharRange(start: Char, end: Char): CharSet =
    fromCharCodeRange(start.toInt, end.toInt)

  def fromUniqueString(characters: String, default: Char): CharSet =
    CharSet(characters.distinct, default)
  def fromUniqueString(characters: String): CharSet =
    CharSet((characters + DefaultCharacter).distinct, DefaultCharacter)

  val ASCII: CharSet           = fromCharCodeRange(0, 127)
  val ExtendedASCII: CharSet   = fromCharCodeRange(0, 255)
  val AlphabeticLower: CharSet = fromCharCodeRange('a'.toInt, 'z'.toInt)
  val AlphabeticUpper: CharSet = fromCharCodeRange('A'.toInt, 'Z'.toInt)
  val Alphabetic: CharSet      = fromSeq(('a' to 'z') ++ ('A' to 'Z'))
  val Numeric: CharSet         = fromCharCodeRange('0'.toInt, '9'.toInt)
  val Alphanumeric: CharSet    = fromSeq(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
  val Whitespace: CharSet      = fromString(" \t\n\r\f")
}
