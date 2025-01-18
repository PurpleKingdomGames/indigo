package indigo.shared.shader.library

import indigo.shared.shader.BlendShader
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class BaseBlendShaderTests extends munit.FunSuite {

  test("Merge WebGL 2.0 vertex shader") {

    inline def modifyVertex: Shader[IndigoUV.VertexEnv, Unit] =
      Shader[IndigoUV.VertexEnv] { _ =>
        def vertex(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      BlendShader.vertex(modifyVertex, IndigoUV.VertexEnv.reference).toOutput.code

    val expected1: String =
      """
      |vec4 vertex(in vec4 v){
      |  return v+vec4(1.0);
      |}
      |""".stripMargin.trim

    val expected2: String =
      """
      |VERTEX=vertex(VERTEX);
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
    assert(clue(actual).contains(clue(expected2)))
  }

  test("Merge WebGL 2.0 vertex shader template") {

    val noop = NoOp.vertex.toGLSL[WebGL2].toOutput.code

    val actual =
      BlendShader.vertexTemplate(noop)

    val expected1: String =
      """
      |vec4 vertex(in vec4 v){
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
  }

  test("Merge WebGL 2.0 fragment shader") {

    inline def modifyColor: Shader[IndigoUV.BlendFragmentEnv, Unit] =
      Shader[IndigoUV.BlendFragmentEnv] { _ =>
        def fragment(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      BlendShader.fragment(modifyColor, IndigoUV.BlendFragmentEnv.reference).toOutput.code

    val expected1: String =
      """
      |vec4 fragment(in vec4 v){
      |  return v+vec4(1.0);
      |}
      |""".stripMargin.trim

    val expected2: String =
      """
      |COLOR=fragment(COLOR);
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
    assert(clue(actual).contains(clue(expected2)))
  }

}
