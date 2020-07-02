package indigo.shared.datatypes

final class Effects(
    val tint: RGBA,
    val overlay: Overlay,
    val border: Border,
    val glow: Glow,
    val alpha: Double,
    val flip: Flip
) {
  def withTint(newValue: RGBA): Effects =
    Effects(newValue, overlay, border, glow, alpha, flip)

  def withOverlay(newOverlay: Overlay): Effects =
    Effects(tint, newOverlay, border, glow, alpha, flip)

  def withBorder(newValue: Border): Effects =
    Effects(tint, overlay, newValue, glow, alpha, flip)

  def withGlow(newValue: Glow): Effects =
    Effects(tint, overlay, border, newValue, alpha, flip)

  def withAlpha(newValue: Double): Effects =
    Effects(tint, overlay, border, glow, newValue, flip)

  def withFlip(newValue: Flip): Effects =
    Effects(tint, overlay, border, glow, alpha, newValue)

  def hash: String =
    tint.hash +
      overlay.hash +
      border.hash +
      glow.hash +
      alpha.toString() +
      flip.hash
}
object Effects {
  val default: Effects = Effects(
    tint = RGBA.None,
    overlay = Overlay.Color.default,
    border = Border.default,
    glow = Glow.default,
    alpha = 1.0,
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )

  def apply(
      tint: RGBA,
      overlay: Overlay,
      border: Border,
      glow: Glow,
      alpha: Double,
      flip: Flip
  ): Effects =
    new Effects(
      tint,
      overlay,
      border,
      glow,
      alpha,
      flip
    )
}

sealed trait Overlay {
  def hash: String
}
object Overlay {

  final class Color(val color: RGBA) extends Overlay {
    def hash: String =
      color.hash
  }
  object Color {
    def apply(color: RGBA): Color =
      new Color(color)

    def unapply(c: Color): Option[RGBA] =
      Some(c.color)

    val default: Color =
      new Color(RGBA.Zero)
  }

  final class LinearGradiant(
      val fromPoint: Point,
      val fromColor: RGBA,
      val toPoint: Point,
      val toColor: RGBA
  ) extends Overlay {
    lazy val hash: String =
      fromPoint.x.toString +
        fromPoint.y.toString +
        fromColor.hash +
        toPoint.x.toString +
        toPoint.y.toString +
        toColor.hash
  }
  object LinearGradiant {
    def apply(fromPoint: Point, fromColor: RGBA, toPoint: Point, toColor: RGBA): LinearGradiant =
      new LinearGradiant(fromPoint, fromColor, toPoint, toColor)

    def unapply(lg: LinearGradiant): Option[(Point, RGBA, Point, RGBA)] =
      Some((lg.fromPoint, lg.fromColor, lg.toPoint, lg.toColor))

    val default: LinearGradiant =
      LinearGradiant(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)
  }
}

final class Border(val color: RGBA, val innerThickness: Thickness, val outerThickness: Thickness) {

  def withInnerThickness(thickness: Thickness): Border =
    new Border(color, thickness, outerThickness)

  def withOuterThickness(thickness: Thickness): Border =
    new Border(color, innerThickness, thickness)

  def hash: String =
    color.hash + innerThickness.hash + outerThickness.hash
}
object Border {
  def apply(color: RGBA, innerThickness: Thickness, outerThickness: Thickness): Border =
    new Border(color, innerThickness, outerThickness)

  def inside(color: RGBA): Border =
    new Border(color, Thickness.Thin, Thickness.None)

  def outside(color: RGBA): Border =
    new Border(color, Thickness.None, Thickness.Thin)

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

final class Glow(val color: RGBA, val innerGlowAmount: Double, val outerGlowAmount: Double) {
  def withColor(newColor: RGBA): Glow =
    new Glow(newColor, innerGlowAmount, outerGlowAmount)

  def withInnerGlowAmount(amount: Double): Glow =
    new Glow(color, Math.max(0, amount), outerGlowAmount)

  def withOuterGlowAmount(amount: Double): Glow =
    new Glow(color, innerGlowAmount, Math.max(0, amount))

  def hash: String =
    color.hash + innerGlowAmount.toString + outerGlowAmount.toString()
}
object Glow {
  def apply(color: RGBA, innerGlowAmount: Double, outerGlowAmount: Double): Glow =
    new Glow(color, innerGlowAmount, outerGlowAmount)

  def inside(color: RGBA): Glow =
    new Glow(color, 1d, 0d)

  def outside(color: RGBA): Glow =
    new Glow(color, 0d, 1d)

  val default: Glow =
    Glow(RGBA.Zero, 0d, 0d)
}
