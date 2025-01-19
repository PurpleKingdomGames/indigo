package indigo.shared.shader.library

import ultraviolet.datatypes.ShaderResult
import ultraviolet.syntax.*

import scala.annotation.nowarn

trait BaseEntityShader:

  protected case class IndigoProjectionData(u_projection: mat4)

  protected case class IndigoFrameData(
      TIME: highp[Float], // Running time
      VIEWPORT_SIZE: vec2 // Size of the viewport in pixels
  )

  protected case class IndigoCloneReferenceData( // Used during cloning.
      u_ref_refFlip: vec4,
      u_ref_sizeAndFrameScale: vec4,
      u_ref_channelOffsets01: vec4,
      u_ref_channelOffsets23: vec4,
      u_ref_textureSizeAtlasSize: vec4
  )

  protected case class IndigoDynamicLightingData(
      numOfLights: Float,
      lightFlags: highp[array[8, vec4]], // vec4(active, type, far cut off, falloff type)
      lightColor: array[8, vec4],
      lightSpecular: array[8, vec4],
      lightPositionRotation: array[8, vec4],     // vec4(x, y, rotation, ???)
      lightNearFarAngleIntensity: array[8, vec4] // vec4(near, far, angle, intensity)
  )

  protected case class GLEnv(gl_InstanceID: Int)
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  protected case class VertEnv(var gl_Position: vec4)

  protected case class UserDefined():
    def vertex(v: vec4): vec4   = v
    def fragment(v: vec4): vec4 = v
    def prepare(): Unit         = ()
    def light(): Unit           = ()
    def composite(): Unit       = ()

  protected type VertexEnv = GLEnv & VertEnv & IndigoFrameData & IndigoProjectionData & IndigoCloneReferenceData &
    UserDefined

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  @nowarn("msg=unused")
  inline def vertexShader[E](inline userVertexFn: Shader[E, Unit], reference: E): Shader[VertexEnv, Unit] =
    Shader[VertexEnv] { env =>
      @layout(0) @in val a_verticesAndCoords: vec4    = null
      @layout(1) @in val a_translateScale: vec4       = null
      @layout(2) @in val a_refFlip: vec4              = null
      @layout(3) @in val a_sizeAndFrameScale: vec4    = null
      @layout(4) @in val a_channelOffsets01: vec4     = null
      @layout(5) @in val a_channelOffsets23: vec4     = null
      @layout(6) @in val a_textureSizeAtlasSize: vec4 = null
      @layout(7) @in val a_rotation: Float            = 0.0f

      ubo[IndigoProjectionData]
      ubo[IndigoFrameData]
      ubo[IndigoCloneReferenceData]

      @uniform val u_baseTransform: mat4 = null
      @uniform val u_mode: Int           = 0

      @out var v_channel_coords_01: vec4    = null // Scaled to position on texture atlas
      @out var v_channel_coords_23: vec4    = null // Scaled to position on texture atlas
      @out var v_uv_size: vec4              = null // Unscaled texture coordinates + Width / height of the objects
      @out var v_screenCoordsRotation: vec3 = null // Where is this pixel on the screen? How much is it rotated by
      @out var v_textureSize: vec2          = null // Actual size of the texture in pixels.
      @out var v_atlasSizeAsUV: vec4 =
        null // Actual size of the atlas in pixels, and it's relative size in UV coords.
      @out var v_channel_pos_01: vec4  = null // Position on the atlas of channels 0 and 1.
      @out var v_channel_pos_23: vec4  = null // Position on the atlas of channels 2 and 3.
      @flat @out var v_instanceId: Int = 0    // The current instance id
      // flat out int v_instanceId // The current instance id

      // Constants
      @const val PI: Float    = 3.141592653589793f
      @const val PI_2: Float  = PI * 0.5f
      @const val PI_4: Float  = PI * 0.25f
      @const val TAU: Float   = 2.0f * PI
      @const val TAU_2: Float = PI
      @const val TAU_4: Float = PI_2
      @const val TAU_8: Float = PI_4

      // Variables
      @global var ATLAS_SIZE: vec2               = null
      @global var VERTEX: vec4                   = null
      @global var TEXTURE_SIZE: vec2             = null
      @global var UV: vec2                       = null
      @global var SIZE: vec2                     = null
      @global var FRAME_SIZE: vec2               = null
      @global var CHANNEL_0_ATLAS_OFFSET: vec2   = null
      @global var CHANNEL_1_ATLAS_OFFSET: vec2   = null
      @global var CHANNEL_2_ATLAS_OFFSET: vec2   = null
      @global var CHANNEL_3_ATLAS_OFFSET: vec2   = null
      @global var CHANNEL_0_TEXTURE_COORDS: vec2 = null
      @global var CHANNEL_1_TEXTURE_COORDS: vec2 = null
      @global var CHANNEL_2_TEXTURE_COORDS: vec2 = null
      @global var CHANNEL_3_TEXTURE_COORDS: vec2 = null
      @global var CHANNEL_0_POSITION: vec2       = null
      @global var CHANNEL_1_POSITION: vec2       = null
      @global var CHANNEL_2_POSITION: vec2       = null
      @global var CHANNEL_3_POSITION: vec2       = null
      @global var CHANNEL_0_SIZE: vec2           = null
      @global var POSITION: vec2                 = null
      @global var SCALE: vec2                    = null
      @global var REF: vec2                      = null
      @global var FLIP: vec2                     = null
      @global var ROTATION: Float                = 0.0f
      @global var INSTANCE_ID: Int               = 0

      // format: off
      def translate2d(t: vec2): mat4 =
        mat4(1.0f, 0.0f, 0.0f, 0.0f,
             0.0f, 1.0f, 0.0f, 0.0f,
             0.0f, 0.0f, 1.0f, 0.0f,
             t.x,  t.y,  0.0f, 1.0f
        )

      // format: off
      def scale2d(s: vec2): mat4 =
        mat4(s.x,  0.0f, 0.0f, 0.0f,
             0.0f, s.y,  0.0f, 0.0f,
             0.0f, 0.0f, 1.0f, 0.0f,
             0.0f, 0.0f, 0.0f, 1.0f
        )

      // format: off
      def rotate2d(angle: Float): mat4 =
        mat4(cos(angle), -sin(angle), 0.0f, 0.0f,
             sin(angle), cos(angle),  0.0f, 0.0f,
             0.0f,       0.0f,        1.0f, 0.0f,
             0.0f,       0.0f,        0.0f, 1.0f
        )
      
      def scaleCoordsWithOffset(texcoord: vec2, offset: vec2): vec2 =
        val transform: mat4 = translate2d(offset) * scale2d(FRAME_SIZE)
        (transform * vec4(texcoord, 1.0f, 1.0f)).xy
      
      userVertexFn.run(reference)

      def main: Unit =
        INSTANCE_ID = env.gl_InstanceID

        VERTEX = vec4(a_verticesAndCoords.xy, 1.0f, 1.0f)
        UV = a_verticesAndCoords.zw
        ROTATION = a_rotation
        POSITION = a_translateScale.xy
        SCALE = a_translateScale.zw

        // 0 = normal, 1 = clone batch, 2 = clone tiles
        u_mode match
          case 0 =>
            ATLAS_SIZE = a_textureSizeAtlasSize.zw
            TEXTURE_SIZE = a_textureSizeAtlasSize.xy
            SIZE = a_sizeAndFrameScale.xy
            FRAME_SIZE = a_sizeAndFrameScale.zw
            REF = a_refFlip.xy
            FLIP = a_refFlip.zw
            CHANNEL_0_ATLAS_OFFSET = a_channelOffsets01.xy
            CHANNEL_1_ATLAS_OFFSET = a_channelOffsets01.zw
            CHANNEL_2_ATLAS_OFFSET = a_channelOffsets23.xy
            CHANNEL_3_ATLAS_OFFSET = a_channelOffsets23.zw

          case 1 =>
            ATLAS_SIZE = env.u_ref_textureSizeAtlasSize.zw
            TEXTURE_SIZE = env.u_ref_textureSizeAtlasSize.xy
            SIZE = env.u_ref_sizeAndFrameScale.xy
            FRAME_SIZE = env.u_ref_sizeAndFrameScale.zw
            REF = env.u_ref_refFlip.xy
            FLIP = env.u_ref_refFlip.zw
            CHANNEL_0_ATLAS_OFFSET = env.u_ref_channelOffsets01.xy
            CHANNEL_1_ATLAS_OFFSET = env.u_ref_channelOffsets01.zw
            CHANNEL_2_ATLAS_OFFSET = env.u_ref_channelOffsets23.xy
            CHANNEL_3_ATLAS_OFFSET = env.u_ref_channelOffsets23.zw

          case 2 =>
            ATLAS_SIZE = env.u_ref_textureSizeAtlasSize.zw
            TEXTURE_SIZE = env.u_ref_textureSizeAtlasSize.xy
            SIZE = a_sizeAndFrameScale.xy
            FRAME_SIZE = a_sizeAndFrameScale.zw
            REF = env.u_ref_refFlip.xy
            FLIP = env.u_ref_refFlip.zw
            CHANNEL_0_ATLAS_OFFSET = a_channelOffsets01.xy
            CHANNEL_1_ATLAS_OFFSET = a_channelOffsets01.zw
            CHANNEL_2_ATLAS_OFFSET = a_channelOffsets23.xy
            CHANNEL_3_ATLAS_OFFSET = a_channelOffsets23.zw

          case _ =>
            ()
          
        VERTEX = env.vertex(VERTEX)

        CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_0_ATLAS_OFFSET)
        CHANNEL_1_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_1_ATLAS_OFFSET)
        CHANNEL_2_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_2_ATLAS_OFFSET)
        CHANNEL_3_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_3_ATLAS_OFFSET)
        CHANNEL_0_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_0_ATLAS_OFFSET)
        CHANNEL_1_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_1_ATLAS_OFFSET)
        CHANNEL_2_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_2_ATLAS_OFFSET)
        CHANNEL_3_POSITION = scaleCoordsWithOffset(vec2(0.0f), CHANNEL_3_ATLAS_OFFSET)
        CHANNEL_0_SIZE = TEXTURE_SIZE / ATLAS_SIZE

        val transform: mat4 =
          translate2d(POSITION) *
          rotate2d(-1.0f * ROTATION) *
          scale2d(SIZE * SCALE) *
          translate2d(-(REF / SIZE) + 0.5f) *
          scale2d(vec2(1.0f, -1.0f) * FLIP)

        env.gl_Position = env.u_projection * u_baseTransform * transform * VERTEX

        val screenCoords: vec2 = env.gl_Position.xy * 0.5f + 0.5f
        v_screenCoordsRotation = vec3(vec2(screenCoords.x, 1.0f - screenCoords.y) * env.VIEWPORT_SIZE, ROTATION)

        v_uv_size = vec4(UV, SIZE)
        v_channel_coords_01 = vec4(CHANNEL_0_TEXTURE_COORDS, CHANNEL_1_TEXTURE_COORDS)
        v_channel_coords_23 = vec4(CHANNEL_2_TEXTURE_COORDS, CHANNEL_3_TEXTURE_COORDS)
        v_textureSize = TEXTURE_SIZE
        v_atlasSizeAsUV = vec4(ATLAS_SIZE, CHANNEL_0_SIZE)
        v_channel_pos_01 = vec4(CHANNEL_0_POSITION, CHANNEL_1_POSITION)
        v_channel_pos_23 = vec4(CHANNEL_2_POSITION, CHANNEL_3_POSITION)
        v_instanceId = INSTANCE_ID
      
    }

  inline def vertex[Env](inline userVertexFn: Shader[Env, Unit], env: Env): ShaderResult =
    vertexShader[Env](userVertexFn, env).toGLSL[IndigoUV.IndigoVertexPrinter](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  inline def vertexRawBody[Env](inline userVertexFn: Shader[Env, Unit], env: Env): ShaderResult =
    vertexShader[Env](userVertexFn, env).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  @nowarn("msg=discarded")
  val vertexTemplate: String => String =
    inline def tag = "//vertex_placeholder"
    inline def placeholder = Shader[IndigoUV.VertexEnv]{_ => RawGLSL(tag)}
    val renderedCode =
      vertexShader[IndigoUV.VertexEnv](placeholder, IndigoUV.VertexEnv.reference).toGLSL[WebGL2](
        ShaderHeader.Version300ES,
        ShaderHeader.PrecisionMediumPFloat
      ).toOutput.code

    val location = renderedCode.indexOf(tag)
    val start = renderedCode.substring(0, location)
    val end = renderedCode.substring(location + tag.length + 1)

    (insert: String) => start + insert + end

  protected type FragmentEnv = IndigoDynamicLightingData & UserDefined

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  @nowarn("msg=unused")
  @nowarn("msg=unset")
  inline def fragmentShader[E](
    inline userFragmentFn: Shader[E, Unit],
    inline userPrepareFn: Shader[E, Unit],
    inline userLightFn: Shader[E, Unit],
    inline userCompositeFn: Shader[E, Unit],
    reference: E
  ): Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>
      @layout(0) @out var fragColor: vec4 = null

      // ** Uniforms **
      // Currently we only ever bind one texture at a time.
      // The texture is however an atlas of textures, so in
      // practice you can read many sub-textures at once.
      // Could remove this limitation.
      @uniform val SRC_CHANNEL: sampler2D.type = sampler2D

      // public
      ubo[IndigoFrameData]
      ubo[IndigoDynamicLightingData]

      // ** Varyings **
      @in var v_channel_coords_01: vec4 = null
      @in var v_channel_coords_23: vec4 = null
      @in var v_uv_size: vec4 = null // Unscaled texture coordinates + Width / height of the objects
      @in var v_screenCoordsRotation: vec3 = null // Where is this pixel on the screen?
      @in var v_textureSize: vec2 = null // Actual size of the texture in pixels.
      @in var v_atlasSizeAsUV: vec4 = null // Actual size of the atlas in pixels, and it's relative size in UV coords.
      @in var v_channel_pos_01: vec4 = null // Position on the atlas of channels 0 and 1.
      @in var v_channel_pos_23: vec4 = null // Position on the atlas of channels 2 and 3.
      @flat @in var v_instanceId: Int = 0 // The current instance id

      // Variables
      @global var UV: vec2 = null // Unscaled texture coordinates
      @global var SIZE: vec2 = null // Width / height of the objects
      @global var CHANNEL_0: vec4 = null // Pixel value from texture channel 0
      @global var CHANNEL_1: vec4 = null // Pixel value from texture channel 1
      @global var CHANNEL_2: vec4 = null // Pixel value from texture channel 2
      @global var CHANNEL_3: vec4 = null // Pixel value from texture channel 3
      @global var CHANNEL_0_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
      @global var CHANNEL_1_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
      @global var CHANNEL_2_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
      @global var CHANNEL_3_TEXTURE_COORDS: vec2 = null // Scaled texture coordinates
      @global var CHANNEL_0_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
      @global var CHANNEL_1_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
      @global var CHANNEL_2_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
      @global var CHANNEL_3_POSITION: vec2 = null // top left position of this texture on the atlas in UV coords
      @global var CHANNEL_0_SIZE: vec2 = null // size of this texture on the atlas in UV coords
      @global var SCREEN_COORDS: vec2 = null
      @global var ROTATION: Float = 0.0f
      @global var TEXTURE_SIZE: vec2 = null // Size of the texture in pixels
      @global var ATLAS_SIZE: vec2 = null // Size of the atlas this texture is on, in pixels
      @global var INSTANCE_ID: Int = 0 // The current instance id

      @global var LIGHT_INDEX: Int = 0
      @global var LIGHT_COUNT: Int = 0
      @global var LIGHT_ACTIVE: Int = 0
      @global var LIGHT_TYPE: Int = 0
      @global var LIGHT_FAR_CUT_OFF: Int = 0
      @global var LIGHT_FALLOFF_TYPE: Int = 0
      @global var LIGHT_COLOR: vec4 = null
      @global var LIGHT_SPECULAR: vec4 = null
      @global var LIGHT_POSITION: vec2 = null
      @global var LIGHT_ROTATION: Float = 0.0f
      @global var LIGHT_NEAR: Float = 0.0f
      @global var LIGHT_FAR: Float = 0.0f
      @global var LIGHT_ANGLE: Float = 0.0f
      @global var LIGHT_INTENSITY: Float = 0.0f

      // Constants
      @const val PI: Float    = 3.141592653589793f
      @const val PI_2: Float  = PI * 0.5f
      @const val PI_4: Float  = PI * 0.25f
      @const val TAU: Float   = 2.0f * PI
      @const val TAU_2: Float = PI
      @const val TAU_4: Float = PI_2
      @const val TAU_8: Float = PI_4

      // Outputs
      @global var COLOR: vec4 = null

      userFragmentFn.run(reference)

      userPrepareFn.run(reference)

      userLightFn.run(reference)

      userCompositeFn.run(reference)

      // Prevents illegal forward reference warning from ultraviolet validater.
      def _indigoProcessLight_(): Unit =
        env.light()

      def main: Unit =

        INSTANCE_ID = v_instanceId

        // Defaults
        UV = v_uv_size.xy
        SIZE = v_uv_size.zw
        COLOR = vec4(0.0f)

        SCREEN_COORDS = v_screenCoordsRotation.xy
        ROTATION = v_screenCoordsRotation.z
        TEXTURE_SIZE = v_textureSize
        ATLAS_SIZE = v_atlasSizeAsUV.xy
        CHANNEL_0_POSITION = v_channel_pos_01.xy
        CHANNEL_1_POSITION = v_channel_pos_01.zw
        CHANNEL_2_POSITION = v_channel_pos_23.xy
        CHANNEL_3_POSITION = v_channel_pos_23.zw
        CHANNEL_0_SIZE = v_atlasSizeAsUV.zw

        CHANNEL_0_TEXTURE_COORDS = min(v_channel_coords_01.xy, CHANNEL_0_POSITION + CHANNEL_0_SIZE)
        CHANNEL_1_TEXTURE_COORDS = min(v_channel_coords_01.zw, CHANNEL_1_POSITION + CHANNEL_0_SIZE)
        CHANNEL_2_TEXTURE_COORDS = min(v_channel_coords_23.xy, CHANNEL_2_POSITION + CHANNEL_0_SIZE)
        CHANNEL_3_TEXTURE_COORDS = min(v_channel_coords_23.zw, CHANNEL_3_POSITION + CHANNEL_0_SIZE)
        CHANNEL_0 = texture2D(SRC_CHANNEL, CHANNEL_0_TEXTURE_COORDS)
        CHANNEL_1 = texture2D(SRC_CHANNEL, CHANNEL_1_TEXTURE_COORDS)
        CHANNEL_2 = texture2D(SRC_CHANNEL, CHANNEL_2_TEXTURE_COORDS)
        CHANNEL_3 = texture2D(SRC_CHANNEL, CHANNEL_3_TEXTURE_COORDS)

        // Colour - build up the COLOR
        COLOR = env.fragment(COLOR)

        // Lighting - prepare, light, composite
        env.prepare()

        LIGHT_COUNT = min(8, max(0, round(env.numOfLights).toInt))
        
        _for(0, _ < LIGHT_COUNT, _ + 1) { i =>
          LIGHT_INDEX = i
          LIGHT_ACTIVE = round(env.lightFlags(i).x).toInt
          LIGHT_TYPE = round(env.lightFlags(i).y).toInt
          LIGHT_FAR_CUT_OFF = round(env.lightFlags(i).z).toInt
          LIGHT_FALLOFF_TYPE = round(env.lightFlags(i).w).toInt
          LIGHT_COLOR = env.lightColor(i)
          LIGHT_SPECULAR = env.lightSpecular(i)
          LIGHT_POSITION = env.lightPositionRotation(i).xy
          LIGHT_ROTATION = env.lightPositionRotation(i).z
          LIGHT_NEAR = env.lightNearFarAngleIntensity(i).x
          LIGHT_FAR = env.lightNearFarAngleIntensity(i).y
          LIGHT_ANGLE = env.lightNearFarAngleIntensity(i).z
          LIGHT_INTENSITY = env.lightNearFarAngleIntensity(i).w

          _indigoProcessLight_()
        }

        // Composite - COMBINE COLOR + Lighting into final pixel color.
        env.composite()
        
        fragColor = COLOR
      }

  @nowarn("msg=unused")
  inline def noopPrepare[E]: Shader[E, Unit] =
    Shader[E] { _ =>
      def prepare: Unit = ()
    }

  @nowarn("msg=unused")
  inline def noopLight[E]: Shader[E, Unit] =
    Shader[E] { _ =>
      def light: Unit = ()
    }

  @nowarn("msg=unused")
  inline def noopComposite[E]: Shader[E, Unit] =
    Shader[E] { _ =>
      def composite: Unit = ()
    }

  inline def fragment[Env](
    inline userFragmentFn: Shader[Env, Unit],
    inline userPrepareFn: Shader[Env, Unit],
    inline userLightFn: Shader[Env, Unit],
    inline userCompositeFn: Shader[Env, Unit],
    env: Env
  ): ShaderResult =
    fragmentShader[Env](
      userFragmentFn,
      userPrepareFn,
      userLightFn,
      userCompositeFn,
      env
    ).toGLSL[IndigoUV.IndigoFragmentPrinter](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  inline def fragment[Env](
    inline userFragmentFn: Shader[Env, Unit],
    env: Env
  ): ShaderResult =
    fragmentShader(
      userFragmentFn,
      noopPrepare,
      noopLight,
      noopComposite,
      env
    ).toGLSL[IndigoUV.IndigoFragmentPrinter](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  inline def fragmentRawBody(
    inline userFragmentFn: Shader[Unit, Unit],
    inline userPrepareFn: Shader[Unit, Unit],
    inline userLightFn: Shader[Unit, Unit],
    inline userCompositeFn: Shader[Unit, Unit]
  ): ShaderResult =
    fragmentShader(
      userFragmentFn,
      userPrepareFn,
      userLightFn,
      userCompositeFn,
      ()
    ).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  inline def fragmentRawBody(
    inline userFragmentFn: Shader[Unit, Unit],
  ): ShaderResult =
    fragmentShader(
      userFragmentFn,
      noopPrepare,
      noopLight,
      noopComposite,
      ()
    ).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  @nowarn("msg=discarded")
  val fragmentTemplate: String => String =
    inline def tag = "//fragment_placeholder"
    inline def placeholder = Shader[IndigoUV.FragmentEnv]{_ => RawGLSL(tag)}
    inline def empty = Shader[IndigoUV.FragmentEnv]{_ => RawGLSL("//")}
    val renderedCode = fragmentShader(placeholder, empty, empty, empty, IndigoUV.FragmentEnv.reference).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    ).toOutput.code

    val location = renderedCode.indexOf(tag)
    val start = renderedCode.substring(0, location)
    val end = renderedCode.substring(location + tag.length + 1)

    (insert: String) => start + insert + end
