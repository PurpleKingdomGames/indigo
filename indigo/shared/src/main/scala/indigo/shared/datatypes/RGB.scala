package indigo.shared.datatypes

import indigo.shared.EqualTo

final case class RGB(r: Double, g: Double, b: Double) {
  def +(other: RGB): RGB =
    RGB.combine(this, other)

  def withRed(newRed: Double): RGB =
    this.copy(r = newRed)

  def withGreen(newGreen: Double): RGB =
    this.copy(g = newGreen)

  def withBlue(newBlue: Double): RGB =
    this.copy(b = newBlue)

  def hash: String =
    r.toString() + g.toString() + b.toString()
}
object RGB {

  implicit val eq: EqualTo[RGB] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.r, b.r) && ev.equal(a.g, b.g) && ev.equal(a.b, b.b)
    }
  }

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

}
