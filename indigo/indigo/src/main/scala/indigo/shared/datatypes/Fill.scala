package indigo.shared.datatypes

import indigo.shared.collections.Batch
import indigo.shared.shader.ShaderPrimitive
import indigo.shared.shader.ShaderPrimitive.rawJSArray
import indigo.shared.shader.Uniform

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

  // GRADIENT_FROM_TO (vec4), GRADIENT_FROM_COLOR (vec4), GRADIENT_TO_COLOR (vec4),
  extension (fill: Fill)
    def toUniformData(prefix: String): Batch[(Uniform, ShaderPrimitive)] =
      fill match
        case Fill.Color(color) =>
          Batch(
            Uniform(prefix + "_GRADIENT") -> rawJSArray(
              scalajs.js.Array(
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                color.r.toFloat,
                color.g.toFloat,
                color.b.toFloat,
                color.a.toFloat,
                color.r.toFloat,
                color.g.toFloat,
                color.b.toFloat,
                color.a.toFloat
              )
            )
          )

        case Fill.LinearGradient(fromPoint, fromColor, toPoint, toColor) =>
          Batch(
            Uniform(prefix + "_GRADIENT") -> rawJSArray(
              scalajs.js.Array(
                fromPoint.x.toFloat,
                fromPoint.y.toFloat,
                toPoint.x.toFloat,
                toPoint.y.toFloat,
                fromColor.r.toFloat,
                fromColor.g.toFloat,
                fromColor.b.toFloat,
                fromColor.a.toFloat,
                toColor.r.toFloat,
                toColor.g.toFloat,
                toColor.b.toFloat,
                toColor.a.toFloat
              )
            )
          )

        case Fill.RadialGradient(fromPoint, fromColor, toPoint, toColor) =>
          Batch(
            Uniform(prefix + "_GRADIENT") -> rawJSArray(
              scalajs.js.Array(
                fromPoint.x.toFloat,
                fromPoint.y.toFloat,
                toPoint.x.toFloat,
                toPoint.y.toFloat,
                fromColor.r.toFloat,
                fromColor.g.toFloat,
                fromColor.b.toFloat,
                fromColor.a.toFloat,
                toColor.r.toFloat,
                toColor.g.toFloat,
                toColor.b.toFloat,
                toColor.a.toFloat
              )
            )
          )

end Fill
