package indigo.gameengine.scenegraph.datatypes

// Graphical effects
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
final case class Effects(alpha: Double, tint: Tint, flip: Flip)
final case class Tint(r: Double, g: Double, b: Double) {
  def +(other: Tint): Tint =
    Tint.combine(this, other)
}
final case class Flip(horizontal: Boolean, vertical: Boolean)

object Tint {
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
