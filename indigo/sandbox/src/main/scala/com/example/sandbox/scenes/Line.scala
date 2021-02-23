package com.example.sandbox.scenes

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
      ShapeShaders.lineId,
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
