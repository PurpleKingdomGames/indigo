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
      ShapeShaders.circleId,
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
