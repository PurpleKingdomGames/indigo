package indigo.shared.shader

class ToUniformBlockTests extends munit.FunSuite {

  def convert[A](value: A)(using c: ToUniformBlock[A]): UniformBlock =
    c.toUniformBlock(value)

  test("toUniformBlock derives") {

    final case class Foo(x: Float, y: Float) derives ToUniformBlock

    val actual = convert(Foo(10, 20.0f))

    val expected =
      UniformBlock(
        UniformBlockName("Foo"),
        Uniform("x") -> ShaderPrimitive.float(10),
        Uniform("y") -> ShaderPrimitive.float(20.0f)
      )

    assertEquals(actual, expected)
  }

}
