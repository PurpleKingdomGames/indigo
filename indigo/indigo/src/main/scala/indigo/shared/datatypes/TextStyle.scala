package indigo.shared.datatypes

final case class TextStyle(
    font: Font,
    color: RGBA,
    stroke: TextStroke,
    alignment: TextAlign,
    baseLine: TextBaseLine,
    direction: TextDirection,
    scaleTextToFit: Boolean
):
  def withFont(newFont: Font): TextStyle =
    this.copy(font = newFont)
  def modifyFont(modifier: Font => Font): TextStyle =
    this.copy(font = modifier(font))

  def withColor(newColor: RGBA): TextStyle =
    this.copy(color = newColor)

  def withStroke(newStroke: TextStroke): TextStyle =
    this.copy(stroke = newStroke)
  def modifyStroke(modifier: TextStroke => TextStroke): TextStyle =
    this.copy(stroke = modifier(stroke))

  def withAlign(newAlign: TextAlign): TextStyle =
    this.copy(alignment = newAlign)

  def withBaseLine(newBaseLine: TextBaseLine): TextStyle =
    this.copy(baseLine = newBaseLine)

  def withDirection(newDirection: TextDirection): TextStyle =
    this.copy(direction = newDirection)

  def withScaleToFit(fitToWidth: Boolean): TextStyle =
    this.copy(scaleTextToFit = fitToWidth)
  def scaleToFit: TextStyle = withScaleToFit(true)
  def noScale: TextStyle    = withScaleToFit(false)

object TextStyle:
  def default: TextStyle =
    TextStyle(
      font = Font.default,
      color = RGBA.Black,
      stroke = TextStroke(RGBA.Zero, Pixels.zero),
      alignment = TextAlign.Left,
      baseLine = TextBaseLine.Alphabetic,
      direction = TextDirection.LeftToRight,
      scaleTextToFit = false
    )

final case class Font(
    family: FontFamily,
    size: Pixels,
    style: FontStyle,
    variant: FontVariant,
    weight: FontWeight
):
  def withFontFamily(newFamily: FontFamily): Font =
    this.copy(family = newFamily)

  def withSize(newSize: Pixels): Font =
    this.copy(size = newSize)

  def withStyle(newStyle: FontStyle): Font =
    this.copy(style = newStyle)

  def withVariant(newVariant: FontVariant): Font =
    this.copy(variant = newVariant)

  def withWeight(newWeight: FontWeight): Font =
    this.copy(weight = newWeight)

object Font:
  def apply(family: FontFamily, size: Pixels): Font =
    Font(
      family = family,
      size = size,
      style = FontStyle.Normal,
      variant = FontVariant.Normal,
      weight = FontWeight.Normal
    )

  val default: Font =
    Font(FontFamily.monospace, Pixels(8))

opaque type FontFamily = String
object FontFamily:
  def apply(family: String): FontFamily = family

  val serif: FontFamily       = FontFamily("serif")
  val sansSerif: FontFamily   = FontFamily("sans-serif")
  val monospace: FontFamily   = FontFamily("monospace")
  val cursive: FontFamily     = FontFamily("cursive")
  val fantasy: FontFamily     = FontFamily("fantasy")
  val systemUI: FontFamily    = FontFamily("system-ui")
  val uiSerif: FontFamily     = FontFamily("ui-serif")
  val uiSansSerif: FontFamily = FontFamily("ui-sans-serif")
  val uiMonospace: FontFamily = FontFamily("ui-monospace")
  val uiRounded: FontFamily   = FontFamily("ui-rounded")
  val emoji: FontFamily       = FontFamily("emoji")
  val math: FontFamily        = FontFamily("math")
  val fangsong: FontFamily    = FontFamily("fangsong")

enum FontVariant derives CanEqual:
  case Normal, SmallCaps

enum FontStyle derives CanEqual:
  case Normal, Italic

enum FontWeight derives CanEqual:
  case Normal, Bold, Lighter, Bolder

final case class TextStroke(color: RGBA, width: Pixels):
  def withColor(newColor: RGBA): TextStroke =
    this.copy(color = newColor)

  def withWidth(newWidth: Pixels): TextStroke =
    this.copy(width = newWidth)

opaque type Pixels = Int
object Pixels:
  def apply(px: Int): Pixels = px

  val zero: Pixels = Pixels(0)

  extension (px: Pixels) def toInt: Int = px

enum TextAlign derives CanEqual:
  case Left, Right, Center, Start, End

enum TextBaseLine derives CanEqual:
  case Top, Hanging, Middle, Alphabetic, Ideographic, Bottom

enum TextDirection derives CanEqual:
  case LeftToRight, RightToLeft, Inherit
