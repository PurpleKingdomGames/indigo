package indigo.shared.datatypes


final class Effects(val alpha: Double, val tint: RGBA, val flip: Flip) {
  def withAlpha(newAlpha: Double): Effects =
    Effects(newAlpha, tint, flip)

  def withTint(newTint: RGBA): Effects =
    Effects(alpha, newTint, flip)

  def withFlip(newFlip: Flip): Effects =
    Effects(alpha, tint, newFlip)

  def hash: String =
    alpha.toString() + tint.hash + flip.hash
}
object Effects {
  val default: Effects = Effects(
    alpha = 1.0,
    tint = RGBA.None,
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )

  def apply(alpha: Double, tint: RGBA, flip: Flip): Effects =
    new Effects(alpha, tint, flip)
}
