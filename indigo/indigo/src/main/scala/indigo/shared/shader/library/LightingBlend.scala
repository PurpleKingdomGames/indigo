package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object LightingBlend:

  case class IndigoLightingBlendData(AMBIENT_LIGHT_COLOR: vec4)

  object fragment:
    inline def shader =
      Shader[BlendFragmentEnv & IndigoLightingBlendData, Unit] { env =>
        ubo[IndigoLightingBlendData]

        def fragment: vec4 =
          val ambient: vec4 =
            vec4(env.AMBIENT_LIGHT_COLOR.xyz * env.AMBIENT_LIGHT_COLOR.w, 1.0f)

          (env.DST * ambient) + (env.DST * env.SRC)
      }

    val output = shader.toGLSL[Indigo]
