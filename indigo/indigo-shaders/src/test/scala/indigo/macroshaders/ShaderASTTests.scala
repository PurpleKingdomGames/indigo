package indigo.macroshaders

import ShaderDSL.*
import ShaderMacros.*

class ShaderASTTests extends munit.FunSuite {

  import ShaderAST.*
  import ShaderAST.DataTypes.*

  test("Build an Indigo compatible fragment shader AST") {
    val expected: ProceduralShader =
      ProceduralShader(
        List(
          Function(
            "fn0",
            List("env"),
            Block(
              List(
                NamedBlock("", "Program", List(vec4(List(float(1), float(1), float(0), float(1)))))
              )
            )
          )
        ),
        NamedBlock(
          "",
          "Shader",
          List(
            Block(List(Block(List(CallFunction("fn0", List("env")), Empty()))))
          )
        )
      )

    trait FragEnv {
      val UV: vec2
    }

    inline def fragment: Shader[FragEnv, rgba] =
      Shader { env =>
        Program(rgba(1.0f, 1.0f, 0.0f, 1.0f))
      }

    val actual =
      ShaderMacros.toAST(fragment)

    assertEquals(actual, expected)
  }

}
