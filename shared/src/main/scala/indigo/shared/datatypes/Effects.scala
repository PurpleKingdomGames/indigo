package indigo.shared.datatypes

import indigo.shared.AsString
import indigo.shared.EqualTo
import indigo.shared.ClearColor

// Graphical effects
final class Effects(val alpha: Double, val tint: Tint, val flip: Flip) {
  def withAlpha(newAlpha: Double): Effects =
    Effects(newAlpha, tint, flip)

  def withTint(newTint: Tint): Effects =
    Effects(alpha, newTint, flip)

  def withFlip(newFlip: Flip): Effects =
    Effects(alpha, tint, newFlip)

  def hash: String =
    alpha.toString() + tint.hash + flip.hash
}
object Effects {
  val default: Effects = Effects(
    alpha = 1.0,
    tint = Tint.None,
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )

  def apply(alpha: Double, tint: Tint, flip: Flip): Effects =
    new Effects(alpha, tint, flip)
}

final class Flip(val horizontal: Boolean, val vertical: Boolean) {
  def flipH: Flip =
    new Flip(!horizontal, vertical)

  def flipV: Flip =
    new Flip(horizontal, !vertical)

  def withFlipH(value: Boolean) =
    new Flip(value, vertical)

  def withFlipV(value: Boolean) =
    new Flip(horizontal, value)

  def hash: String =
    (horizontal, vertical) match {
      case (false, false) => "00"
      case (true, false)  => "10"
      case (false, true)  => "01"
      case (true, true)   => "11"
    }
}
object Flip {
  def apply(horizontal: Boolean, vertical: Boolean): Flip =
    new Flip(horizontal, vertical)
}

final class Tint(val r: Double, val g: Double, val b: Double, val a: Double) {
  def +(other: Tint): Tint =
    Tint.combine(this, other)

  def withRed(newRed: Double): Tint =
    Tint(newRed, g, b, a)

  def withGreen(newGreen: Double): Tint =
    Tint(r, newGreen, b, a)

  def withBlue(newBlue: Double): Tint =
    Tint(r, g, newBlue, a)

  def withAmount(amount: Double): Tint =
    Tint(r, g, b, amount)

  def toClearColor: ClearColor =
    ClearColor(r * a, g * a, b * a, 1)

  def hash: String =
    r.toString() + g.toString() + b.toString() + a.toString()

  override def toString: String =
    implicitly[AsString[Tint]].show(this)
}
object Tint {

  def apply(red: Double, green: Double, blue: Double, amount: Double): Tint =
    new Tint(red, green, blue, amount)

  implicit val show: AsString[Tint] = {
    val ev = implicitly[AsString[Double]]

    AsString.create { v =>
      s"Tint(${ev.show(v.r)}, ${ev.show(v.g)}, ${ev.show(v.b)}, ${ev.show(v.a)})"
    }
  }

  implicit val eq: EqualTo[Tint] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.r, b.r) && ev.equal(a.g, b.g) && ev.equal(a.b, b.b)
    }
  }

  val Red: Tint     = Tint(1, 0, 0, 1)
  val Green: Tint   = Tint(0, 1, 0, 1)
  val Blue: Tint    = Tint(0, 0, 1, 1)
  val Yellow: Tint  = Tint(1, 1, 0, 1)
  val Magenta: Tint = Tint(1, 0, 1, 1)
  val Cyan: Tint    = Tint(0, 1, 1, 1)
  val White: Tint   = Tint(1, 1, 1, 1)
  val Black: Tint   = Tint(0, 0, 0, 1)

  val Normal: Tint = White
  val None: Tint   = White
  val Zero: Tint   = Tint(0, 0, 0, 0)

  def combine(a: Tint, b: Tint): Tint =
    (a, b) match {
      case (Tint.None, x) =>
        x
      case (x, Tint.None) =>
        x
      case (x, y) =>
        Tint(x.r + y.r, x.g + y.g, x.b + y.b, x.a + y.a)
    }

}
