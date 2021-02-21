package com.example.sandbox.scenes

import indigo.shared.datatypes.RGBA
import indigo.shared.materials.GLSLShader
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive._
import indigo.shared.materials.Material

final case class ShapeMaterial(fill: RGBA, stroke: RGBA, strokeWidth: Int, strokeIsInside: Boolean, useAntiAliasing: Boolean) extends Material {

  def withFillColor(newFill: RGBA): ShapeMaterial =
    this.copy(fill = newFill)

  def withStrokeColor(newStroke: RGBA): ShapeMaterial =
    this.copy(stroke = newStroke)

  def withStrokeWidth(newWidth: Int): ShapeMaterial =
    this.copy(strokeWidth = newWidth)

  def withStrokeInside(isInside: Boolean): ShapeMaterial =
    this.copy(strokeIsInside = isInside)

  def strokeInside: ShapeMaterial =
    this.copy(strokeIsInside = true)

  def strokeOutside: ShapeMaterial =
    this.copy(strokeIsInside = false)

  def withAntiAliasing(smoothEdges: Boolean): ShapeMaterial =
    this.copy(useAntiAliasing = smoothEdges)

  def smooth: ShapeMaterial =
    withAntiAliasing(true)

  def sharp: ShapeMaterial =
    withAntiAliasing(false)

  val hash: String =
    "shape" + fill.hash + stroke.hash + strokeWidth.toString() + useAntiAliasing.toString()

  def toGLSLShader: GLSLShader =
    GLSLShader(
      ShapeShaders.circleId,
      List(
        Uniform("STROKE_WIDTH") -> float(strokeWidth.toDouble),
        Uniform("SMOOTH")       -> float(if (useAntiAliasing) 1.0 else 0.0),
        Uniform("STROKE_COLOR") -> vec4(fill.r, fill.g, fill.b, fill.a),
        Uniform("FILL_COLOR")   -> vec4(stroke.r, stroke.g, stroke.b, stroke.a)
      )
    )
}
object ShapeMaterial {

  def apply(fill: RGBA): ShapeMaterial =
    ShapeMaterial(fill, RGBA.Zero, 0, false, false)

  def apply(fill: RGBA, stroke: RGBA, strokeWidth: Int): ShapeMaterial =
    ShapeMaterial(fill, stroke, strokeWidth, false, false)

}
