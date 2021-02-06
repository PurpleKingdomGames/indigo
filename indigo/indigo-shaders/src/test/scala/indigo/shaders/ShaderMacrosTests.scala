package indigo.shaders

class ShaderMacrosTests extends munit.FunSuite {

  test("glsl") {

    val out = ShaderMacros.toGLSL((x: Float) => x + 1.0f)

    println(out)

    assert(1 == 1)
  }

  // test("blah") {

  //   val x = 0
  //   val y = 1
  //   val z = 2

  //   println("--")

  //   ShaderMacros.debugSingle(x)
  //   ShaderMacros.debugSingle(x + y)

  //   println("--")

  //   ShaderMacros.debug(x)
  //   ShaderMacros.debug(x, y)
  //   ShaderMacros.debug(x + y)
  //   ShaderMacros.debug(x, x + y)
  //   ShaderMacros.debug("A", x, x + y)
  //   ShaderMacros.debug("A", x, "B", y)
  //   ShaderMacros.debug(x, y, z)

  //   println("--")

  //   ShaderMacros.debug((x: Int) => x * 10)

  //   assert(1 == 1)
  // }

}
