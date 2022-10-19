package indigo.macroshaders

class ProgramTests extends munit.FunSuite {

  test("apply & run") {
    val f = Program[Int](8)
    assert(f.run == 8)
  }

  test("pure & run") {
    val f = Program.pure[Int](10)
    assert(f.run == 10)
  }

  test("map") {
    val f = Program.pure[Int](10).map(_ * 10)
    assert(f.run == 100)
  }

  test("ap") {
    val f = Program.pure[Int](10).ap(Program.pure[Int => String](_.toString))
    assert(f.run == "10")
  }

  test("flatten") {
    val f = Program.pure[Program[Int]](
      Program.pure[Int](10)
    )
    assert(f.flatten.run == 10)
  }

  test("flatMap") {
    val f = Program.pure[Int](10).flatMap(i => Program.pure[Int](i * 10))
    assert(f.run == 100)
  }

  test("combine") {
    val f = Program.pure[Int](10) |*| Program(20)
    assert(f.run == (10, 20))
  }

  test("merge") {
    val f = (Program.pure[Int](10) merge Program(20))(_ + _)
    assert(f.run == 30)
  }

  test("pipe") {
    val f = Program.pure[Int](10) |> Pipeline(_ + 20)
    assert(f.run == 30)
  }

}
