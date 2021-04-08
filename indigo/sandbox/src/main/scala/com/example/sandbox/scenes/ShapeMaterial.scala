package com.example.sandbox.scenes

import indigo.shared.datatypes.RGBA
import indigo.shared.materials.GLSLShader
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive._
import indigo.shared.materials.Material

final case class ShapeMaterial(fill: RGBA, stroke: RGBA, strokeWidth: Int) extends Material {

  def withFillColor(newFill: RGBA): ShapeMaterial =
    this.copy(fill = newFill)

  def withStrokeColor(newStroke: RGBA): ShapeMaterial =
    this.copy(stroke = newStroke)

  def withStrokeWidth(newWidth: Int): ShapeMaterial =
    this.copy(strokeWidth = newWidth)

  val hash: String =
    "shape" + fill.hash + stroke.hash + strokeWidth.toString()

  def toGLSLShader: GLSLShader =
    GLSLShader(
      ShapeShaders.circleId,
      List(
        Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
        Uniform("STROKE_COLOR")   -> vec4(stroke.r, stroke.g, stroke.b, stroke.a),
        Uniform("FILL_COLOR") -> vec4(fill.r, fill.g, fill.b, fill.a)
      )
    )
}
object ShapeMaterial {

  def apply(fill: RGBA): ShapeMaterial =
    ShapeMaterial(fill, RGBA.Zero, 0)

}
