package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object Blit:

  trait Env extends Lighting.LightEnv {
    val FILLTYPE: highp[Float] = 0.0f
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoBitmapData(FILLTYPE: highp[Float])

  inline def fragment =
    Shader[Env] { env =>
      import TileAndStretch.*

      // Delegates
      val _tileAndStretchChannel: (Int, vec4, sampler2D.type, vec2, vec2, vec2, vec2, vec2) => vec4 =
        tileAndStretchChannel

      ubo[IndigoBitmapData]

      def fragment(color: vec4): vec4 =
        env.CHANNEL_0 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_0,
          env.SRC_CHANNEL,
          env.CHANNEL_0_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_1 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_1,
          env.SRC_CHANNEL,
          env.CHANNEL_1_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_2 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_2,
          env.SRC_CHANNEL,
          env.CHANNEL_2_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )
        env.CHANNEL_3 = _tileAndStretchChannel(
          env.FILLTYPE.toInt,
          env.CHANNEL_3,
          env.SRC_CHANNEL,
          env.CHANNEL_3_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE
        )

        env.CHANNEL_0
    }
