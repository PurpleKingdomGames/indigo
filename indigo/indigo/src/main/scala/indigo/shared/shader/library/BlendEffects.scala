package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object BlendEffects:

  case class IndigoBlendEffectsData(
      ALPHA_SATURATION_OVERLAYTYPE_BG: highp[vec4],
      TINT: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  object fragment:
    inline def shader =
      Shader[IndigoBlendFragmentEnv & IndigoBlendEffectsData] { env =>
        import ImageEffectFunctions.*

        ubo[IndigoBlendEffectsData]

        def fragment: vec4 =

          val effectsBg: Int = round(env.ALPHA_SATURATION_OVERLAYTYPE_BG.w).toInt

          val effects: vec4 =
            effectsBg match
              case 0 => env.SRC
              case 1 => mix(env.DST, env.SRC, env.SRC.w)
              case _ => env.SRC

          val alpha: Float    = env.ALPHA_SATURATION_OVERLAYTYPE_BG.x
          val baseColor: vec4 = applyBasicEffects(effects, alpha, env.TINT.xyz)

          // 0 = color 1 = linear gradient 2 = radial gradient
          val overlayType: Int = round(env.ALPHA_SATURATION_OVERLAYTYPE_BG.z).toInt
          val overlay: vec4 =
            overlayType match
              case 0 =>
                calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

              case 1 =>
                calculateLinearGradientOverlay(
                  baseColor,
                  vec2(env.GRADIENT_FROM_TO.x, env.SIZE.y - env.GRADIENT_FROM_TO.y),
                  vec2(env.GRADIENT_FROM_TO.z, env.SIZE.y - env.GRADIENT_FROM_TO.w),
                  env.UV * env.SIZE,
                  env.GRADIENT_FROM_COLOR,
                  env.GRADIENT_TO_COLOR
                )

              case 2 =>
                calculateRadialGradientOverlay(
                  baseColor,
                  vec2(env.GRADIENT_FROM_TO.x, env.SIZE.y - env.GRADIENT_FROM_TO.y),
                  vec2(env.GRADIENT_FROM_TO.z, env.SIZE.y - env.GRADIENT_FROM_TO.w),
                  env.UV * env.SIZE,
                  env.GRADIENT_FROM_COLOR,
                  env.GRADIENT_TO_COLOR
                )

              case _ =>
                calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

          calculateSaturation(overlay, env.ALPHA_SATURATION_OVERLAYTYPE_BG.y)
      }

    val output = shader.toGLSL[Indigo]
