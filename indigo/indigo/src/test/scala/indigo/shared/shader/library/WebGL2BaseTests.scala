package indigo.shared.shader.library

import ultraviolet.syntax.*

class WebGL2BaseTests extends munit.FunSuite {

  test("Base WebGL 1.0 vertex shader") {

    inline def modifyVertex: Shader[WebGL2Base.EnvIn, vec4] =
      Shader[WebGL2Base.EnvIn, vec4] { env =>
        env.VERTEX + vec4(1.0f)
      }

    val actual =
      WebGL2Base.vertex(modifyVertex)

    val expected: String =
      """
      |void vertex(){
      |  VERTEX=VERTEX+vec4(1.0);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

}
