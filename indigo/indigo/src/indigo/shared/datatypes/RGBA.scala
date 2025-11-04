package indigo.shared.datatypes

final case class RGBA(r: Double, g: Double, b: Double, a: Double) derives CanEqual:
  def +(other: RGBA): RGBA =
    RGBA.combine(this, other)

  def withRed(newRed: Double): RGBA =
    this.copy(r = newRed)

  def withGreen(newGreen: Double): RGBA =
    this.copy(g = newGreen)

  def withBlue(newBlue: Double): RGBA =
    this.copy(b = newBlue)

  def withAlpha(newAlpha: Double): RGBA =
    this.copy(a = newAlpha)

  def withAmount(amount: Double): RGBA =
    withAlpha(amount)

  def makeOpaque: RGBA =
    this.copy(a = 1d)

  def makeTransparent: RGBA =
    this.copy(a = 0d)

  def mix(other: RGBA, amount: Double): RGBA = {
    val mix = Math.min(1.0, Math.max(0.0, amount))
    RGBA(
      (r * (1.0 - mix)) + (other.r * mix),
      (g * (1.0 - mix)) + (other.g * mix),
      (b * (1.0 - mix)) + (other.b * mix),
      (a * (1.0 - mix)) + (other.a * mix)
    )
  }
  def mix(other: RGBA): RGBA =
    mix(other, 0.5)

  def toRGB: RGB =
    RGB(r, g, b)

  def toHexString: String =
    val convert: Double => String = d =>
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if hex.length == 1 then "0" + hex else hex

    convert(r) + convert(g) + convert(b) + convert(a)

  def toHexString(prefix: String): String =
    prefix + toHexString

  def toArray: Array[Float] =
    Array(r.toFloat, g.toFloat, b.toFloat, a.toFloat)

object RGBA:

  val Red: RGBA     = RGBA(1, 0, 0, 1)
  val Green: RGBA   = RGBA(0, 1, 0, 1)
  val Blue: RGBA    = RGBA(0, 0, 1, 1)
  val Yellow: RGBA  = RGBA(1, 1, 0, 1)
  val Magenta: RGBA = RGBA(1, 0, 1, 1)
  val Cyan: RGBA    = RGBA(0, 1, 1, 1)
  val White: RGBA   = RGBA(1, 1, 1, 1)
  val Black: RGBA   = RGBA(0, 0, 0, 1)

  val Normal: RGBA = White
  val None: RGBA   = White
  val Zero: RGBA   = RGBA(0, 0, 0, 0)

  // https://en.wikipedia.org/wiki/X11_color_names
  val Coral: RGBA     = fromHexString("#FF7F50")
  val Crimson: RGBA   = fromHexString("#DC143C")
  val DarkBlue: RGBA  = fromHexString("#00008B")
  val Indigo: RGBA    = fromHexString("#4B0082")
  val Olive: RGBA     = fromHexString("#808000")
  val Orange: RGBA    = fromHexString("#FFA500")
  val Pink: RGBA      = fromHexString("#FFC0CB")
  val Plum: RGBA      = fromHexString("#DDA0DD")
  val Purple: RGBA    = fromHexString("#A020F0")
  val Salmon: RGBA    = fromHexString("#FA8072")
  val SeaGreen: RGBA  = fromHexString("#2E8B57")
  val Silver: RGBA    = fromHexString("#C0C0C0")
  val SlateGray: RGBA = fromHexString("#708090")
  val SteelBlue: RGBA = fromHexString("#4682B4")
  val Teal: RGBA      = fromHexString("#008080")
  val Thistle: RGBA   = fromHexString("#D8BFD8")
  val Tomato: RGBA    = fromHexString("#FF6347")

  def apply(r: Double, g: Double, b: Double): RGBA =
    RGBA(r, g, b, 1.0)

  def combine(a: RGBA, b: RGBA): RGBA =
    (a, b) match {
      case (RGBA.None, x) =>
        x
      case (x, RGBA.None) =>
        x
      case (x, y) =>
        RGBA(x.r + y.r, x.g + y.g, x.b + y.b, x.a + y.a)
    }

  def fromHexString(hex: String): RGBA =
    hex match {
      case h if h.startsWith("0x") && h.length == 10 =>
        fromColorInts(
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6, 8), 16),
          Integer.parseInt(hex.substring(8, 10), 16)
        )

      case h if h.startsWith("0x") && h.length == 8 =>
        fromColorInts(
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6, 8), 16)
        )

      case h if h.startsWith("#") && h.length == 9 =>
        fromColorInts(
          Integer.parseInt(hex.substring(1, 3), 16),
          Integer.parseInt(hex.substring(3, 5), 16),
          Integer.parseInt(hex.substring(5, 7), 16),
          Integer.parseInt(hex.substring(7, 9), 16)
        )

      case h if h.startsWith("#") && h.length == 7 =>
        fromColorInts(
          Integer.parseInt(hex.substring(1, 3), 16),
          Integer.parseInt(hex.substring(3, 5), 16),
          Integer.parseInt(hex.substring(5, 7), 16)
        )

      case h if h.length == 8 =>
        fromColorInts(
          Integer.parseInt(hex.substring(0, 2), 16),
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6, 8), 16)
        )

      case h if h.length == 6 =>
        fromColorInts(
          Integer.parseInt(hex.substring(0, 2), 16),
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4), 16)
        )

      case _ =>
        RGBA.Black
    }

  def fromColorInts(r: Int, g: Int, b: Int): RGBA =
    RGBA((1.0 / 255) * r, (1.0 / 255) * g, (1.0 / 255) * b, 1.0)

  def fromColorInts(r: Int, g: Int, b: Int, a: Int): RGBA =
    RGBA((1.0 / 255) * r, (1.0 / 255) * g, (1.0 / 255) * b, (1.0 / 255) * a)
