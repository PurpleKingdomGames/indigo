package indigoextras.effectmaterials.shaders

import indigo.shared.shader.library.IndigoUV.*
import indigo.shared.shader.library.Lighting
import indigo.shared.shader.library.TileAndStretch
import ultraviolet.syntax.*

object RefractionShaders:

  trait BlendEnv extends BlendFragmentEnvReference {
    val REFRACTION_AMOUNT: Float = 0.0f
  }
  object BlendEnv:
    val reference: BlendEnv = new BlendEnv {}

  trait FragEnv extends Lighting.LightEnv {
    val FILLTYPE: highp[Float] = 0.0f
  }
  object FragEnv:
    val reference: FragEnv = new FragEnv {}

  final case class IndigoRefractionBlendData(REFRACTION_AMOUNT: Float)

  inline def refractionFragment =
    Shader[BlendEnv] { env =>
      ubo[IndigoRefractionBlendData]

      def fragment(color: vec4): vec4 =
        val normal = normalize(env.SRC - 0.5f).xy
        val offset = env.UV + (normal * env.REFRACTION_AMOUNT * env.SRC.w)

        texture2D(env.DST_CHANNEL, offset)
    }

  final case class IndigoBitmapData(FILLTYPE: highp[Float])

  inline def normalMinusBlue =
    Shader[FragEnv] { env =>
      import TileAndStretch.*

      ubo[IndigoBitmapData]

      def fragment(color: vec4): vec4 =

        env.CHANNEL_0 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_0,
          env.SRC_CHANNEL,
          env.CHANNEL_0_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_1 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_1,
          env.SRC_CHANNEL,
          env.CHANNEL_1_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_2 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_2,
          env.SRC_CHANNEL,
          env.CHANNEL_2_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_3 = tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_3,
          env.SRC_CHANNEL,
          env.CHANNEL_3_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )

        val redGreen = vec3(env.CHANNEL_0.xy, 0.0f)
        val alpha: Float =
          if abs(redGreen.x - 0.5f) < 0.01f && abs(redGreen.y - 0.5f) < 0.01f then 0.0f
          else max(redGreen.x, redGreen.y)

        vec4(env.CHANNEL_0.xy * alpha, 0.0f, alpha)

    }
