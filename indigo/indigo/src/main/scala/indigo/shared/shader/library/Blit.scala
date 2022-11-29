package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object Blit:

  case class IndigoBitmapData(FILLTYPE: highp[Float])

  object fragment:
    inline def shader =
      Shader[IndigoFragmentEnv & IndigoBitmapData] { env =>
        ubo[IndigoBitmapData]

        def stretchedUVs(pos: vec2, size: vec2): vec2 =
          pos + env.UV * size

        def tiledUVs(pos: vec2, size: vec2): vec2 =
          pos + (fract(env.UV * (env.SIZE / env.TEXTURE_SIZE)) * size)

        def fragment: vec4 =
          env.FILLTYPE.toInt match
            case 1 =>
              env.CHANNEL_0 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_0_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_1 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_1_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_2 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_2_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_3 = texture2D(env.SRC_CHANNEL, stretchedUVs(env.CHANNEL_3_POSITION, env.CHANNEL_0_SIZE))

            case 2 =>
              env.CHANNEL_0 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_0_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_1 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_1_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_2 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_2_POSITION, env.CHANNEL_0_SIZE))
              env.CHANNEL_3 = texture2D(env.SRC_CHANNEL, tiledUVs(env.CHANNEL_3_POSITION, env.CHANNEL_0_SIZE))

            case _ =>
              ()

          env.CHANNEL_0;
      }

    val output = shader.toGLSL[Indigo]
