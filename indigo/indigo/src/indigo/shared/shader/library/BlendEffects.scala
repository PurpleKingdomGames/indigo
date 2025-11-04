package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

object BlendEffects:

  trait Env extends BlendFragmentEnvReference {
    val ALPHA_SATURATION_OVERLAYTYPE_BG: highp[vec4] = vec4(0.0f)
    val TINT: vec4                                   = vec4(0.0f)
    val GRADIENT_FROM_TO: vec4                       = vec4(0.0f)
    val GRADIENT_FROM_COLOR: vec4                    = vec4(0.0f)
    val GRADIENT_TO_COLOR: vec4                      = vec4(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoBlendEffectsData(
      ALPHA_SATURATION_OVERLAYTYPE_BG: highp[vec4],
      TINT: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      import ImageEffectFunctions.*

      ubo[IndigoBlendEffectsData]

      def fragment(color: vec4): vec4 =

        // Delegates
        val _applyBasicEffects: (vec4, Float, vec3) => vec4 =
          applyBasicEffects
        val _calculateColorOverlay: (vec4, vec4) => vec4 =
          calculateColorOverlay
        val _calculateLinearGradientOverlay: (vec4, vec2, vec2, vec2, vec4, vec4) => vec4 =
          calculateLinearGradientOverlay
        val _calculateRadialGradientOverlay: (vec4, vec2, vec2, vec2, vec4, vec4) => vec4 =
          calculateRadialGradientOverlay
        val _calculateSaturation: (vec4, Float) => vec4 =
          calculateSaturation

        val effectsBg: Int = round(env.ALPHA_SATURATION_OVERLAYTYPE_BG.w).toInt

        val effects: vec4 =
          effectsBg match
            case 0 => env.SRC
            case 1 => mix(env.DST, env.SRC, env.SRC.w)
            case _ => env.SRC

        val alpha: Float    = env.ALPHA_SATURATION_OVERLAYTYPE_BG.x
        val baseColor: vec4 = _applyBasicEffects(effects, alpha, env.TINT.xyz)

        // 0 = color 1 = linear gradient 2 = radial gradient
        val overlayType: Int = round(env.ALPHA_SATURATION_OVERLAYTYPE_BG.z).toInt
        val overlay: vec4 =
          overlayType match
            case 0 =>
              _calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

            case 1 =>
              _calculateLinearGradientOverlay(
                baseColor,
                vec2(env.GRADIENT_FROM_TO.x, env.SIZE.y - env.GRADIENT_FROM_TO.y),
                vec2(env.GRADIENT_FROM_TO.z, env.SIZE.y - env.GRADIENT_FROM_TO.w),
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case 2 =>
              _calculateRadialGradientOverlay(
                baseColor,
                vec2(env.GRADIENT_FROM_TO.x, env.SIZE.y - env.GRADIENT_FROM_TO.y),
                vec2(env.GRADIENT_FROM_TO.z, env.SIZE.y - env.GRADIENT_FROM_TO.w),
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case _ =>
              _calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

        _calculateSaturation(overlay, env.ALPHA_SATURATION_OVERLAYTYPE_BG.y)
    }
