package indigoplugin

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
