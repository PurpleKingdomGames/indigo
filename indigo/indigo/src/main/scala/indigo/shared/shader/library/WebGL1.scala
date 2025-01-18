package indigo.shared.shader.library

import indigo.shared.shader.RawShaderCode
import indigo.shared.shader.ShaderId
import ultraviolet.syntax.*

import scala.annotation.nowarn

object WebGL1 extends RawShaderCode:

  val id: ShaderId = ShaderId("indigo_default_WebGL1")

  val vertex: String =
    WebGL1BaseShaders.vertex.output.toOutput.code

  val fragment: String =
    WebGL1BaseShaders.fragment.output.toOutput.code

object WebGL1BaseShaders:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
  @nowarn("msg=unused")
  object vertex:
    inline def shader =
      Shader[WebGL1Env] { env =>
        @attribute val a_verticesAndCoords: vec4 = null

        @uniform val u_projection: mat4     = null
        @uniform val u_translateScale: vec4 = null
        @uniform val u_refRotation: vec4    = null
        @uniform val u_frameTransform: vec4 = null
        @uniform val u_sizeFlip: vec4       = null
        @uniform val u_baseTransform: mat4  = null

        @in var v_texcoord: vec2 = null

        def translate2d(t: vec2): mat4 =
          mat4(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, t.x, t.y, 0f, 1f)

        def scale2d(s: vec2): mat4 =
          mat4(s.x, 0f, 0f, 0f, 0f, s.y, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        def rotate2d(angle: Float): mat4 =
          mat4(cos(angle), -sin(angle), 0f, 0f, sin(angle), cos(angle), 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        def scaleTexCoordsWithOffset(texcoord: vec2, offset: vec2): vec2 =
          val transform: mat4 = translate2d(offset) * scale2d(u_frameTransform.zw)
          (transform * vec4(texcoord, 1.0f, 1.0f)).xy

        def scaleTexCoords(texcoord: vec2): vec2 =
          scaleTexCoordsWithOffset(texcoord, u_frameTransform.xy)

        def main(): Unit =
          val vertices: vec4  = vec4(a_verticesAndCoords.xy, 1.0f, 1.0f);
          val texcoords: vec2 = a_verticesAndCoords.zw;

          val ref: vec2                  = u_refRotation.xy;
          val size: vec2                 = u_sizeFlip.xy;
          val flip: vec2                 = u_sizeFlip.zw;
          val translation: vec2          = u_translateScale.xy;
          val scale: vec2                = u_translateScale.zw;
          val rotation: Float            = u_refRotation.w;
          val moveToReferencePoint: vec2 = -(ref / size) + 0.5f;

          val transform: mat4 =
            translate2d(translation) *
              rotate2d(-1.0f * rotation) *
              scale2d(size * scale) *
              translate2d(moveToReferencePoint) *
              scale2d(vec2(1.0f, -1.0f) * flip);

          env.gl_Position = u_projection * u_baseTransform * transform * vertices;

          v_texcoord = scaleTexCoords(texcoords);
      }

    val output = shader.toGLSL[WebGL1]

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  @nowarn("msg=unused")
  object fragment:
    inline def shader =
      Shader[WebGL1Env] { env =>
        @uniform val u_texture: sampler2D.type = sampler2D
        @in val v_texcoord: vec2               = null

        def main: Unit =
          env.gl_FragColor = texture2D(u_texture, v_texcoord)
      }

    val output = shader.toGLSL[WebGL1](ShaderHeader.PrecisionMediumPFloat)
