package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object ImageEffects:

  case class IndigoImageEffectsData(
      ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE: highp[vec4],
      TINT: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  object fragment:
    inline def shader =
      Shader[IndigoFragmentEnv & IndigoImageEffectsData] { env =>
        ubo[IndigoImageEffectsData]

        def applyBasicEffects(textureColor: vec4): vec4 =
          val alpha: Float        = env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.x
          val withAlpha: vec4     = vec4(textureColor.xyz * alpha, textureColor.w * alpha)
          val tintedVersion: vec4 = vec4(withAlpha.xyz * env.TINT.xyz, withAlpha.w)

          tintedVersion

        def calculateColorOverlay(color: vec4): vec4 =
          mix(color, vec4(env.GRADIENT_FROM_COLOR.xyz * color.w, color.w), env.GRADIENT_FROM_COLOR.w)

        def calculateLinearGradientOverlay(color: vec4): vec4 =
          val pointA: vec2 = env.GRADIENT_FROM_TO.xy
          val pointB: vec2 = env.GRADIENT_FROM_TO.zw
          val pointP: vec2 = env.UV * env.SIZE

          // `h` is the distance along the gradient 0 at A, 1 at B
          val h: Float =
            min(1.0f, max(0.0f, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)))

          val gradient: vec4 = mix(env.GRADIENT_FROM_COLOR, env.GRADIENT_TO_COLOR, h)

          mix(color, vec4(gradient.xyz * color.w, color.w), gradient.w)

        def calculateRadialGradientOverlay(color: vec4): vec4 =
          val pointA: vec2 = env.GRADIENT_FROM_TO.xy
          val pointB: vec2 = env.GRADIENT_FROM_TO.zw
          val pointP: vec2 = env.UV * env.SIZE

          val radius: Float      = length(pointB - pointA)
          val distanceToP: Float = length(pointP - pointA)

          val sdf: Float = clamp(-((distanceToP - radius) / radius), 0.0f, 1.0f)

          val gradient: vec4 = mix(env.GRADIENT_TO_COLOR, env.GRADIENT_FROM_COLOR, sdf)

          mix(color, vec4(gradient.xyz * color.w, color.w), gradient.w)

        def calculateSaturation(color: vec4): vec4 =
          val saturation: Float = env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.y
          val average: Float    = (color.x + color.y + color.z) / 3.0f
          val grayscale: vec4   = vec4(average, average, average, color.w)

          mix(grayscale, color, max(0.0f, min(1.0f, saturation)))

        def stretchedUVs(pos: vec2, size: vec2): vec2 =
          pos + env.UV * size

        def tiledUVs(pos: vec2, size: vec2): vec2 =
          pos + (fract(env.UV * (env.SIZE / env.TEXTURE_SIZE)) * size)

        def fragment: vec4 =

          // 0 = normal 1 = stretch 2 = tile
          val fillType: Int =
            round(env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.w).toInt

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

          val baseColor: vec4 = applyBasicEffects(env.CHANNEL_0)

          // 0 = color 1 = linear gradient 2 = radial gradient
          val overlayType: Int = round(env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.z).toInt
          val overlay: vec4 =
            overlayType match
              case 0 =>
                calculateColorOverlay(baseColor)

              case 1 =>
                calculateLinearGradientOverlay(baseColor)

              case 2 =>
                calculateRadialGradientOverlay(baseColor)

              case _ =>
                calculateColorOverlay(baseColor)

          val saturation: vec4 = calculateSaturation(overlay)

          saturation
      }

    val output = shader.toGLSL[Indigo]
