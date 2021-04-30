package indigo.shared.datatypes

final case class RGB(r: Double, g: Double, b: Double) derives CanEqual {
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

  def toRGBA: RGBA =
    RGBA(r, g, b, 1.0)

  def toArray: Array[Float] =
    Array(r.toFloat, g.toFloat, b.toFloat)

  def hash: String =
    r.toString() + g.toString() + b.toString()
}
object RGB {

  val Red: RGB     = RGB(1, 0, 0)
  val Green: RGB   = RGB(0, 1, 0)
  val Blue: RGB    = RGB(0, 0, 1)
  val Yellow: RGB  = RGB(1, 1, 0)
  val Magenta: RGB = RGB(1, 0, 1)
  val Cyan: RGB    = RGB(0, 1, 1)
  val White: RGB   = RGB(1, 1, 1)
  val Black: RGB   = RGB(0, 0, 0)

  val Normal: RGB = White
  val None: RGB   = White
  val Zero: RGB   = RGB(0, 0, 0)

  def combine(a: RGB, b: RGB): RGB =
    (a, b) match {
      case (RGB.None, x) =>
        x
      case (x, RGB.None) =>
        x
      case (x, y) =>
        RGB(x.r + y.r, x.g + y.g, x.b + y.b)
    }

  def fromHexString(hex: String): RGB =
    RGBA.fromHexString(hex).toRGB

  def fromColorInts(r: Int, g: Int, b: Int): RGB =
    RGBA.fromColorInts(r, g, b).toRGB

}
