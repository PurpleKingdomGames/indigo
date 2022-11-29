package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object NoOp:

  object vertex:
    inline def shader =
      Shader {
        def vertex: Unit = {}
      }

    val output = shader.toGLSL[Indigo]

  object fragment:
    inline def shader =
      Shader {
        def fragment: Unit = {}
      }

    val output = shader.toGLSL[Indigo]

  object prepare:
    inline def shader =
      Shader {
        def prepare: Unit = {}
      }

    val output = shader.toGLSL[Indigo]

  object light:
    inline def shader =
      Shader {
        def light: Unit = {}
      }

    val output = shader.toGLSL[Indigo]

  object composite:
    inline def shader =
      Shader {
        def composite: Unit = {}
      }

    val output = shader.toGLSL[Indigo]
