package indigo.shared.shader.library

import ultraviolet.syntax.*

import scala.annotation.nowarn

object ShapeLine:

  trait Env extends Lighting.LightEnv {
    val STROKE_WIDTH: Float = 0.0f
    val STROKE_COLOR: vec4  = vec4(0.0f)
    val START: vec2         = vec2(0.0f)
    val END: vec2           = vec2(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoShapeData(
      STROKE_WIDTH: Float,
      STROKE_COLOR: vec4,
      START: vec2,
      END: vec2
  )

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      ubo[IndigoShapeData]

      def sdfCalc(p: vec2, a: vec2, b: vec2): Float =
        val pa = p - a
        val ba = b - a
        val h  = clamp(dot(pa, ba) / dot(ba, ba), 0.0f, 1.0f)
        length(pa - ba * h)

      def fragment(color: vec4): vec4 =
        val strokeWidthHalf = max(0.0f, env.STROKE_WIDTH / env.SIZE.x / 2.0f)
        val sdf             = sdfCalc(env.UV, env.START / env.SIZE, env.END / env.SIZE)
        val annularSdf      = sdf - strokeWidthHalf
        val strokeAmount    = (1.0f - step(0.0f, annularSdf)) * env.STROKE_COLOR.w
        vec4(env.STROKE_COLOR.xyz * strokeAmount, strokeAmount)
    }
