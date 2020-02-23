package indigo.shared.datatypes

final class Effects(
    val tint: RGBA,
    val overlay: Overlay,
    val border: EdgeEffect,
    val glow: EdgeEffect,
    val alpha: Double,
    val flip: Flip
) {
  def withTint(newValue: RGBA): Effects =
    Effects(newValue, overlay, border, glow, alpha, flip)

  def withColorOverlay(newValue: Overlay.Color): Effects =
    Effects(tint, newValue, border, glow, alpha, flip)

  def withGradiantOverlay(newValue: Overlay.LinearGradiant): Effects =
    Effects(tint, newValue, border, glow, alpha, flip)

  def withBorder(newValue: EdgeEffect): Effects =
    Effects(tint, overlay, newValue, glow, alpha, flip)

  def withGlow(newValue: EdgeEffect): Effects =
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
    border = EdgeEffect.default,
    glow = EdgeEffect.default,
    alpha = 1.0,
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )

  def apply(
      tint: RGBA,
      overlay: Overlay,
      border: EdgeEffect,
      glow: EdgeEffect,
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

final class EdgeEffect(val color: RGBA, val innerThickness: Thickness, val outerThickness: Thickness) {
  def hash: String =
    color.hash + innerThickness.hash + outerThickness.hash
}
object EdgeEffect {
  def apply(color: RGBA, innerThickness: Thickness, outerThickness: Thickness): EdgeEffect =
    new EdgeEffect(color, innerThickness, outerThickness)

  val default: EdgeEffect =
    EdgeEffect(RGBA.Zero, Thickness.None, Thickness.None)
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
