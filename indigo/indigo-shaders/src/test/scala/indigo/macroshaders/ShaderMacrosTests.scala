package indigo.macroshaders

class ShaderMacrosTests extends munit.FunSuite {

  test("Convert Float => Float to GLSL") {

    val actual =
      ShaderMacros.toGLSL("addOne", (x: Float) => x + 1.0f)

    val expected =
      s"""
      |float addOne(float x) {
      |  return x + 1.0;
      |}
      |""".stripMargin

    assertEquals(actual, expected)
  }

}
