package indigo.shaders

class ShaderDSLTests extends munit.FunSuite {

  test("expression / addition") {
    val actual =
      vec2(1, 2) + (vec3(3, 4, 5) * vec4(6, 7, 8, 9)) / float(10)

    val expected =
      "vec2(1.0, 2.0) + ((vec3(3.0, 4.0, 5.0) * vec4(6.0, 7.0, 8.0, 9.0)) / 10.0)"

    assertEquals(actual.render, expected)
  }

  // test("") {

  //   val glsl1 = vec2(1, 2) + (vec3(3, 4, 5) * vec4(6, 7, 8, 9)) / float(10)
  //   glsl1.render

  //   val glsl2 = Abs(Min(float(1), Max(float(0), vec3(1, 2, 3))))
  //   glsl2.render

  //   val glsl3 =
  //     routine("foo", Ref("color"), Ref("time")) {
  //       vec3(3, 4, 5) * vec4(6, 7, 8, 9)
  //     }

  //   glsl3.render

  //   assert(1 == 2)

  // }

}
