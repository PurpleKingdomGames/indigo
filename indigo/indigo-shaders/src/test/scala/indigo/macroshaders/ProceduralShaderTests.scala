package indigo.macroshaders

class ProceduralShaderTests extends munit.FunSuite {
  import ShaderAST.*
  import ShaderAST.DataTypes.*

  test("exists: whole result == search") {

    val actual =
      ProceduralShader(Nil, vec4(List(float(1), float(1), float(0), float(1))))
        .exists(_ == vec4(List(float(1), float(1), float(0), float(1))))

    assert(actual)

  }

  test("exists: component in vec4") {

    val actual =
      ProceduralShader(Nil, vec4(List(float(1), float(1), float(0), float(1))))
        .exists(_ == float(1))

    assert(actual)

  }

  test("exists") {

    val actual =
      ProceduralShaderSamples.sample1.exists(_ == vec4(List(float(1), float(1), float(0), float(1))))

    assert(actual)

  }

}

object ProceduralShaderSamples:

  import ShaderAST.*
  import ShaderAST.DataTypes.*

  val sample1 =
    ProceduralShader(
      List(
        Function(
          "fn0",
          List("env"),
          Block(List(NamedBlock("", "Program", List(vec4(List(float(1), float(1), float(0), float(1))))))),
          None
        )
      ),
      NamedBlock("", "Shader", List(Block(List(Block(List(CallFunction("fn0", Nil, List("env"), None), Empty()))))))
    )
