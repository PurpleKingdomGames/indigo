package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object ShapePolygon:

  case class IndigoShapeData(
      STROKE_WIDTH: Float,
      FILL_TYPE: Float,
      COUNT: Float,
      STROKE_COLOR: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4,
      VERTICES: array[16, vec2]
  )

  object fragment:
    @SuppressWarnings(
      Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null", "scalafix:DisableSyntax.while")
    )
    inline def shader =
      Shader[IndigoFragmentEnv & IndigoShapeData] { env =>
        import ShapeShaderFunctions.*

        ubo[IndigoShapeData]

        // Borrowed with thanks! https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
        def sdfCalc(p: vec2, count: Int, v: array[16, vec2]): Float =
          var d      = dot(p - v(0), p - v(0))
          var s      = 1.0f
          var i: Int = 0
          var j: Int = count - 1

          while i < count do
            j = i
            i = i + 1
            val e = v(j) - v(i)
            val w = p - v(i)
            val b = w - e * clamp(dot(w, e) / dot(e, e), 0.0f, 1.0f)
            d = min(d, dot(b, b))
            val c = bvec3(p.y >= v(i).y, p.y < v(j).y, e.x * w.y > e.y * w.x)
            if (all(c) || all(not(c))) s = s * -1.0f

          s * sqrt(d)

        def toUvSpace(count: Int, v: array[16, vec2]): array[16, vec2] =
          val polygon: array[16, vec2] = null

          _for(0, _ < count, _ + 1) { i =>
            polygon(i) = v(i) / env.SIZE
          }

          polygon

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

          val iCount = env.COUNT.toInt

          val polygon = toUvSpace(iCount, env.VERTICES)

          val sdf        = sdfCalc(env.UV, iCount, polygon)
          val annularSdf = abs(sdf) - strokeWidthHalf

          val fillAmount   = (1.0f - step(0.0f, sdf)) * fill.a
          val strokeAmount = (1.0f - step(0.0f, annularSdf)) * env.STROKE_COLOR.a

          val fillColor   = vec4(fill.rgb * fillAmount, fillAmount)
          val strokeColor = vec4(env.STROKE_COLOR.rgb * strokeAmount, strokeAmount)

          mix(fillColor, strokeColor, strokeAmount)
      }

    val output = shader.toGLSL[Indigo]
