package indigo.shared.datatypes

final case class RGBA(r: Double, g: Double, b: Double, a: Double) {
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

  def toRGB: RGB =
    RGB(r, g, b)

  def ===(other: RGBA): Boolean =
    r == other.r && g == other.g && b == other.b && a == other.a

  def toArray: Array[Float] =
    Array(r.toFloat, g.toFloat, b.toFloat, a.toFloat)

  def hash: String =
    r.toString() + g.toString() + b.toString() + a.toString()
}
object RGBA {

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
    hex.trim match {
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

}
