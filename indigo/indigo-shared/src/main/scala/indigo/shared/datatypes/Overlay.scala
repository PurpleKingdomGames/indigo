package indigo.shared.datatypes

sealed trait Overlay {
  def hash: String
}
object Overlay {

  final case class Color(color: RGBA) extends Overlay {

    def withColor(newColor: RGBA): Color =
      this.copy(color = newColor)

    def hash: String =
      "co" + color.hash
  }
  object Color {
    val default: Color =
      Color(RGBA.Zero)
  }

  final case class LinearGradiant(
      fromPoint: Point,
      fromColor: RGBA,
      toPoint: Point,
      toColor: RGBA
  ) extends Overlay {

    def withFromColor(newColor: RGBA): LinearGradiant =
      this.copy(fromColor = newColor)

    def withToColor(newColor: RGBA): LinearGradiant =
      this.copy(toColor = newColor)

    def withFromPoint(newPosition: Point): LinearGradiant =
      this.copy(fromPoint = newPosition)

    def withToPoint(newPosition: Point): LinearGradiant =
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
  object LinearGradiant {
    val default: LinearGradiant =
      LinearGradiant(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)
  }

  final case class RadialGradiant(
      fromPoint: Point,
      fromColor: RGBA,
      toPoint: Point,
      toColor: RGBA
  ) extends Overlay {

    def withFromColor(newColor: RGBA): RadialGradiant =
      this.copy(fromColor = newColor)

    def withToColor(newColor: RGBA): RadialGradiant =
      this.copy(toColor = newColor)

    def withFromPoint(newPosition: Point): RadialGradiant =
      this.copy(fromPoint = newPosition)

    def withToPoint(newPosition: Point): RadialGradiant =
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
  object RadialGradiant {
    val default: RadialGradiant =
      RadialGradiant(Point.zero, RGBA.Zero, Point.zero, RGBA.Zero)
  }

}
