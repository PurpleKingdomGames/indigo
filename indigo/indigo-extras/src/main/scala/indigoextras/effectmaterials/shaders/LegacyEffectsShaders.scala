package indigoextras.effectmaterials.shaders

import indigo.shared.shader.library.IndigoUV.*
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
        ALPHA_SATURATION_OVERLAYTYPE: highp[vec3],
        TINT: vec4,
        GRADIENT_FROM_TO: vec4,
        GRADIENT_FROM_COLOR: vec4,
        GRADIENT_TO_COLOR: vec4,
        BORDER_COLOR: vec4,
        GLOW_COLOR: vec4,
        EFFECT_AMOUNTS: vec4
    )

    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def shader =
      Shader[IndigoFragmentEnv] { env =>
        ubo[IndigoLegacyEffectsData]

        def fragment: vec4 =
          vec4(1.0)
      }

    val output = shader.toGLSL[Indigo]
