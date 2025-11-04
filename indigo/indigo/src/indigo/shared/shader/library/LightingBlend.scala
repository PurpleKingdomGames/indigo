package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

object LightingBlend:

  trait Env extends BlendFragmentEnvReference {
    val AMBIENT_LIGHT_COLOR: vec4 = vec4(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoLightingBlendData(AMBIENT_LIGHT_COLOR: vec4)

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      ubo[IndigoLightingBlendData]

      def fragment(color: vec4): vec4 =
        val ambient: vec4 =
          vec4(env.AMBIENT_LIGHT_COLOR.xyz * env.AMBIENT_LIGHT_COLOR.w, 1.0f)

        (env.DST * ambient) + (env.DST * env.SRC)
    }
