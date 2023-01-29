package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object ShapeLine:

  case class IndigoShapeData(
      STROKE_WIDTH: Float,
      STROKE_COLOR: vec4,
      START: vec2,
      END: vec2
  )

  object fragment:
    inline def shader =
      Shader[FragmentEnv & IndigoShapeData] { env =>
        ubo[IndigoShapeData]

        def sdfCalc(p: vec2, a: vec2, b: vec2): Float =
          val pa = p - a
          val ba = b - a
          val h  = clamp(dot(pa, ba) / dot(ba, ba), 0.0f, 1.0f)
          length(pa - ba * h)

        def fragment: vec4 =
          val strokeWidthHalf = max(0.0f, env.STROKE_WIDTH / env.SIZE.x / 2.0f)
          val sdf             = sdfCalc(env.UV, env.START / env.SIZE, env.END / env.SIZE)
          val annularSdf      = sdf - strokeWidthHalf
          val strokeAmount    = (1.0f - step(0.0f, annularSdf)) * env.STROKE_COLOR.w
          vec4(env.STROKE_COLOR.xyz * strokeAmount, strokeAmount)
      }

    val output = shader.toGLSL[Indigo]
