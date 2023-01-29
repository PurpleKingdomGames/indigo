package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object ShapeBox:

  case class IndigoShapeData(
      ASPECT_RATIO: vec2,
      STROKE_WIDTH: Float,
      FILL_TYPE: Float,
      STROKE_COLOR: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  object fragment:
    inline def shader =
      Shader[FragmentEnv & IndigoShapeData] { env =>
        import ShapeShaderFunctions.*

        ubo[IndigoShapeData]

        // Borrowed with thanks! https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
        def sdfCalc(p: vec2, b: vec2): Float =
          val d = abs(p) - b
          length(max(d, 0.0f)) + min(max(d.x, d.y), 0.0f)

        def fragment: vec4 =
          val strokeWidthHalf = max(0.0f, env.STROKE_WIDTH / env.SIZE.x / 2.0f)

          val fillType = round(env.FILL_TYPE).toInt
          val fill: vec4 =
            fillType match
              case 1 =>
                calculateLinearGradient(
                  env.GRADIENT_FROM_TO.xy,
                  env.GRADIENT_FROM_TO.zw,
                  env.UV * env.SIZE,
                  env.GRADIENT_FROM_COLOR,
                  env.GRADIENT_TO_COLOR
                )

              case 2 =>
                calculateRadialGradient(
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

    val output = shader.toGLSL[Indigo]
