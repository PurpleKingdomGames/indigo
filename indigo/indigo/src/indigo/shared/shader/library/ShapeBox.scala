package indigo.shared.shader.library

import ultraviolet.syntax.*

import scala.annotation.nowarn

object ShapeBox:

  trait Env extends Lighting.LightEnv {
    val ASPECT_RATIO: vec2        = vec2(0.0f)
    val STROKE_WIDTH: Float       = 0.0f
    val FILL_TYPE: Float          = 0.0f
    val STROKE_COLOR: vec4        = vec4(0.0f)
    val GRADIENT_FROM_TO: vec4    = vec4(0.0f)
    val GRADIENT_FROM_COLOR: vec4 = vec4(0.0f)
    val GRADIENT_TO_COLOR: vec4   = vec4(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoShapeData(
      ASPECT_RATIO: vec2,
      STROKE_WIDTH: Float,
      FILL_TYPE: Float,
      STROKE_COLOR: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      import ShapeShaderFunctions.*

      // Delegates
      val _calculateLinearGradient: (vec2, vec2, vec2, vec4, vec4) => vec4 =
        calculateLinearGradient
      val _calculateRadialGradient: (vec2, vec2, vec2, vec4, vec4) => vec4 =
        calculateRadialGradient

      ubo[IndigoShapeData]

      // Borrowed with thanks! https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
      def sdfCalc(p: vec2, b: vec2): Float =
        val d = abs(p) - b
        length(max(d, 0.0f)) + min(max(d.x, d.y), 0.0f)

      def fragment(color: vec4): vec4 =
        val strokeWidthHalf = max(0.0f, env.STROKE_WIDTH / env.SIZE.x / 2.0f)

        val fillType = round(env.FILL_TYPE).toInt
        val fill: vec4 =
          fillType match
            case 1 =>
              _calculateLinearGradient(
                env.GRADIENT_FROM_TO.xy,
                env.GRADIENT_FROM_TO.zw,
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case 2 =>
              _calculateRadialGradient(
                env.GRADIENT_FROM_TO.xy,
                env.GRADIENT_FROM_TO.zw,
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case _ =>
              env.GRADIENT_FROM_COLOR

        val sdf        = sdfCalc(env.UV - 0.5f, (vec2(0.5f) * env.ASPECT_RATIO) - strokeWidthHalf)
        val annularSdf = abs(sdf) - strokeWidthHalf

        val fillAmount   = (1.0f - step(0.0f, sdf)) * fill.a
        val strokeAmount = (1.0f - step(0.0f, annularSdf)) * env.STROKE_COLOR.a

        val fillColor   = vec4(fill.rgb * fillAmount, fillAmount)
        val strokeColor = vec4(env.STROKE_COLOR.rgb * strokeAmount, strokeAmount)

        mix(fillColor, strokeColor, strokeAmount)
    }
