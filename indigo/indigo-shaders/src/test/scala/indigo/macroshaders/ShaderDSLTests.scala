package indigo.macroshaders

import ShaderDSL.*

class ShaderDSLTests extends munit.FunSuite {

  import ShaderAST.*

  test("Program: apply & run") {
    val f = Program[Int]("count me".length)
    assert(f.run == 8)
  }

  test("Program: pure & run") {
    val f = Program.pure[Int](10)
    assert(f.run == 10)
  }

  test("Program: map") {
    val f = Program.pure[Int](10).map(_ * 10)
    assert(f.run == 100)
  }

  test("Program: ap") {
    val f = Program.pure[Int](10).ap(Program.pure[Int => String](_.toString))
    assert(f.run == "10")
  }

  test("Program: flatten") {
    val f = Program.pure[Program[Int]](
      Program.pure[Int](10)
    )
    assert(f.flatten.run == 10)
  }

  test("Program: flatMap") {
    val f = Program.pure[Int](10).flatMap(i => Program.pure[Int](i * 10))
    assert(f.run == 100)
  }

}
