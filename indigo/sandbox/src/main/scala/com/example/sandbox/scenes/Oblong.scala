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

final case class Oblong(
    dimensions: Rectangle,
    depth: Depth,
    fill: RGBA,
    strokeColor: RGBA,
    strokeWidth: Int
) extends SceneEntity {

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

  def withFillColor(newFill: RGBA): Oblong =
    this.copy(fill = newFill)

  def withStrokeColor(newStrokeColor: RGBA): Oblong =
    this.copy(strokeColor = newStrokeColor)

  def withStrokeWidth(newWidth: Int): Oblong =
    this.copy(strokeWidth = newWidth)

  private lazy val aspect: Vector2 =
    if (bounds.size.x > bounds.size.y)
      Vector2(1.0, bounds.size.y.toDouble / bounds.size.x.toDouble)
    else
      Vector2(bounds.size.x.toDouble / bounds.size.y.toDouble, 1.0)

  def toGLSLShader: GLSLShader =
    GLSLShader(
      ShapeShaders.oblongId,
      List(
        Uniform("ASPECT_RATIO") -> vec2(aspect.x, aspect.y),
        Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
        Uniform("STROKE_COLOR") -> vec4(strokeColor.r, strokeColor.g, strokeColor.b, strokeColor.a),
        Uniform("FILL_COLOR")   -> vec4(fill.r, fill.g, fill.b, fill.a)
      )
    )
}
object Oblong {

  def apply(dimensions: Rectangle, fill: RGBA): Oblong =
    Oblong(
      dimensions,
      Depth(1),
      fill,
      RGBA.Zero,
      0
    )

}
