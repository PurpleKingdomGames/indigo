package indigo.shared.shader.library

import ultraviolet.syntax.*

import scala.annotation.nowarn

object ShapeCircle:

  trait Env extends Lighting.LightEnv {
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

      def sdfCalc(p: vec2, r: Float): Float =
        length(p) - r

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

        val sdf        = sdfCalc(env.UV - 0.5f, 0.5f - strokeWidthHalf)
        val annularSdf = abs(sdf) - strokeWidthHalf

        val fillAmount   = (1.0f - step(0.0f, sdf)) * fill.a
        val strokeAmount = (1.0f - step(0.0f, annularSdf)) * env.STROKE_COLOR.a

        val fillColor   = vec4(fill.rgb * fillAmount, fillAmount)
        val strokeColor = vec4(env.STROKE_COLOR.rgb * strokeAmount, strokeAmount)

        mix(fillColor, strokeColor, strokeAmount)

    }

object ShapeShaderFunctions:

  inline def calculateLinearGradient: (vec2, vec2, vec2, vec4, vec4) => vec4 =
    (pointA: vec2, pointB: vec2, pointP: vec2, fromColor: vec4, toColor: vec4) =>
      // `h` is the distance along the gradient 0 at A, 1 at B
      val h: Float =
        min(1.0f, max(0.0f, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)))

      mix(fromColor, toColor, h)

  inline def calculateRadialGradient: (vec2, vec2, vec2, vec4, vec4) => vec4 =
    (pointA: vec2, pointB: vec2, pointP: vec2, fromColor: vec4, toColor: vec4) =>
      val radius      = length(pointB - pointA)
      val distanceToP = length(pointP - pointA)
      val sdf         = clamp(-((distanceToP - radius) / radius), 0.0f, 1.0f)

      mix(toColor, fromColor, sdf)
