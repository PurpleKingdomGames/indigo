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

  test("toUniformBlock derives with array") {

    final case class Foo(z: Array[Float]) derives ToUniformBlock

    val actual = convert(Foo(Array(1.0f, 2.0f, 3.0f)))

    val expected =
      UniformBlock(
        UniformBlockName("Foo"),
        Uniform("z") -> ShaderPrimitive.rawArray(1.0f, 2.0f, 3.0f)
      )

    assertEquals(actual.uniformHash, expected.uniformHash)
    assertEquals(
      actual.uniforms.map(_._2.toArray.map(_.toString).mkString(", ")),
      expected.uniforms.map(_._2.toArray.map(_.toString).mkString(", "))
    )
  }

}
