package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object NormalBlend:

  object fragment:
    inline def shader =
      Shader[BlendFragmentEnv, Unit] { env =>
        def fragment: vec4 =
          env.SRC
      }

    val output = shader.toGLSL[Indigo]
