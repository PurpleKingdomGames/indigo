package indigo.shared.shader.library

import ultraviolet.datatypes.ShaderResult
import ultraviolet.syntax.*

import scala.annotation.nowarn

trait BaseBlendShader:

  protected case class IndigoMergeData(u_projection: mat4, u_scale: vec2)
  protected case class IndigoFrameData(
      TIME: highp[Float], // Running time
      VIEWPORT_SIZE: vec2 // Size of the viewport in pixels
  )

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  protected case class VertexEnv(var gl_Position: vec4)

  protected case class UserDefined():
    def vertex(v: vec4): vec4   = v
    def fragment(v: vec4): vec4 = v

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  @nowarn("msg=unused")
  inline def vertexShader[E](
      inline userVertexFn: Shader[E, Unit],
      reference: E
  ): Shader[IndigoMergeData & VertexEnv & UserDefined, Unit] =
    Shader[IndigoMergeData & VertexEnv & UserDefined] { env =>

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

      userVertexFn.run(reference)

      def main: Unit =
        UV = a_verticesAndCoords.zw
        SIZE = env.u_scale
        VERTEX = vec4(a_verticesAndCoords.x, a_verticesAndCoords.y, 1.0f, 1.0f)

        VERTEX = env.vertex(VERTEX)

        val moveToTopLeft: vec2 = SIZE / 2.0f
        val transform: mat4 = translate2d(moveToTopLeft) * scale2d(SIZE)

        env.gl_Position = env.u_projection * transform * VERTEX
    }

  inline def vertex[Env](inline userVertexFn: Shader[Env, Unit], env: Env): ShaderResult =
    vertexShader(userVertexFn, env).toGLSL[IndigoUV.IndigoVertexPrinter](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  inline def vertexRawBody(inline userVertexFn: Shader[Unit, Unit]): ShaderResult =
    vertexShader(userVertexFn, ()).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  @nowarn("msg=discarded")
  val vertexTemplate: String => String =
    inline def tag = "//vertex_placeholder"
    inline def placeholder = Shader[IndigoUV.VertexEnv]{_ => RawGLSL(tag)}
    val renderedCode = vertexShader(placeholder, IndigoUV.VertexEnv.reference).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    ).toOutput.code

    val location = renderedCode.indexOf(tag)
    val start = renderedCode.substring(0, location)
    val end = renderedCode.substring(location + tag.length + 1)

    (insert: String) => start + insert + end

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  @nowarn("msg=unused")
  inline def fragmentShader[E](inline userFragmentFn: Shader[E, Unit], reference: E): Shader[UserDefined, Unit] =
    Shader[UserDefined] { env =>

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

      userFragmentFn.run(reference)

      def main: Unit =
        SRC = texture2D(SRC_CHANNEL, UV)
        DST = texture2D(DST_CHANNEL, UV)
        COLOR = SRC

        // Colour
        COLOR = env.fragment(COLOR)

        fragColor = COLOR
      
    }

  inline def fragment[Env](inline userFragmentFn: Shader[Env, Unit], env: Env): ShaderResult =
    fragmentShader(userFragmentFn, env).toGLSL[IndigoUV.IndigoBlendFragmentPrinter](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  inline def fragmentRawBody(inline userFragmentFn: Shader[Unit, Unit]): ShaderResult =
    fragmentShader(userFragmentFn, ()).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    )

  @nowarn("msg=discarded")
  val fragmentTemplate: String => String =
    inline def tag = "//fragment_placeholder"
    inline def placeholder = Shader[IndigoUV.BlendFragmentEnv]{_ => RawGLSL(tag)}
    val renderedCode = fragmentShader(placeholder, IndigoUV.BlendFragmentEnv.reference).toGLSL[WebGL2](
      ShaderHeader.Version300ES,
      ShaderHeader.PrecisionMediumPFloat
    ).toOutput.code

    val location = renderedCode.indexOf(tag)
    val start = renderedCode.substring(0, location)
    val end = renderedCode.substring(location + tag.length + 1)

    (insert: String) => start + insert + end
