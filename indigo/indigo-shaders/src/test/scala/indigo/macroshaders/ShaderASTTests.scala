package indigo.macroshaders

import ShaderDSL.*
import ShaderDSL.glsl.*
import ShaderMacros.*

class ShaderASTTests extends munit.FunSuite {

  import ShaderAST.*

  test("Build an Indigo compatible fragment shader AST") {
    val expected: ShaderAST =
      FragmentProgram(
        Block(
          DataTypes.rgba(1.0f, 1.0f, 0.0f, 1.0f)
        )
      )

    // Constants must be inline'd
    inline def outColor = rgba(1.0f, 1.0f, 0.0f, 1.0f)

    val actual =
      ShaderMacros.toAST(IndigoFrag(_ => outColor))

    assertEquals(actual, expected)
  }

  test("Can render a fragment shader AST") {
    val expected: String =
      s"""//<indigo-fragment>
      |void fragment() {
      |  COLOR = rgba(1.0f, 1.0f, 0.0f, 1.0f);
      |}
      |//</indigo-fragment>
      |""".stripMargin

    val actual =
      ShaderMacros.toAST(IndigoFrag(_ => rgba(1.0f, 1.0f, 0.0f, 1.0f))).render

    assertEquals(actual, expected)
  }

}
