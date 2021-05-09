package indigo.platform.assets

import indigo.facades.IndigoCanvasRenderingContext2D
import indigo.facades.IndigoTextMetrics

import indigo.shared.datatypes.TextStyle
import indigo.shared.datatypes.TextAlign
import indigo.shared.datatypes.TextBaseLine
import indigo.shared.datatypes.TextDirection
import indigo.shared.datatypes.Font
import indigo.shared.datatypes.FontFamily
import indigo.shared.datatypes.Pixels
import indigo.shared.datatypes.FontStyle
import indigo.shared.datatypes.FontVariant
import indigo.shared.datatypes.FontWeight
import indigo.shared.datatypes.Rectangle

import org.scalajs.dom.html
import org.scalajs.dom
import org.scalajs.dom.raw
import scala.scalajs.js.Dynamic
import scala.scalajs.js.undefined

class DynamicText:

  private lazy val textContext: IndigoCanvasRenderingContext2D = createTextContext()

  private val toFontStatement: Font => String = f =>
    val style = f.style match
      case FontStyle.Normal => "normal"
      case FontStyle.Italic => "italic"

    val variant = f.variant match
      case FontVariant.Normal    => "normal"
      case FontVariant.SmallCaps => "small-caps"

    val weight = f.weight match
      case FontWeight.Normal  => "normal"
      case FontWeight.Bold    => "bold"
      case FontWeight.Lighter => "lighter"
      case FontWeight.Bolder  => "bolder"

    s"$style $variant $weight ${f.size.toInt}px ${f.family.name}"

  private def setupText(text: String, style: TextStyle, width: Int, height: Int): Unit =
    textContext.canvas.width = width
    textContext.canvas.height = height

    textContext.font = toFontStatement(style.font) //"normal 14px monospace" // bold 48px serif

    textContext.textAlign = style.alignment match
      case TextAlign.Left   => "left"
      case TextAlign.Right  => "right"
      case TextAlign.Center => "center"
      case TextAlign.Start  => "start"
      case TextAlign.End    => "end"

    textContext.textBaseline = style.baseLine match
      case TextBaseLine.Top         => "top"
      case TextBaseLine.Hanging     => "hanging"
      case TextBaseLine.Middle      => "middle"
      case TextBaseLine.Alphabetic  => "alphabetic"
      case TextBaseLine.Ideographic => "ideographic"
      case TextBaseLine.Bottom      => "bottom"

    textContext.direction = style.direction match
      case TextDirection.LeftToRight => "ltr"
      case TextDirection.RightToLeft => "rtl"
      case TextDirection.Inherit     => "inherit"

    textContext.fillStyle = style.color.toHexString("#")

    textContext.strokeStyle = style.stroke.color.toHexString("#")
    textContext.lineWidth = style.stroke.width.toInt

    textContext.clearRect(0, 0, width, height)

  def createTextContext(): IndigoCanvasRenderingContext2D =
    dom.document
      .createElement("canvas")
      .asInstanceOf[html.Canvas]
      .getContext("2d", Dynamic.literal())
      .asInstanceOf[IndigoCanvasRenderingContext2D]

  def makeTextImageData(text: String, style: TextStyle, width: Int, height: Int): raw.HTMLCanvasElement =
    setupText(text, style, width, height)

    val x = 0
    val y = style.font.size.toInt

    if style.scaleTextToFit then
      textContext.strokeText(text, x, y, width)
      textContext.fillText(text, x, y, width)
    else
      textContext.strokeText(text, x, y)
      textContext.fillText(text, x, y)

    textContext.canvas

  def measureText(text: String, style: TextStyle, width: Int, height: Int): Rectangle =
    setupText(text, style, width, height)

    val textMetrics: IndigoTextMetrics =
      textContext.measureText(text).asInstanceOf[IndigoTextMetrics]

    Rectangle(
      x = 0,
      y = 0,
      width = (Math.abs(textMetrics.actualBoundingBoxLeft) + Math.abs(textMetrics.actualBoundingBoxRight)).toInt,
      height = (Math.abs(textMetrics.actualBoundingBoxAscent) + Math.abs(textMetrics.actualBoundingBoxAscent)).toInt
    )
