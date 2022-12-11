package indigoextras.effectmaterials.shaders

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object RefractionShaders:

  final case class IndigoRefractionBlendData(REFRACTION_AMOUNT: Float)

  object refractionFragment:
    inline def shader =
      Shader[IndigoRefractionBlendData & IndigoBlendFragmentEnv] { env =>
        ubo[IndigoRefractionBlendData]

        def fragment: vec4 =
          val normal = normalize(env.SRC - 0.5f).xy
          val offset = env.UV + (normal * env.REFRACTION_AMOUNT * env.SRC.w)

          texture2D(env.DST_CHANNEL, offset)
      }

    val output = shader.toGLSL[Indigo]

  final case class IndigoBitmapData(FILLTYPE: highp[Float])

  object normalMinusBlue:
    inline def shader =
      Shader[IndigoBitmapData & IndigoFragmentEnv] { env =>
        ubo[IndigoBitmapData]

        def stretchedUVs(pos: vec2, size: vec2): vec2 =
          pos + env.UV * size

        def tiledUVs(pos: vec2, size: vec2): vec2 =
          pos + (fract(env.UV * (env.SIZE / env.TEXTURE_SIZE)) * size)

        def fragment: vec4 =

          // 0 = normal 1 = stretch 2 = tile
          val fillType = round(env.FILLTYPE).toInt

          fillType match
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

          val redGreen = vec3(env.CHANNEL_0.xy, 0.0f)
          val alpha: Float =
            if abs(redGreen.x - 0.5f) < 0.01f && abs(redGreen.y - 0.5f) < 0.01f then 0.0f
            else max(redGreen.x, redGreen.y)

          vec4(env.CHANNEL_0.xy * alpha, 0.0f, alpha)

      }

    val output = shader.toGLSL[Indigo]
