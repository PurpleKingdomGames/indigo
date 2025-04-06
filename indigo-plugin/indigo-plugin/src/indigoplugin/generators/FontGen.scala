package indigoplugin.generators

import indigoplugin.FontOptions
import indigoplugin.FontLayout
import scala.annotation.tailrec
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.font.GlyphVector

/** Provides functionality for generating font images and associated FontInfo instances.
  */
object FontGen {
  private val CharBatchSize = 2048

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

    val charDetails =
      fontOptions.layout match {
        case FontLayout.Normal(maxCharactersPerLine) =>
          val (cellWidth, cellHeight) =
            filteredCharDetails.foldLeft((0, 0)) { case ((w, h), c) =>
              (if (c.width > w) c.width else w, if (c.height > h) c.height else h)
            }

          normalLayout(
            filteredCharDetails,
            helper.getMaxAscent,
            maxCharactersPerLine,
            cellWidth,
            cellHeight
          )

        case FontLayout.Monospace(maxCharactersPerLine, cellWidth, cellHeight) =>
          monospaceLayout(
            filteredCharDetails,
            helper.getMaxAscent,
            maxCharactersPerLine,
            cellWidth,
            cellHeight
          )

        case FontLayout.IndexedGrid(maxCharactersPerLine, cellWidth, cellHeight) =>
          indexedGridLayout(
            filteredCharDetails,
            helper.getMaxAscent,
            maxCharactersPerLine,
            cellWidth,
            cellHeight
          )
      }

    // Write out FontInfo
    val default =
      charDetails
        .find(_.char == fontOptions.charSet.default)
        .getOrElse(throw new Exception(s"Couldn't find default character '${fontOptions.charSet.default.toString}'"))

    val (sheetWidth, sheetHeight) =
      fontOptions.layout match {
        case FontLayout.Normal(maxCharactersPerLine) =>
          findFontSheetBounds(charDetails)

        case l @ FontLayout.Monospace(_, _, _) =>
          (l.fontSheetWidth, l.fontSheetHeight(charDetails.length))

        case l @ FontLayout.IndexedGrid(_, _, _) =>
          (l.fontSheetWidth, l.fontSheetHeight(charDetails.map(_.code)))
      }

    val fontInfo =
      genFontInfo(moduleName, fullyQualifiedPackage, fontOptions.fontKey, sheetWidth, sheetHeight, default, charDetails)

    os.write.over(file, fontInfo)

    // Write out font image
    val outImageFileName = s"$moduleName.png"

    helper.drawFontSheet((imageOut / outImageFileName).toIO, charDetails, sheetWidth, sheetHeight, fontOptions)

    Seq(file)
  }

  def sanitiseName(name: String, ext: String): String = {
    val noExt = if (ext.nonEmpty && name.endsWith(ext)) name.dropRight(ext.length) else name
    noExt.replaceAll("[^A-Za-z0-9]", "-").split("-").map(_.capitalize).mkString
  }

  def normalLayout(
      unplacedChars: List[CharDetail],
      maxAscent: Int,
      maxCharsPerLine: Int,
      cellWidth: Int,
      cellHeight: Int
  ): List[CharDetail] = {
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
          val y    = lineCount * cellHeight
          val newC = c.copy(x = x, y = y)

          rec(cs, lineCount, charCount + 1, nextX + cellWidth, newC :: acc)
      }

    rec(unplacedChars, 0, 0, 0, Nil)
  }

  def monospaceLayout(
      unplacedChars: List[CharDetail],
      maxAscent: Int,
      maxCharsPerLine: Int,
      cellWidth: Int,
      cellHeight: Int
  ): List[CharDetail] = {
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
          val x    = nextX + ((cellWidth - c.width) / 2) // Center the character in the cell
          val y    = lineCount * cellHeight - (cellHeight - maxAscent)
          val newC = c.copy(x = x, y = y)

          rec(cs, lineCount, charCount + 1, nextX + cellWidth, newC :: acc)
      }

    rec(unplacedChars, 0, 0, 0, Nil)
  }

  def indexedGridLayout(
      unplacedChars: List[CharDetail],
      maxAscent: Int,
      maxCharsPerLine: Int,
      cellWidth: Int,
      cellHeight: Int
  ): List[CharDetail] = {
    @tailrec
    def rec(
        remaining: List[CharDetail],
        acc: List[CharDetail]
    ): List[CharDetail] =
      remaining match {
        case Nil =>
          acc.reverse

        case c :: cs =>
          val x    = (c.code % maxCharsPerLine) * cellWidth
          val y    = ((c.code / maxCharsPerLine) * cellHeight) - (cellHeight - maxAscent)
          val newC = c.copy(x = x, y = y)

          rec(cs, newC :: acc)
      }

    rec(unplacedChars, Nil)
  }

  def findFontSheetBounds(charDetails: List[CharDetail]): (Int, Int) =
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
    val charBatches = chars
      .sliding(CharBatchSize, CharBatchSize)
      .zipWithIndex
      .map { case (batch, index) =>
        val chars = batch
          .map(cd => "    " + toFontChar(cd) + ",")
          .mkString("\n")
          .dropRight(1) // Drops the last ','
        // language=scala
        s"""private object CharBatch$index {
           |  val batch = Batch(
           |    ${chars}
           |  )
           |}
           |""".stripMargin
      }
      .mkString("\n")
    val charBatchAdditions = (0 until (chars.length / CharBatchSize) + 1)
      .map(index => s"      .addChars(CharBatch${index}.batch)")
      .mkString("\n")

    val dx = default.x.toString
    val dy = default.y.toString
    val dw = default.width.toString
    val dh = default.height.toString

    // language=scala
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
    |    ${charBatchAdditions}
    |}
    |
    |$charBatches
    |
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
        .deriveFont(Font.PLAIN, fontSize.toFloat)

    Helper(font)
  }

  final case class Helper(font: Font) {

    def getCharBounds(char: Char): (Int, Int, Int) = {
      val tmpBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)

      val tmpG2d = tmpBuffer.createGraphics()
      tmpG2d.setFont(font)

      val frc         = tmpG2d.getFontRenderContext()
      val fontMetrics = tmpG2d.getFontMetrics()
      val gv          = font.createGlyphVector(frc, Array(char))

      val w = gv.getGlyphMetrics(0).getAdvance.toInt
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

    def getMaxAscent: Int = {
      val tmpBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
      val tmpG2d    = tmpBuffer.createGraphics()
      tmpG2d.setFont(font)
      val fontMetrics = tmpG2d.getFontMetrics()

      fontMetrics.getMaxAscent()
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

      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

      g2d.setFont(font)
      g2d.setColor(fontOptions.color.toColor)

      if (fontOptions.antiAlias) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        charDetails.foreach { c =>
          g2d.drawString(c.char.toString, c.x, c.y + c.ascent)
        }
      } else {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)

        val frc: FontRenderContext = new FontRenderContext(null, false, false)

        charDetails.foreach { c =>
          val gv    = font.createGlyphVector(frc, c.char.toString)
          val shape = gv.getOutline(c.x.toFloat, (c.y + c.ascent).toFloat)

          g2d.fill(shape)
        }
      }

      g2d.dispose()

      ImageIO.write(bufferedImage, "PNG", outFile)

      ()
    }

  }

}

final case class CharDetail(char: Char, code: Int, x: Int, y: Int, width: Int, height: Int, ascent: Int)
