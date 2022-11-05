package indigo.macroshaders

import scala.annotation.targetName

trait ShaderDSLOps extends ShaderDSLTypeExtensions:

  def length(genType: Float | vec2 | vec3 | vec4): Float =
    genType match
      case f: Float =>
        Math.sqrt(Math.pow(f, 2.0f)).toFloat

      case vec2(x, y) =>
        Math.sqrt(Math.pow(x, 2.0f) + Math.pow(y, 2.0f)).toFloat

      case vec3(x, y, z) =>
        Math.sqrt(Math.pow(x, 2.0f) + Math.pow(y, 2.0f) + Math.pow(z, 2.0f)).toFloat

      case vec4(x, y, z, w) =>
        Math
          .sqrt(
            Math.pow(x, 2.0f) +
              Math.pow(y, 2.0f) +
              Math.pow(z, 2.0f) +
              Math.pow(w, 2.0f)
          )
          .toFloat

  def step(edge: Float, x: Float): Float =
    if x < edge then 0.0f else 1.0f

  @targetName("fract_float")
  def fract(genType: Float): Float = genType
  @targetName("fract_vec2")
  def fract(genType: vec2): vec2   = genType
  def floor(genType: vec2): vec2   = genType
  def dot(a: vec2, b: vec2): Float = 0.0f
