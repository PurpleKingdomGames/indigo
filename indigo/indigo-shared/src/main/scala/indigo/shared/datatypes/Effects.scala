package indigo.shared.datatypes

final case class Effects(
    tint: RGBA,
    overlay: Overlay,
    border: Border,
    glow: Glow,
    alpha: Double
) {
  def withTint(newTint: RGBA): Effects =
    this.copy(tint = newTint)

  def withOverlay(newOverlay: Overlay): Effects =
    this.copy(overlay = newOverlay)

  def withBorder(newBorder: Border): Effects =
    this.copy(border = newBorder)

  def withGlow(newGlow: Glow): Effects =
    this.copy(glow = newGlow)

  def withAlpha(newAlpha: Double): Effects =
    this.copy(alpha = newAlpha)

  def hash: String =
    tint.hash +
      overlay.hash +
      border.hash +
      glow.hash +
      alpha.toString()
}
object Effects {
  val default: Effects = Effects(
    tint = RGBA.None,
    overlay = Overlay.Color.default,
    border = Border.default,
    glow = Glow.default,
    alpha = 1.0
  )

}

final case class Border(color: RGBA, innerThickness: Thickness, outerThickness: Thickness) {

  def withColor(newColor: RGBA): Border =
    this.copy(color = newColor)

  def withInnerThickness(thickness: Thickness): Border =
    this.copy(innerThickness = thickness)

  def withOuterThickness(thickness: Thickness): Border =
    this.copy(outerThickness = thickness)

  def hash: String =
    color.hash + innerThickness.hash + outerThickness.hash
}
object Border {
  def inside(color: RGBA): Border =
    Border(color, Thickness.Thin, Thickness.None)

  def outside(color: RGBA): Border =
    Border(color, Thickness.None, Thickness.Thin)

  val default: Border =
    Border(RGBA.Zero, Thickness.None, Thickness.None)
}

sealed trait Thickness {
  def hash: String =
    (this match {
      case Thickness.None  => 0
      case Thickness.Thin  => 1
      case Thickness.Thick => 2
    }).toString()
}
object Thickness {
  case object None  extends Thickness
  case object Thin  extends Thickness
  case object Thick extends Thickness
}

final case class Glow(color: RGBA, innerGlowAmount: Double, outerGlowAmount: Double) {
  def withColor(newColor: RGBA): Glow =
    this.copy(color = newColor)

  def withInnerGlowAmount(amount: Double): Glow =
    this.copy(innerGlowAmount = Math.max(0, amount))

  def withOuterGlowAmount(amount: Double): Glow =
    this.copy(outerGlowAmount = Math.max(0, amount))

  def hash: String =
    color.hash + innerGlowAmount.toString + outerGlowAmount.toString()
}
object Glow {
  def inside(color: RGBA): Glow =
    Glow(color, 1d, 0d)

  def outside(color: RGBA): Glow =
    Glow(color, 0d, 1d)

  val default: Glow =
    Glow(RGBA.Zero, 0d, 0d)
}
