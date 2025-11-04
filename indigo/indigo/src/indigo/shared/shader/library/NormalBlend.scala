package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

object NormalBlend:

  trait Env extends BlendFragmentEnvReference
  object Env:
    val reference: Env = new Env {}

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      def fragment(color: vec4): vec4 =
        env.SRC
    }
