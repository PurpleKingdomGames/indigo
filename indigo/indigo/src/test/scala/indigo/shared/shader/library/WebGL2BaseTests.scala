package indigo.shared.shader.library

import ultraviolet.syntax.*

class WebGL2BaseTests extends munit.FunSuite {

  test("Base WebGL 2.0 vertex shader") {

    inline def modifyVertex: vec4 => Shader[IndigoUV.IndigoVertexEnv, vec4] =
      (input: vec4) =>
        Shader[IndigoUV.IndigoVertexEnv, vec4] { _ =>
          input + vec4(1.0f)
        }

    val actual =
      WebGL2Base.vertex(modifyVertex).toOutput.code

    val expected1: String =
      """
      |vec4 def0(in vec4 input){
      |  return input+vec4(1.0);
      |}
      |""".stripMargin.trim

    val expected2: String =
      """
      |void vertex(){
      |  VERTEX=def0(VERTEX);
      |}
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
    assert(clue(actual).contains(clue(expected2)))
  }

  test("Base WebGL 2.0 fragment shader") {

    inline def modifyColor: vec4 => Shader[IndigoUV.IndigoFragmentEnv, vec4] =
      (input: vec4) =>
        Shader[IndigoUV.IndigoFragmentEnv, vec4] { _ =>
          input + vec4(1.0f)
        }

    val actual =
      WebGL2Base.fragment(modifyColor).toOutput.code

    val expected1: String =
      """
      |vec4 def0(in vec4 input){
      |  return input+vec4(1.0);
      |}
      |""".stripMargin.trim

    val expected2: String =
      """
      |void fragment(){
      |  COLOR=def0(COLOR);
      |}
      |""".stripMargin.trim

    assert(clue(actual).contains(clue(expected1)))
    assert(clue(actual).contains(clue(expected2)))
  }

}
