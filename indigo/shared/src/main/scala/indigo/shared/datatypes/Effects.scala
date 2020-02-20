package indigo.shared.datatypes

final class Effects(
    val tint: RGBA,
    val colorOverlay: RGBA,
    val gradiantOverlay: LinearGradiantOverlay,
    val outerBorder: ColorAmount,
    val innerBorder: ColorAmount,
    val outerGlow: ColorAmount,
    val innerGlow: ColorAmount,
    val blur: Double,
    val alpha: Double,
    val flip: Flip
) {
  def withTint(newValue: RGBA): Effects =
    Effects(newValue, colorOverlay, gradiantOverlay, outerBorder, innerBorder, outerGlow, innerGlow, blur, alpha, flip)

  def withColorOverlay(newValue: RGBA): Effects =
    Effects(tint, newValue, gradiantOverlay, outerBorder, innerBorder, outerGlow, innerGlow, blur, alpha, flip)

  def withGradiantOverlay(newValue: LinearGradiantOverlay): Effects =
    Effects(tint, colorOverlay, newValue, outerBorder, innerBorder, outerGlow, innerGlow, blur, alpha, flip)

  def withOuterBorder(newValue: ColorAmount): Effects =
    Effects(tint, colorOverlay, gradiantOverlay, newValue, innerBorder, outerGlow, innerGlow, blur, alpha, flip)

  def withInnerBorder(newValue: ColorAmount): Effects =
    Effects(tint, colorOverlay, gradiantOverlay, outerBorder, newValue, outerGlow, innerGlow, blur, alpha, flip)

  def withOuterGlow(newValue: ColorAmount): Effects =
    Effects(tint, colorOverlay, gradiantOverlay, outerBorder, innerBorder, newValue, innerGlow, blur, alpha, flip)

  def withInnerGlow(newValue: ColorAmount): Effects =
    Effects(tint, colorOverlay, gradiantOverlay, outerBorder, innerBorder, outerGlow, newValue, blur, alpha, flip)

  def withBlur(newValue: Double): Effects =
    Effects(tint, colorOverlay, gradiantOverlay, outerBorder, innerBorder, outerGlow, innerGlow, newValue, alpha, flip)

  def withAlpha(newValue: Double): Effects =
    Effects(tint, colorOverlay, gradiantOverlay, outerBorder, innerBorder, outerGlow, innerGlow, blur, newValue, flip)

  def withFlip(newValue: Flip): Effects =
    Effects(tint, colorOverlay, gradiantOverlay, outerBorder, innerBorder, outerGlow, innerGlow, blur, alpha, newValue)

  def hash: String =
    tint.hash +
      colorOverlay.hash +
      gradiantOverlay.hash +
      outerBorder.hash +
      innerBorder.hash +
      outerGlow.hash +
      innerGlow.hash +
      blur.toString() +
      alpha.toString() +
      flip.hash
}
object Effects {
  val default: Effects = Effects(
    tint = RGBA.None,
    colorOverlay = RGBA.Zero,
    gradiantOverlay = LinearGradiantOverlay.default,
    outerBorder = ColorAmount.default,
    innerBorder = ColorAmount.default,
    outerGlow = ColorAmount.default,
    innerGlow = ColorAmount.default,
    blur = 0.0,
    alpha = 1.0,
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )

  def apply(
      tint: RGBA,
      colorOverlay: RGBA,
      gradiantOverlay: LinearGradiantOverlay,
      outerBorder: ColorAmount,
      innerBorder: ColorAmount,
      outerGlow: ColorAmount,
      innerGlow: ColorAmount,
      blur: Double,
      alpha: Double,
      flip: Flip
  ): Effects =
    new Effects(
      tint,
      colorOverlay,
      gradiantOverlay,
      outerBorder,
      innerBorder,
      outerGlow,
      innerGlow,
      blur,
      alpha,
      flip
    )
}

final class LinearGradiantOverlay(
    val fromPoint: Point,
    val fromColor: RGBA,
    val toPoint: Point,
    val toColor: RGBA
) {

  def hash: String =
    fromPoint.x.toString +
      fromPoint.y.toString +
      fromColor.hash +
      toPoint.x.toString +
      toPoint.y.toString +
      toColor.hash
}
object LinearGradiantOverlay {
  def apply(fromPoint: Point, fromColor: RGBA, toPoint: Point, toColor: RGBA): LinearGradiantOverlay =
    new LinearGradiantOverlay(fromPoint, fromColor, toPoint, toColor)

  val default: LinearGradiantOverlay =
    LinearGradiantOverlay(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)
}

final class ColorAmount(val color: RGBA, val amount: Double) {
  def hash: String =
    color.hash + amount.toString()
}
object ColorAmount {
  def apply(color: RGBA, amount: Double): ColorAmount =
    new ColorAmount(color, amount)

  val default: ColorAmount =
    ColorAmount(RGBA.Zero, 0.0)
}
