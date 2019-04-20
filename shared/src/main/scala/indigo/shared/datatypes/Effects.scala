package indigo.shared.datatypes

import indigo.shared.AsString
import indigo.shared.EqualTo

// Graphical effects
final case class Effects(alpha: Double, tint: Tint, flip: Flip)
object Effects {
  val default: Effects = Effects(
    alpha = 1.0,
    tint = Tint.None,
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )
}

final case class Flip(horizontal: Boolean, vertical: Boolean)

final case class Tint(r: Double, g: Double, b: Double) {
  def +(other: Tint): Tint =
    Tint.combine(this, other)
}
object Tint {

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
