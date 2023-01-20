package indigo.shared.shader.library

import ultraviolet.syntax.*

class WebGL2MergeTests extends munit.FunSuite {

  test("Merge WebGL 2.0 vertex shader") {

    inline def modifyVertex: vec4 => Shader[Unit, vec4] =
      (input: vec4) =>
        Shader[Unit, vec4] { _ =>
          input + vec4(1.0f)
        }

    val actual =
      WebGL2Merge.vertex(modifyVertex).toOutput.code

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

  test("Merge WebGL 2.0 fragment shader") {

    inline def modifyColor: vec4 => Shader[Unit, vec4] =
      (input: vec4) =>
        Shader[Unit, vec4] { _ =>
          input + vec4(1.0f)
        }

    val actual =
      WebGL2Merge.fragment(modifyColor).toOutput.code

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
