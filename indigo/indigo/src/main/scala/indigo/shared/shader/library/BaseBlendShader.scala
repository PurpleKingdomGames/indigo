package indigo.shared.shader.library

import indigo.shared.shader.RawShaderCode
import indigo.shared.shader.ShaderId
import ultraviolet.datatypes.ShaderResult
import ultraviolet.syntax.*

object BaseBlendShader:

  private case class IndigoMergeData(u_projection: mat4, u_scale: vec2)
  private case class IndigoFrameData(
      TIME: highp[Float], // Running time
      VIEWPORT_SIZE: vec2 // Size of the viewport in pixels
  )

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private case class VertexEnv(var gl_Position: vec4)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  inline def vertex(inline modifyVertex: vec4 => Shader[Unit, vec4]): ShaderResult =
    Shader[IndigoMergeData & VertexEnv] { env =>

      @layout(0) @in val a_verticesAndCoords: vec4 = null

      ubo[IndigoMergeData]
      ubo[IndigoFrameData]

      @out var SIZE: vec2 = null
      @out var UV: vec2   = null

      @const val PI: Float    = 3.141592653589793f
      @const val PI_2: Float  = PI * 0.5f
      @const val PI_4: Float  = PI * 0.25f
      @const val TAU: Float   = 2.0f * PI
      @const val TAU_2: Float = PI
      @const val TAU_4: Float = PI_2
      @const val TAU_8: Float = PI_4

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

      @global var VERTEX: vec4 = null

      def vertex(): Unit =
        VERTEX = modifyVertex(VERTEX).run(())

      def main: Unit =
        UV = a_verticesAndCoords.zw
        SIZE = env.u_scale
        VERTEX = vec4(a_verticesAndCoords.x, a_verticesAndCoords.y, 1.0f, 1.0f)

        vertex()

        val moveToTopLeft: vec2 = SIZE / 2.0f
        val transform: mat4 = translate2d(moveToTopLeft) * scale2d(SIZE)

        env.gl_Position = env.u_projection * transform * VERTEX
    }.toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  inline def fragment(inline modifyColor: vec4 => Shader[Unit, vec4]): ShaderResult =
    Shader {

      @in val SIZE: vec2 = null // In this case, screen size.
      @in val UV: vec2 = null // Unscaled texture coordinates

      @uniform val SRC_CHANNEL: sampler2D.type = sampler2D
      @uniform val DST_CHANNEL: sampler2D.type = sampler2D

      @out var fragColor: vec4 = null

      ubo[IndigoFrameData]

      // Constants
      @const val PI: Float    = 3.141592653589793f
      @const val PI_2: Float  = PI * 0.5f
      @const val PI_4: Float  = PI * 0.25f
      @const val TAU: Float   = 2.0f * PI
      @const val TAU_2: Float = PI
      @const val TAU_4: Float = PI_2
      @const val TAU_8: Float = PI_4

      @global var SRC: vec4 = null // Pixel value from SRC texture
      @global var DST: vec4 = null // Pixel value from DST texture

      // Output
      @global var COLOR: vec4 = null

      def fragment(): Unit =
        COLOR = modifyColor(COLOR).run(())

      def main: Unit =
        SRC = texture2D(SRC_CHANNEL, UV)
        DST = texture2D(DST_CHANNEL, UV)
        COLOR = vec4(0.0f)

        // Colour
        fragment()

        fragColor = COLOR
      
    }.toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )
