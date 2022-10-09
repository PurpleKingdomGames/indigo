package indigo.macroshaders

import ShaderDSL.*
import ShaderDSL.glsl.*

class ShaderMacrosTests extends munit.FunSuite {

  test("Convert Float => Float to GLSL") {
    val actual =
      ShaderMacros.toGLSLString("addOne", (x: Float) => x + 1.0f)

    val expected =
      s"""
      |float addOne(float x) {
      |  return x + 1.0;
      |}
      |""".stripMargin

    assertEquals(actual, expected)
  }

  test("Make a fragment shader") {
    import ShaderDSL.*

    val f: Function1[Unit, rgba] = _ => rgba(1.0, 0.0, 0.0, 1.0)

    val actual =
      ShaderMacros.toFrag(() => rgba(1.0, 0.0, 0.0, 1.0))

    val expected =
      s"""
      |void fragment() {
      |  COLOR = vec4(1.0, 0.0, 0.0, 1.0);
      |}
      |""".stripMargin

    assertEquals(actual, expected)

  }

}
