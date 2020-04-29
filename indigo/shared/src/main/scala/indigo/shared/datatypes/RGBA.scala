package indigo.shared.datatypes

import indigo.shared.AsString
import indigo.shared.EqualTo
import indigo.shared.ClearColor

final class RGBA(val r: Double, val g: Double, val b: Double, val a: Double) {
  def +(other: RGBA): RGBA =
    RGBA.combine(this, other)

  def withRed(newRed: Double): RGBA =
    RGBA(newRed, g, b, a)

  def withGreen(newGreen: Double): RGBA =
    RGBA(r, newGreen, b, a)

  def withBlue(newBlue: Double): RGBA =
    RGBA(r, g, newBlue, a)

  def withAmount(amount: Double): RGBA =
    RGBA(r, g, b, amount)

  def toClearColor: ClearColor =
    ClearColor(r * a, g * a, b * a, 1)

  def toArray: Array[Float] =
    Array(r.toFloat, g.toFloat, b.toFloat, a.toFloat)

  def hash: String =
    r.toString() + g.toString() + b.toString() + a.toString()

  override def toString: String =
    implicitly[AsString[RGBA]].show(this)
}
object RGBA {

  def apply(red: Double, green: Double, blue: Double, amount: Double): RGBA =
    new RGBA(red, green, blue, amount)

  implicit val show: AsString[RGBA] = {
    val ev = implicitly[AsString[Double]]

    AsString.create { v =>
      s"RGBA(${ev.show(v.r)}, ${ev.show(v.g)}, ${ev.show(v.b)}, ${ev.show(v.a)})"
    }
  }

  implicit val eq: EqualTo[RGBA] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.r, b.r) && ev.equal(a.g, b.g) && ev.equal(a.b, b.b)
    }
  }

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

  def combine(a: RGBA, b: RGBA): RGBA =
    (a, b) match {
      case (RGBA.None, x) =>
        x
      case (x, RGBA.None) =>
        x
      case (x, y) =>
        RGBA(x.r + y.r, x.g + y.g, x.b + y.b, x.a + y.a)
    }

}

