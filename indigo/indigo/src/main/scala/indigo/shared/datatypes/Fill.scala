package indigo.shared.datatypes

sealed trait Fill
object Fill:

  def None: Color =
    Color(RGBA.Zero)

  final case class Color(color: RGBA) extends Fill derives CanEqual:
    def withColor(newColor: RGBA): Color =
      this.copy(color = newColor)

  object Color:
    val default: Color =
      Color(RGBA.Zero)

  final case class LinearGradient(
      fromPoint: Point,
      fromColor: RGBA,
      toPoint: Point,
      toColor: RGBA
  ) extends Fill derives CanEqual:

    def withFromColor(newColor: RGBA): LinearGradient =
      this.copy(fromColor = newColor)

    def withToColor(newColor: RGBA): LinearGradient =
      this.copy(toColor = newColor)

    def withFromPoint(newPosition: Point): LinearGradient =
      this.copy(fromPoint = newPosition)

    def withToPoint(newPosition: Point): LinearGradient =
      this.copy(toPoint = newPosition)

  object LinearGradient:
    val default: LinearGradient =
      LinearGradient(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)

  final case class RadialGradient(
      fromPoint: Point,
      fromColor: RGBA,
      toPoint: Point,
      toColor: RGBA
  ) extends Fill:

    def withFromColor(newColor: RGBA): RadialGradient =
      this.copy(fromColor = newColor)

    def withToColor(newColor: RGBA): RadialGradient =
      this.copy(toColor = newColor)

    def withFromPoint(newPosition: Point): RadialGradient =
      this.copy(fromPoint = newPosition)

    def withToPoint(newPosition: Point): RadialGradient =
      this.copy(toPoint = newPosition)

  object RadialGradient:
    val default: RadialGradient =
      RadialGradient(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)

    def apply(center: Point, radius: Int, fromColor: RGBA, toColor: RGBA): RadialGradient =
      RadialGradient(center, fromColor, center + Point(center.x + radius, center.y), toColor)

end Fill
