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

  def alignLeft: TextStyle =
    withAlign(TextAlign.Left)
  def alignCenter: TextStyle =
    withAlign(TextAlign.Center)
  def alignRight: TextStyle =
    withAlign(TextAlign.Right)
  def alignStart: TextStyle =
    withAlign(TextAlign.Start)
  def alignEnd: TextStyle =
    withAlign(TextAlign.End)

  def withBaseLine(newBaseLine: TextBaseLine): TextStyle =
    this.copy(baseLine = newBaseLine)

  def withDirection(newDirection: TextDirection): TextStyle =
    this.copy(direction = newDirection)

  def withScaleToFit(fitToWidth: Boolean): TextStyle =
    this.copy(scaleTextToFit = fitToWidth)
  def scaleToFit: TextStyle = withScaleToFit(true)
  def noScale: TextStyle    = withScaleToFit(false)

  // convenience methods

  def bold: TextStyle =
    modifyFont(_.withWeight(FontWeight.Bold))
  def noBold: TextStyle =
    modifyFont(_.withWeight(FontWeight.Normal))

  def italic: TextStyle =
    modifyFont(_.withStyle(FontStyle.Italic))
  def noItalic: TextStyle =
    modifyFont(_.withStyle(FontStyle.Normal))

  def withFontFamily(newFamily: FontFamily): TextStyle =
    modifyFont(_.withFontFamily(newFamily))

  def withSize(newSize: Pixels): TextStyle =
    modifyFont(_.withSize(newSize))

  def withStyle(newStyle: FontStyle): TextStyle =
    modifyFont(_.withStyle(newStyle))

  def withVariant(newVariant: FontVariant): TextStyle =
    modifyFont(_.withVariant(newVariant))

  def withWeight(newWeight: FontWeight): TextStyle =
    modifyFont(_.withWeight(newWeight))

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
  def bold: Font =
    withWeight(FontWeight.Bold)
  def noBold: Font =
    withWeight(FontWeight.Normal)

  def italic: Font =
    withStyle(FontStyle.Italic)
  def noItalic: Font =
    withStyle(FontStyle.Normal)

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
  inline def apply(family: String): FontFamily = family

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

  extension (f: FontFamily) def name: String = f

enum FontVariant(css: String) derives CanEqual:
  def toCSS: String = css
  case Normal extends FontVariant("normal")
  case SmallCaps extends FontVariant("small-caps")

enum FontStyle(css: String) derives CanEqual:
  def toCSS: String = css
  case Normal extends FontStyle("normal")
  case Italic extends FontStyle("italic")

enum FontWeight(css: String) derives CanEqual:
  def toCSS: String = css
  case Normal extends FontWeight("normal")
  case Bold extends FontWeight("bold")
  case Lighter extends FontWeight("lighter")
  case Bolder extends FontWeight("bolder")

final case class TextStroke(color: RGBA, width: Pixels):
  def withColor(newColor: RGBA): TextStroke =
    this.copy(color = newColor)

  def withWidth(newWidth: Pixels): TextStroke =
    this.copy(width = newWidth)

opaque type Pixels = Int
object Pixels:
  inline def apply(px: Int): Pixels = px

  val zero: Pixels = Pixels(0)

  extension (px: Pixels) def toInt: Int = px

enum TextAlign(css: String) derives CanEqual:
  def toCSS: String = css
  case Left extends TextAlign("left")
  case Right extends TextAlign("right")
  case Center extends TextAlign("center")
  case Start extends TextAlign("start")
  case End extends TextAlign("end")

enum TextBaseLine(css: String) derives CanEqual:
  def toCSS: String = css
  case Top extends TextBaseLine("top")
  case Hanging extends TextBaseLine("hanging")
  case Middle extends TextBaseLine("middle")
  case Alphabetic extends TextBaseLine("alphabetic")
  case Ideographic extends TextBaseLine("ideographic")
  case Bottom extends TextBaseLine("bottom")

enum TextDirection(css: String) derives CanEqual:
  def toCSS: String = css
  case LeftToRight extends TextDirection("ltr")
  case RightToLeft extends TextDirection("rtl")
  case Inherit extends TextDirection("inherit")
