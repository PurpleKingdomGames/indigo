package indigoextras.effectmaterials.shaders

import indigo.shared.shader.library.ImageEffectFunctions
import indigo.shared.shader.library.IndigoUV.*
import indigo.shared.shader.library.Lighting
import indigo.shared.shader.library.TileAndStretch
import ultraviolet.syntax.*

import scala.annotation.nowarn

object LegacyEffectsShaders:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  @nowarn("msg=unused")
  inline def vertex =
    Shader[VertexEnv] { env =>
      @out var v_offsetTL: vec2 = null
      @out var v_offsetTC: vec2 = null
      @out var v_offsetTR: vec2 = null
      @out var v_offsetML: vec2 = null
      @out var v_offsetMC: vec2 = null
      @out var v_offsetMR: vec2 = null
      @out var v_offsetBL: vec2 = null
      @out var v_offsetBC: vec2 = null
      @out var v_offsetBR: vec2 = null

      @const val gridOffsets = array[9, vec2](
        vec2(-1.0f, -1.0f),
        vec2(0.0f, -1.0f),
        vec2(1.0f, -1.0f),
        vec2(-1.0f, 0.0f),
        vec2(0.0f, 0.0f),
        vec2(1.0f, 0.0f),
        vec2(-1.0f, 1.0f),
        vec2(0.0f, 1.0f),
        vec2(1.0f, 1.0f)
      )

      def generateTexCoords3x3: array[9, vec2] =
        val onePixel = 1.0f / env.SIZE
        array[9, vec2](
          env.UV + (onePixel * gridOffsets(0)),
          env.UV + (onePixel * gridOffsets(1)),
          env.UV + (onePixel * gridOffsets(2)),
          env.UV + (onePixel * gridOffsets(3)),
          env.UV + (onePixel * gridOffsets(4)),
          env.UV + (onePixel * gridOffsets(5)),
          env.UV + (onePixel * gridOffsets(6)),
          env.UV + (onePixel * gridOffsets(7)),
          env.UV + (onePixel * gridOffsets(8))
        )

      def vertex(v: vec4): vec4 =
        val offsets = generateTexCoords3x3

        v_offsetTL = (offsets(0) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetTC = (offsets(1) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetTR = (offsets(2) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetML = (offsets(3) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetMC = (offsets(4) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetMR = (offsets(5) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetBL = (offsets(6) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetBC = (offsets(7) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET
        v_offsetBR = (offsets(8) * env.FRAME_SIZE) + env.CHANNEL_0_ATLAS_OFFSET

        v
    }

  trait Env extends Lighting.LightEnv {
    val ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE: highp[vec4] = vec4(0.0f)
    val NINE_SLICE_CENTER: highp[vec4]                     = vec4(0.0f)
    val TINT: vec4                                         = vec4(0.0f)
    val GRADIENT_FROM_TO: vec4                             = vec4(0.0f)
    val GRADIENT_FROM_COLOR: vec4                          = vec4(0.0f)
    val GRADIENT_TO_COLOR: vec4                            = vec4(0.0f)
    val BORDER_COLOR: vec4                                 = vec4(0.0f)
    val GLOW_COLOR: vec4                                   = vec4(0.0f)
    val EFFECT_AMOUNTS: vec4                               = vec4(0.0f)
  }
  object Env:
    val reference: Env = new Env {}

  final case class IndigoLegacyEffectsData(
      ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE: highp[vec4],
      NINE_SLICE_CENTER: highp[vec4],
      TINT: vec4,
      GRADIENT_FROM_TO: vec4,
      GRADIENT_FROM_COLOR: vec4,
      GRADIENT_TO_COLOR: vec4,
      BORDER_COLOR: vec4,
      GLOW_COLOR: vec4,
      EFFECT_AMOUNTS: vec4
  )

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  @nowarn("msg=unused")
  inline def fragment =
    Shader[Env] { env =>
      import ImageEffectFunctions.*
      import TileAndStretch.*

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

      @in val v_offsetTL: vec2 = null
      @in val v_offsetTC: vec2 = null
      @in val v_offsetTR: vec2 = null
      @in val v_offsetML: vec2 = null
      @in val v_offsetMC: vec2 = null
      @in val v_offsetMR: vec2 = null
      @in val v_offsetBL: vec2 = null
      @in val v_offsetBC: vec2 = null
      @in val v_offsetBR: vec2 = null

      ubo[IndigoLegacyEffectsData]

      // format: off
      @const val border1px = array[9, Float](
        0.0f, 1.0f, 0.0f,
        1.0f, 0.0f, 1.0f,
        0.0f, 1.0f, 0.0f
      )

      // format: off
      @const val border2px = array[9, Float](
        1.0f, 1.0f, 1.0f,
        1.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 1.0f
      )

      def calculateOuterBorder(baseAlpha: Float, alphas: array[9, Float], amount: Float): vec4 = {
        val checkedAmount = clamp(amount, 0.0f, 2.0f)

        val borderAmount: Int =
          if abs(checkedAmount) >= 0.99f && abs(checkedAmount) < 1.01f then
            1
          else if abs(checkedAmount) >= 1.99f && abs(checkedAmount) < 2.01f then
            2
          else
            0

        if borderAmount == 0 || baseAlpha > 0.001f then
          vec4(0.0f)
        else
          val kernel: array[9, Float] =
            borderAmount match
              case 2 => border2px
              case _ => border1px // 0 has been ruled out

          // format: off
          val alphaSum: Float =
            alphas(0) * kernel(0) +
            alphas(1) * kernel(1) +
            alphas(2) * kernel(2) +
            alphas(3) * kernel(3) +
            alphas(4) * kernel(4) +
            alphas(5) * kernel(5) +
            alphas(6) * kernel(6) +
            alphas(7) * kernel(7) +
            alphas(8) * kernel(8)

          if alphaSum > 0.0f then env.BORDER_COLOR
          else vec4(0.0f)
      }

      def calculateInnerBorder(baseAlpha: Float, alphas: array[9, Float], amount: Float): vec4 = {
        val checkedAmount = clamp(amount, 0.0f, 2.0f)

        val borderAmount: Int =
          if abs(checkedAmount) >= 0.99f && abs(checkedAmount) < 1.01f then
            1
          else if abs(checkedAmount) >= 1.99f && abs(checkedAmount) < 2.01f then
            2
          else
            0

        if borderAmount == 0 || baseAlpha < 0.001f then
          vec4(0.0f)
        else
          val kernel: array[9, Float] =
            borderAmount match
              case 2 => border2px
              case _ => border1px // 0 has been ruled out

          // format: off
          val alphaSum: Float =
            alphas(0) * kernel(0) +
            alphas(1) * kernel(1) +
            alphas(2) * kernel(2) +
            alphas(3) * kernel(3) +
            alphas(4) * kernel(4) +
            alphas(5) * kernel(5) +
            alphas(6) * kernel(6) +
            alphas(7) * kernel(7) +
            alphas(8) * kernel(8)

          if alphaSum > 0.0f then env.BORDER_COLOR
          else vec4(0.0f)
      }

      // format: off
      @const val glowKernel: array[9, Float] = array[9, Float](
        1.0f, 0.5f, 1.0f,
        0.5f, 0.0f, 0.5f,
        1.0f, 0.5f, 1.0f
      )
      // glowKernel values summed up.
      @const val glowKernelWeight: Float = 6.0f

      def calculateOuterGlow(baseAlpha: Float, alphas: array[9, Float], amount: Float): vec4 =
        if baseAlpha > 0.01f then
          vec4(0.0f)
        else
          // format: off
          val alphaSum: Float =
            alphas(0) * glowKernel(0) +
            alphas(1) * glowKernel(1) +
            alphas(2) * glowKernel(2) +
            alphas(3) * glowKernel(3) +
            alphas(4) * glowKernel(4) +
            alphas(5) * glowKernel(5) +
            alphas(6) * glowKernel(6) +
            alphas(7) * glowKernel(7) +
            alphas(8) * glowKernel(8)

          if alphaSum > 0.0f then
            val checkedAmount: Float = max(0.0f, amount)
            val glowAmount: Float = (alphaSum / glowKernelWeight) * checkedAmount
            vec4(env.GLOW_COLOR.xyz, env.GLOW_COLOR.w * glowAmount)
          else vec4(0.0f)

      def calculateInnerGlow(baseAlpha: Float, alphas: array[9, Float], amount: Float): vec4 =
        if baseAlpha < 0.01f then
          vec4(0.0f)
        else
          // format: off
          val alphaSum: Float =
            floor(alphas(0)) * glowKernel(0) +
            floor(alphas(1)) * glowKernel(1) +
            floor(alphas(2)) * glowKernel(2) +
            floor(alphas(3)) * glowKernel(3) +
            floor(alphas(4)) * glowKernel(4) +
            floor(alphas(5)) * glowKernel(5) +
            floor(alphas(6)) * glowKernel(6) +
            floor(alphas(7)) * glowKernel(7) +
            floor(alphas(8)) * glowKernel(8)

          if alphaSum > 0.0f then
            val checkedAmount = max(0.0f, amount)
            val glowAmount = (alphaSum / glowKernelWeight) * checkedAmount
            vec4(env.GLOW_COLOR.xyz, env.GLOW_COLOR.w * glowAmount)
          else vec4(0.0f)

      def fragment(color: vec4): vec4 =

        // ----------------------------------
        // Identical to ImageEffects (start)

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

        // Identical to ImageEffects (end)
        // --------------------------------

        val saturatedColor = _calculateSaturation(overlay, env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.y)

        val sampledRegionAlphas = array[9, Float](
          texture2D(env.SRC_CHANNEL, v_offsetTL).w,
          texture2D(env.SRC_CHANNEL, v_offsetTC).w,
          texture2D(env.SRC_CHANNEL, v_offsetTR).w,
          texture2D(env.SRC_CHANNEL, v_offsetML).w,
          texture2D(env.SRC_CHANNEL, v_offsetMC).w,
          texture2D(env.SRC_CHANNEL, v_offsetMR).w,
          texture2D(env.SRC_CHANNEL, v_offsetBL).w,
          texture2D(env.SRC_CHANNEL, v_offsetBC).w,
          texture2D(env.SRC_CHANNEL, v_offsetBR).w
        )

        val sampledRegionAlphasInverse = array[9, Float](
          (1.0f - sampledRegionAlphas(0)),
          (1.0f - sampledRegionAlphas(1)),
          (1.0f - sampledRegionAlphas(2)),
          (1.0f - sampledRegionAlphas(3)),
          (1.0f - sampledRegionAlphas(4)),
          (1.0f - sampledRegionAlphas(5)),
          (1.0f - sampledRegionAlphas(6)),
          (1.0f - sampledRegionAlphas(7)),
          (1.0f - sampledRegionAlphas(8))
        )

        val outerBorderAmount = env.EFFECT_AMOUNTS.x
        val innerBorderAmount = env.EFFECT_AMOUNTS.y
        val outerGlowAmount = env.EFFECT_AMOUNTS.z
        val innerGlowAmount = env.EFFECT_AMOUNTS.w

        val innerGlow = calculateInnerGlow(saturatedColor.w, sampledRegionAlphasInverse, innerGlowAmount)
        val outerGlow = calculateOuterGlow(saturatedColor.w, sampledRegionAlphas, outerGlowAmount)
        val innerBorder = calculateInnerBorder(saturatedColor.w, sampledRegionAlphasInverse, innerBorderAmount)
        val outerBorder = calculateOuterBorder(saturatedColor.w, sampledRegionAlphas, outerBorderAmount)

        val withInnerGlow = vec4(mix(saturatedColor.xyz, innerGlow.xyz, innerGlow.w), saturatedColor.w)
        val withOuterGlow = mix(withInnerGlow, outerGlow, outerGlow.w)
        val withInnerBorder = mix(withOuterGlow, innerBorder, innerBorder.w)
        val withOuterBorder = mix(withInnerBorder, outerBorder, outerBorder.w)

        withOuterBorder
    }
