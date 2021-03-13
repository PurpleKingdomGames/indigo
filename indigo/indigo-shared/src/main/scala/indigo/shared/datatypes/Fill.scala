package indigo.shared.datatypes

sealed trait Fill {
  def hash: String
}
object Fill {

  final case class Color(color: RGBA) extends Fill {

    def withColor(newColor: RGBA): Color =
      this.copy(color = newColor)

    def hash: String =
      "co" + color.hash
  }
  object Color {
    val default: Color =
      Color(RGBA.Zero)
  }

  final case class LinearGradient(
      fromPoint: Point,
      fromColor: RGBA,
      toPoint: Point,
      toColor: RGBA
  ) extends Fill {

    def withFromColor(newColor: RGBA): LinearGradient =
      this.copy(fromColor = newColor)

    def withToColor(newColor: RGBA): LinearGradient =
      this.copy(toColor = newColor)

    def withFromPoint(newPosition: Point): LinearGradient =
      this.copy(fromPoint = newPosition)

    def withToPoint(newPosition: Point): LinearGradient =
      this.copy(toPoint = newPosition)

    lazy val hash: String =
      "lgo" +
        fromPoint.x.toString +
        fromPoint.y.toString +
        fromColor.hash +
        toPoint.x.toString +
        toPoint.y.toString +
        toColor.hash
  }
  object LinearGradient {
    val default: LinearGradient =
      LinearGradient(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)
  }

  final case class RadialGradient(
      fromPoint: Point,
      fromColor: RGBA,
      toPoint: Point,
      toColor: RGBA
  ) extends Fill {

    def withFromColor(newColor: RGBA): RadialGradient =
      this.copy(fromColor = newColor)

    def withToColor(newColor: RGBA): RadialGradient =
      this.copy(toColor = newColor)

    def withFromPoint(newPosition: Point): RadialGradient =
      this.copy(fromPoint = newPosition)

    def withToPoint(newPosition: Point): RadialGradient =
      this.copy(toPoint = newPosition)

    lazy val hash: String =
      "rgo" +
        fromPoint.x.toString +
        fromPoint.y.toString +
        fromColor.hash +
        toPoint.x.toString +
        toPoint.y.toString +
        toColor.hash
  }
  object RadialGradient {
    val default: RadialGradient =
      RadialGradient(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)

    def apply(center: Point, radius: Int, fromColor: RGBA, toColor: RGBA): RadialGradient =
      RadialGradient(center, fromColor, center + Point(center.x + radius, center.y), toColor)
  }

}
