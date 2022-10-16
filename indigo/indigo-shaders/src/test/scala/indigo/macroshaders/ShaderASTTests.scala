package indigo.macroshaders

import ShaderDSL.*
import ShaderMacros.*

class ShaderASTTests extends munit.FunSuite {

  import ShaderAST.*

  test("Build an Indigo compatible fragment shader AST") {
    val expected: ShaderAST =
      FragmentProgram(
        Block(
          DataTypes.vec4(
            List(DataTypes.float(1.0f), DataTypes.float(1.0f), DataTypes.float(0.0f), DataTypes.float(1.0f))
          )
        )
      )

    trait FragEnv {
      val UV: vec2
    }

    inline def fragment: ShaderContext[FragEnv, rgba] =
      ShaderContext(env => Program(rgba(env.UV, 0.0f, 1.0f)))

    val actual =
      ShaderMacros.toAST(fragment)

    println(">>>>")
    println(ShaderMacros.toAST(fragment).render)
    println("<<<<")

    assertEquals(actual, expected)
  }

  // test("Build an Indigo compatible fragment shader AST") {
  //   val expected: ShaderAST =
  //     FragmentProgram(
  //       Block(
  //         DataTypes.rgba(1.0f, 1.0f, 0.0f, 1.0f)
  //       )
  //     )

  //   // Constants must be inline'd
  //   inline def outColor = rgba(1.0f, 1.0f, 0.0f, 1.0f)

  //   val actual =
  //     ShaderMacros.toAST(IndigoFrag(_ => outColor))

  //   assertEquals(actual, expected)
  // }

  // test("Can render a fragment shader AST") {
  //   val expected: String =
  //     s"""//<indigo-fragment>
  //     |void fragment() {
  //     |  COLOR = rgba(1.0, 1.0, 0.0, 1.0);
  //     |}
  //     |//</indigo-fragment>
  //     |""".stripMargin

  //   val actual =
  //     ShaderMacros.toAST(IndigoFrag(_ => rgba(1.0f, 1.0f, 0.0f, 1.0f))).render

  //   assertEquals(actual, expected)
  // }

}
