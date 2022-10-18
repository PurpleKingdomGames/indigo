package indigo.macroshaders

import ShaderDSL.*
import ShaderMacros.*

class ShaderASTTests extends munit.FunSuite {

  import ShaderAST.*

  test("Build an Indigo compatible fragment shader AST") {
    val expected: ShaderAST =
      Block(
        DataTypes.vec4(
          List(DataTypes.float(1.0f), DataTypes.float(1.0f), DataTypes.float(0.0f), DataTypes.float(1.0f))
        )
      )

    trait FragEnv {
      val UV: vec2
    }

    inline def fragment: Shader[FragEnv, rgba] =
      Shader { env =>
        val zero  = 0.0f
        val alpha = 1.0f
        Program(rgba(env.UV, zero, alpha))
      }

    val actual =
      ShaderMacros.toAST(fragment)

    println(">>>>")
    println(ShaderMacros.toAST(fragment).toString)
    println("----")
    println(ShaderMacros.toAST(fragment).render)
    println("<<<<")

    assertEquals(actual, expected)
  }

}
