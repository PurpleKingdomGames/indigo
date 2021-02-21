package com.example.sandbox.scenes

import indigo.shared.datatypes.RGBA
import indigo.shared.materials.GLSLShader
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive._
import indigo.shared.materials.Material
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Rectangle

final case class StrokeMaterial(stroke: RGBA, strokeWidth: Int, start: Point, end: Point) extends Material {

  def withStrokeColor(newStroke: RGBA): StrokeMaterial =
    this.copy(stroke = newStroke)

  def withStrokeWidth(newWidth: Int): StrokeMaterial =
    this.copy(strokeWidth = newWidth)

  val hash: String =
    "stroke" + stroke.hash + strokeWidth.toString()

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
        Uniform("STROKE_COLOR") -> vec4(stroke.r, stroke.g, stroke.b, stroke.a),
        Uniform("START")        -> vec2(s.x.toDouble, s.y.toDouble),
        Uniform("END")          -> vec2(e.x.toDouble, e.y.toDouble)
      )
    )
  }
}
