package indigo.shared.datatypes

import indigo.shared.AsString
import indigo.shared.EqualTo

// Graphical effects
final class Effects(val alpha: Double, val tint: Tint, val flip: Flip) {
  def withAlpha(newAlpha: Double): Effects =
    Effects(newAlpha, tint, flip)

  def withTint(newTint: Tint): Effects =
    Effects(alpha, newTint, flip)

  def withFlip(newFlip: Flip): Effects =
    Effects(alpha, tint, newFlip)
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
}
object Flip {
  def apply(horizontal: Boolean, vertical: Boolean): Flip =
    new Flip(horizontal, vertical)
}

final class Tint(val r: Double, val g: Double, val b: Double) {
  def +(other: Tint): Tint =
    Tint.combine(this, other)

  def withRed(newRed: Double): Tint =
    Tint(newRed, g, b)

  def withGree(newGreen: Double): Tint =
    Tint(r, newGreen, b)

  def withBlue(newBlue: Double): Tint =
    Tint(r, g, newBlue)
}
object Tint {

  def apply(r: Double, g: Double, b: Double): Tint =
    new Tint(r, g, b)

  implicit val show: AsString[Tint] = {
    val ev = implicitly[AsString[Double]]

    AsString.create { v =>
      s"Displayable(${ev.show(v.r)}, ${ev.show(v.g)}, ${ev.show(v.b)})"
    }
  }

  implicit val eq: EqualTo[Tint] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.r, b.r) && ev.equal(a.g, b.g) && ev.equal(a.b, b.b)
    }
  }

  val None: Tint = Tint(1, 1, 1)
  val Red: Tint = Tint(1, 0, 0)
  val Green: Tint = Tint(0, 1, 0)
  val Blue: Tint = Tint(0, 0, 1)

  def combine(a: Tint, b: Tint): Tint =
    (a, b) match {
      case (Tint.None, x) =>
        x
      case (x, Tint.None) =>
        x
      case (x, y) =>
        Tint(x.r + y.r, x.g + y.g, x.b + y.b)
    }

}
