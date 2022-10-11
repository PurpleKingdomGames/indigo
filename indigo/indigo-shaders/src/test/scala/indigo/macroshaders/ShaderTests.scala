package indigo.macroshaders

import ShaderDSL.*
import ShaderDSL.glsl.*
import ShaderMacros.*

class ShaderTests extends munit.FunSuite {

  test("build an Indigo compatible fragment shader") {
    val expected: ShaderAST =
      ShaderAST.DataTypes.rgba(1.0f, 1.0f, 0.0f, 1.0f)

    // Constants must be inline'd
    inline def outColor = rgba(1.0f, 1.0f, 0.0f, 1.0f)

    // println(">> " + ShaderMacros.showASTExpr(ShaderASTSample.sample))

    val actual =
      ShaderMacros.toAST(IndigoFrag(_ => outColor))

    // println(actual)

    assertEquals(actual, expected)
  }

}
