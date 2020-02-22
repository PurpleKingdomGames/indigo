package indigo.shared.datatypes

final class Effects(
    val tint: RGBA,
    val overlay: Overlay,
    val border: EdgeEffect,
    val glow: EdgeEffect,
    val blur: Double,
    val alpha: Double,
    val flip: Flip
) {
  def withTint(newValue: RGBA): Effects =
    Effects(newValue, overlay, border, glow, blur, alpha, flip)

  def withColorOverlay(newValue: Overlay.Color): Effects =
    Effects(tint, newValue, border, glow, blur, alpha, flip)

  def withGradiantOverlay(newValue: Overlay.LinearGradiant): Effects =
    Effects(tint, newValue, border, glow, blur, alpha, flip)

  def withBorder(newValue: EdgeEffect): Effects =
    Effects(tint, overlay, newValue, glow, blur, alpha, flip)

  def withGlow(newValue: EdgeEffect): Effects =
    Effects(tint, overlay, border, newValue, blur, alpha, flip)

  def withBlur(newValue: Double): Effects =
    Effects(tint, overlay, border, glow, newValue, alpha, flip)

  def withAlpha(newValue: Double): Effects =
    Effects(tint, overlay, border, glow, blur, newValue, flip)

  def withFlip(newValue: Flip): Effects =
    Effects(tint, overlay, border, glow, blur, alpha, newValue)

  def hash: String =
    tint.hash +
      overlay.hash +
      border.hash +
      glow.hash +
      blur.toString() +
      alpha.toString() +
      flip.hash
}
object Effects {
  val default: Effects = Effects(
    tint = RGBA.None,
    overlay = Overlay.Color.default,
    border = EdgeEffect.default,
    glow = EdgeEffect.default,
    blur = 0.0,
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
      blur: Double,
      alpha: Double,
      flip: Flip
  ): Effects =
    new Effects(
      tint,
      overlay,
      border,
      glow,
      blur,
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

final class EdgeEffect(val color: RGBA, val innerAmount: Int, val outerAmount: Int) {
  def hash: String =
    color.hash + innerAmount.toString() + outerAmount.toString()
}
object EdgeEffect {
  def apply(color: RGBA, innerAmount: Int, outerAmount: Int): EdgeEffect =
    new EdgeEffect(color, innerAmount, outerAmount)

  val default: EdgeEffect =
    EdgeEffect(RGBA.Zero, 0, 0)
}
