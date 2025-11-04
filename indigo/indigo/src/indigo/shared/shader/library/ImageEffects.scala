package indigo.shared.shader.library

import ultraviolet.syntax.*

import scala.annotation.nowarn

object ImageEffects:

  trait Env extends Lighting.LightEnv {
    val ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE: highp[vec4] = vec4(0.0f)
    val NINE_SLICE_CENTER: highp[vec4]                     = vec4(0.0f)
    val TINT: vec4                                         = vec4(0.0f)
    val GRADIENT_FROM_TO: vec4                             = vec4(0.0f)
    val GRADIENT_FROM_COLOR: vec4                          = vec4(0.0f)
    val GRADIENT_TO_COLOR: vec4                            = vec4(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoImageEffectsData(
      ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE: highp[vec4],
      NINE_SLICE_CENTER: highp[vec4],
      TINT: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4
  )

  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      import ImageEffectFunctions.*
      import TileAndStretch.*

      ubo[IndigoImageEffectsData]

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
        val _tileAndStretchChannel: (Int, vec4, sampler2D.type, vec2, vec2, vec2, vec2, vec2, vec4) => vec4 =
          tileAndStretchChannel

        // 0 = normal 1 = stretch 2 = tile
        val fillType: Int =
          round(env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.w).toInt

        env.CHANNEL_0 = _tileAndStretchChannel(
          fillType,
          env.CHANNEL_0,
          env.SRC_CHANNEL,
          env.CHANNEL_0_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )
        env.CHANNEL_1 = _tileAndStretchChannel(
          fillType,
          env.CHANNEL_1,
          env.SRC_CHANNEL,
          env.CHANNEL_1_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )
        env.CHANNEL_2 = _tileAndStretchChannel(
          fillType,
          env.CHANNEL_2,
          env.SRC_CHANNEL,
          env.CHANNEL_2_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )
        env.CHANNEL_3 = _tileAndStretchChannel(
          fillType,
          env.CHANNEL_3,
          env.SRC_CHANNEL,
          env.CHANNEL_3_POSITION,
          env.CHANNEL_0_SIZE,
          env.UV,
          env.SIZE,
          env.TEXTURE_SIZE,
          env.NINE_SLICE_CENTER
        )

        val alpha: Float    = env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.x
        val baseColor: vec4 = _applyBasicEffects(env.CHANNEL_0, alpha, env.TINT.xyz)

        // 0 = color 1 = linear gradient 2 = radial gradient
        val overlayType: Int = round(env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.z).toInt
        val overlay: vec4 =
          overlayType match
            case 0 =>
              _calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

            case 1 =>
              _calculateLinearGradientOverlay(
                baseColor,
                env.GRADIENT_FROM_TO.xy,
                env.GRADIENT_FROM_TO.zw,
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case 2 =>
              _calculateRadialGradientOverlay(
                baseColor,
                env.GRADIENT_FROM_TO.xy,
                env.GRADIENT_FROM_TO.zw,
                env.UV * env.SIZE,
                env.GRADIENT_FROM_COLOR,
                env.GRADIENT_TO_COLOR
              )

            case _ =>
              _calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

        _calculateSaturation(overlay, env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.y)
    }

object ImageEffectFunctions:

  /** Applies alpha and tint
    */
  inline def applyBasicEffects: (vec4, Float, vec3) => vec4 =
    (textureColor: vec4, alpha: Float, tint: vec3) =>
      val withAlpha: vec4 = vec4(textureColor.xyz * alpha, textureColor.w * alpha)
      vec4(withAlpha.xyz * tint, withAlpha.w)

  inline def calculateColorOverlay: (vec4, vec4) => vec4 =
    (baseColor: vec4, overlayColor: vec4) =>
      mix(baseColor, vec4(overlayColor.xyz * baseColor.w, baseColor.w), overlayColor.w)

  inline def calculateLinearGradientOverlay: (vec4, vec2, vec2, vec2, vec4, vec4) => vec4 =
    (baseColor: vec4, pointA: vec2, pointB: vec2, pointP: vec2, fromColor: vec4, toColor: vec4) =>
      // `h` is the distance along the gradient 0 at A, 1 at B
      val h: Float =
        min(1.0f, max(0.0f, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)))
      val gradient: vec4 = mix(fromColor, toColor, h)

      mix(baseColor, vec4(gradient.xyz * baseColor.w, baseColor.w), gradient.w)

  inline def calculateRadialGradientOverlay: (vec4, vec2, vec2, vec2, vec4, vec4) => vec4 =
    (baseColor: vec4, pointA: vec2, pointB: vec2, pointP: vec2, fromColor: vec4, toColor: vec4) =>
      val radius: Float      = length(pointB - pointA)
      val distanceToP: Float = length(pointP - pointA)
      val sdf: Float         = clamp(-((distanceToP - radius) / radius), 0.0f, 1.0f)
      val gradient: vec4     = mix(toColor, fromColor, sdf)

      mix(baseColor, vec4(gradient.xyz * baseColor.w, baseColor.w), gradient.w)

  inline def calculateSaturation: (vec4, Float) => vec4 =
    (color: vec4, saturation: Float) =>
      val average: Float  = (color.x + color.y + color.z) / 3.0f
      val grayscale: vec4 = vec4(average, average, average, color.w)

      mix(grayscale, color, max(0.0f, min(1.0f, saturation)))
