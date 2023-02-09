package indigo.shared.shader.library

import indigo.shared.shader.EntityShader
import ultraviolet.syntax.*

class BaseEntityShaderTests extends munit.FunSuite {

  test("Base WebGL 2.0 vertex shader") {

    inline def modifyVertex: Shader[IndigoUV.VertexEnv, Unit] =
      Shader[IndigoUV.VertexEnv] { _ =>
        def vertex(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      EntityShader.vertex(modifyVertex).toOutput.code

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

  test("Base WebGL 2.0 fragment shader") {

    inline def modifyColor: Shader[IndigoUV.FragmentEnv, Unit] =
      Shader[IndigoUV.FragmentEnv] { _ =>
        def fragment(v: vec4): vec4 =
          v + vec4(1.0f)
      }

    val actual =
      EntityShader.fragment(modifyColor).toOutput.code

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
