package indigoextras.effectmaterials.shaders

import indigo.shared.shader.library.ImageEffectFunctions
import indigo.shared.shader.library.IndigoUV.*
import indigo.shared.shader.library.TileAndStretch
import ultraviolet.syntax.*

object LegacyEffectsShaders:

  object vertex:
    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def shader =
      Shader[IndigoVertexEnv] { env =>
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
          vec2(-1.0, -1.0),
          vec2(0.0, -1.0),
          vec2(1.0, -1.0),
          vec2(-1.0, 0.0),
          vec2(0.0, 0.0),
          vec2(1.0, 0.0),
          vec2(-1.0, 1.0),
          vec2(0.0, 1.0),
          vec2(1.0, 1.0)
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

        def vertex: Unit =
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
      }

    val output = shader.toGLSL[Indigo]

  object fragment:

    final case class IndigoLegacyEffectsData(
        ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE: highp[vec4],
        TINT: vec4,
        GRADIENT_FROM_TO: vec4,
        GRADIENT_FROM_COLOR: vec4,
        GRADIENT_TO_COLOR: vec4,
        BORDER_COLOR: vec4,
        GLOW_COLOR: vec4,
        EFFECT_AMOUNTS: vec4
    )

    inline def shader =
      Shader[IndigoFragmentEnv & IndigoLegacyEffectsData] { env =>
        import ImageEffectFunctions.*
        import TileAndStretch.*

        ubo[IndigoLegacyEffectsData]

        def fragment: vec4 =

          // ----------------------------------
          // Identical to ImageEffects (start)

          // 0 = normal 1 = stretch 2 = tile
          val fillType: Int =
            round(env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.w).toInt

          env.CHANNEL_0 = tileAndStretchChannel(
            fillType,
            env.CHANNEL_0,
            env.SRC_CHANNEL,
            env.CHANNEL_0_POSITION,
            env.CHANNEL_0_SIZE,
            env.UV,
            env.SIZE,
            env.TEXTURE_SIZE
          )
          env.CHANNEL_1 = tileAndStretchChannel(
            fillType,
            env.CHANNEL_1,
            env.SRC_CHANNEL,
            env.CHANNEL_1_POSITION,
            env.CHANNEL_0_SIZE,
            env.UV,
            env.SIZE,
            env.TEXTURE_SIZE
          )
          env.CHANNEL_2 = tileAndStretchChannel(
            fillType,
            env.CHANNEL_2,
            env.SRC_CHANNEL,
            env.CHANNEL_2_POSITION,
            env.CHANNEL_0_SIZE,
            env.UV,
            env.SIZE,
            env.TEXTURE_SIZE
          )
          env.CHANNEL_3 = tileAndStretchChannel(
            fillType,
            env.CHANNEL_3,
            env.SRC_CHANNEL,
            env.CHANNEL_3_POSITION,
            env.CHANNEL_0_SIZE,
            env.UV,
            env.SIZE,
            env.TEXTURE_SIZE
          )

          val alpha: Float    = env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.x
          val baseColor: vec4 = applyBasicEffects(env.CHANNEL_0, alpha, env.TINT.xyz)

          // 0 = color 1 = linear gradient 2 = radial gradient
          val overlayType: Int = round(env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.z).toInt
          val overlay: vec4 =
            overlayType match
              case 0 =>
                calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

              case 1 =>
                calculateLinearGradientOverlay(
                  baseColor,
                  env.GRADIENT_FROM_TO.xy,
                  env.GRADIENT_FROM_TO.zw,
                  env.UV * env.SIZE,
                  env.GRADIENT_FROM_COLOR,
                  env.GRADIENT_TO_COLOR
                )

              case 2 =>
                calculateRadialGradientOverlay(
                  baseColor,
                  env.GRADIENT_FROM_TO.xy,
                  env.GRADIENT_FROM_TO.zw,
                  env.UV * env.SIZE,
                  env.GRADIENT_FROM_COLOR,
                  env.GRADIENT_TO_COLOR
                )

              case _ =>
                calculateColorOverlay(baseColor, env.GRADIENT_FROM_COLOR)

          calculateSaturation(overlay, env.ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.y)

      // Identical to ImageEffects (end)
      // --------------------------------
      }

    val output = shader.toGLSL[Indigo]
