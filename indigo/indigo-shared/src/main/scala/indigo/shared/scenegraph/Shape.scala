package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Depth
import indigo.shared.scenegraph.SceneEntity
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Rectangle
import indigo.shared.materials.GLSLShader
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive._
import indigo.shared.datatypes.RGBA
import indigo.shared.shader.StandardShaders

sealed trait Shape extends SceneEntity
object Shape {

  final case class Box(
      dimensions: Rectangle,
      depth: Depth,
      fill: RGBA,
      strokeColor: RGBA,
      strokeWidth: Int
  ) extends Shape {

    val rotation: Radians = Radians.zero
    val scale: Vector2    = Vector2.one
    val flip: Flip        = Flip.default
    val ref: Point        = Point.zero

    private lazy val square: Int =
      Math.max(dimensions.size.x, dimensions.size.y)

    lazy val position: Point =
      dimensions.position - (Point(square) / 2) - (strokeWidth / 2)

    lazy val bounds: Rectangle =
      Rectangle(
        position,
        Point(square) + strokeWidth
      )

    def withFillColor(newFill: RGBA): Box =
      this.copy(fill = newFill)

    def withStrokeColor(newStrokeColor: RGBA): Box =
      this.copy(strokeColor = newStrokeColor)

    def withStrokeWidth(newWidth: Int): Box =
      this.copy(strokeWidth = newWidth)

    private lazy val aspect: Vector2 =
      if (bounds.size.x > bounds.size.y)
        Vector2(1.0, bounds.size.y.toDouble / bounds.size.x.toDouble)
      else
        Vector2(bounds.size.x.toDouble / bounds.size.y.toDouble, 1.0)

    def toGLSLShader: GLSLShader =
      GLSLShader(
        StandardShaders.ShapeBox.id,
        List(
          Uniform("ASPECT_RATIO") -> vec2(aspect.x, aspect.y),
          Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
          Uniform("STROKE_COLOR") -> vec4(strokeColor.r, strokeColor.g, strokeColor.b, strokeColor.a),
          Uniform("FILL_COLOR")   -> vec4(fill.r, fill.g, fill.b, fill.a)
        )
      )
  }
  object Box {

    def apply(dimensions: Rectangle, fill: RGBA): Box =
      Box(
        dimensions,
        Depth(1),
        fill,
        RGBA.Zero,
        0
      )

  }

  final case class Circle(
      center: Point,
      radius: Int,
      depth: Depth,
      fill: RGBA,
      strokeColor: RGBA,
      strokeWidth: Int
  ) extends SceneEntity {

    val rotation: Radians = Radians.zero
    val scale: Vector2    = Vector2.one
    val flip: Flip        = Flip.default
    val ref: Point        = Point.zero

    lazy val position: Point =
      center - radius - (strokeWidth / 2)

    lazy val bounds: Rectangle =
      Rectangle(
        position,
        Point(radius * 2) + strokeWidth
      )

    def withFillColor(newFill: RGBA): Circle =
      this.copy(fill = newFill)

    def withStrokeColor(newStrokeColor: RGBA): Circle =
      this.copy(strokeColor = newStrokeColor)

    def withStrokeWidth(newWidth: Int): Circle =
      this.copy(strokeWidth = newWidth)

    def toGLSLShader: GLSLShader =
      GLSLShader(
        StandardShaders.ShapeCircle.id,
        List(
          Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
          Uniform("STROKE_COLOR") -> vec4(strokeColor.r, strokeColor.g, strokeColor.b, strokeColor.a),
          Uniform("FILL_COLOR")   -> vec4(fill.r, fill.g, fill.b, fill.a)
        )
      )
  }
  object Circle {

    def apply(center: Point, radius: Int, fill: RGBA): Circle =
      Circle(
        center,
        radius,
        Depth(1),
        fill,
        RGBA.Zero,
        0
      )

  }

  final case class Line(
      start: Point,
      end: Point,
      depth: Depth,
      strokeColor: RGBA,
      strokeWidth: Int
  ) extends SceneEntity {

    val rotation: Radians = Radians.zero
    val scale: Vector2    = Vector2.one
    val flip: Flip        = Flip.default
    val ref: Point        = Point.zero

    lazy val position: Point =
      Point(
        Math.min(start.x, end.x),
        Math.min(start.y, end.y)
      ) - (strokeWidth / 2)

    lazy val bounds: Rectangle = {
      val w = Math.max(start.x, end.x) - position.x
      val h = Math.max(start.y, end.y) - position.y

      Rectangle(position, Point(Math.max(w, h)) + strokeWidth)
    }

    def withStrokeColor(newStrokeColor: RGBA): Line =
      this.copy(strokeColor = newStrokeColor)

    def withStrokeWidth(newWidth: Int): Line =
      this.copy(strokeWidth = newWidth)

    def toGLSLShader: GLSLShader = {
      val bounds: Rectangle =
        Rectangle.fromTwoPoints(start, end)

      // Relative to bounds
      val s = start - bounds.position + (strokeWidth / 2)
      val e = end - bounds.position + (strokeWidth / 2)

      GLSLShader(
        StandardShaders.ShapeLine.id,
        List(
          Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
          Uniform("STROKE_COLOR") -> vec4(strokeColor.r, strokeColor.g, strokeColor.b, strokeColor.a),
          Uniform("START")        -> vec2(s.x.toDouble, s.y.toDouble),
          Uniform("END")          -> vec2(e.x.toDouble, e.y.toDouble)
        )
      )
    }
  }
  object Line {

    def apply(start: Point, end: Point, strokeWidth: Int): Line =
      Line(
        start,
        end,
        Depth(1),
        RGBA.Black,
        strokeWidth
      )

  }

}
