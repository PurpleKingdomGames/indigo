package indigo.shared.shader.library

import indigo.shared.shader.EntityShader
import ultraviolet.syntax.*

import scala.annotation.nowarn

@nowarn("msg=unused")
@nowarn("msg=discarded")
class BaseEntityShaderTests extends munit.FunSuite {

  test("Base WebGL 2.0 vertex shader") {

    inline def modifyVertex: Shader[IndigoUV.VertexEnv, Unit] =
      Shader[IndigoUV.VertexEnv] { _ =>
        def vertex(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      EntityShader.vertex(modifyVertex, IndigoUV.VertexEnv.reference).toOutput.code

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

  test("Base WebGL 2.0 vertex shader (Raw)") {

    inline def modifyVertex: Shader[IndigoUV.VertexEnv, Unit] =
      Shader[IndigoUV.VertexEnv] { _ =>
        RawGLSL(
          """
//#vertex_start
vec4 vertex(vec4 v){
  return v;
}
//#vertex_end"""
        )
      }

    val actual =
      EntityShader.vertexRawBody(modifyVertex, IndigoUV.VertexEnv.reference).toOutput.code

    // println(actual)

    val expected1: String =
      """
      |//#vertex_start
      |vec4 vertex(vec4 v){
      |  return v;
      |}
      |//#vertex_end;
      |""".stripMargin.trim

    val expected2: String =
      """
      |VERTEX=vertex(VERTEX);
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
    assert(clue(actual).contains(clue(expected2)))
  }

  test("Base WebGL 2.0 fragment shader") {

    inline def modifyColor: Shader[IndigoUV.FragmentEnv, Unit] =
      Shader[IndigoUV.FragmentEnv] { _ =>
        def fragment(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      EntityShader
        .fragment(modifyColor, IndigoUV.FragmentEnv.reference)
        .toOutput
        .code

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

    val expected3: String =
      """
      |void prepare(){}
      |""".stripMargin.trim

    val expected4: String =
      """
      |void light(){}
      |""".stripMargin.trim

    val expected5: String =
      """
      |void composite(){}
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
    assert(clue(actual).contains(clue(expected2)))
    assert(clue(actual).contains(clue(expected3)))
    assert(clue(actual).contains(clue(expected4)))
    assert(clue(actual).contains(clue(expected5)))
  }

}
