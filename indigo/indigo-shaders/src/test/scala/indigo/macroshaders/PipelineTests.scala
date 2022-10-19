package indigo.macroshaders

class PipelineTests extends munit.FunSuite {

  val f: Int => String =
    (i: Int) => "count: " + i.toString

  val g: String => Boolean =
    (s: String) => s.length > 10

  val x: Int => Boolean =
    (i: Int) => i > 10

  test("lift / apply / arr (construction)") {
    assertEquals((Program(10) |> Pipeline(f)).run, "count: 10")
    assertEquals((Program(20) |> Pipeline.arr(f)).run, "count: 20")
    assertEquals((Program(30) |> Pipeline.lift(f)).run, "count: 30")
  }

  test("andThen / >>>") {
    assertEquals((Program(10) |> (Pipeline(f) andThen Pipeline(g))).run, false)
    assertEquals((Program(10000) |> (Pipeline(f) >>> Pipeline(g))).run, true)
  }

  test("parallel / &&& / and") {
    assertEquals(
      (Program(100) |> (Pipeline(f) and Pipeline(x))).run,
      ("count: 100", true)
    )
    assertEquals((Program(1) |> (Pipeline(f) &&& Pipeline(x))).run, ("count: 1", false))
  }

  test("Pipeline should be able to compose pipeline functions") {
    val f = Pipeline.lift((i: Int) => s"$i")
    val g = Pipeline.lift((s: String) => s.length < 2)

    val h: Pipeline[Int, Boolean] = f andThen g

    assertEquals(h.runWith(Program(1)).run, true)
    assertEquals(h.runWith(Program(1000)).run, false)
  }

  test("Pipeline should be able to run pipeline functions and parallel") {
    val f = Pipeline.lift((i: Int) => s"$i")
    val g = Pipeline.lift((i: Int) => i < 10)

    val h: Pipeline[Int, (String, Boolean)] = f and g

    assertEquals(h.runWith(Program(1)).run, ("1", true))
    assertEquals(h.runWith(Program(1000)).run, ("1000", false))
  }
}
