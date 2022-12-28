package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object ShapeCircle:

  case class IndigoShapeData(
      STROKE_WIDTH: Float,
      FILL_TYPE: Float,
      STROKE_COLOR: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  object fragment:
    inline def shader =
      Shader[IndigoFragmentEnv & IndigoShapeData] { env =>
        ubo[IndigoShapeData]

        def sdfCalc(p: vec2, r: Float): Float =
          length(p) - r

        def calculateColor: vec4 =
          env.GRADIENT_FROM_COLOR

        def calculateLinearGradient: vec4 =
          val pointA = env.GRADIENT_FROM_TO.xy
          val pointB = env.GRADIENT_FROM_TO.zw
          val pointP = env.UV * env.SIZE

          // `h` is the distance along the gradient 0 at A, 1 at B
          val h: Float =
            min(1.0f, max(0.0f, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)))

          mix(env.GRADIENT_FROM_COLOR, env.GRADIENT_TO_COLOR, h)

        def calculateRadialGradient: vec4 =
          val pointA = env.GRADIENT_FROM_TO.xy
          val pointB = env.GRADIENT_FROM_TO.zw
          val pointP = env.UV * env.SIZE

          val radius      = length(pointB - pointA)
          val distanceToP = length(pointP - pointA)

          val sdf = clamp(-((distanceToP - radius) / radius), 0.0f, 1.0f)

          mix(env.GRADIENT_TO_COLOR, env.GRADIENT_FROM_COLOR, sdf)

        def fragment: vec4 =

          val strokeWidthHalf = max(0.0f, env.STROKE_WIDTH / env.SIZE.x / 2.0f)

          val fillType = round(env.FILL_TYPE).toInt
          val fill: vec4 =
            fillType match
              case 0 => calculateColor
              case 1 => calculateLinearGradient
              case 2 => calculateRadialGradient
              case _ => calculateColor

          val sdf        = sdfCalc(env.UV - 0.5f, 0.5f - strokeWidthHalf)
          val annularSdf = abs(sdf) - strokeWidthHalf

          val fillAmount   = (1.0f - step(0.0f, sdf)) * fill.a
          val strokeAmount = (1.0f - step(0.0f, annularSdf)) * env.STROKE_COLOR.a

          val fillColor   = vec4(fill.rgb * fillAmount, fillAmount)
          val strokeColor = vec4(env.STROKE_COLOR.rgb * strokeAmount, strokeAmount)

          mix(fillColor, strokeColor, strokeAmount)

      }

    val output = shader.toGLSL[Indigo]
