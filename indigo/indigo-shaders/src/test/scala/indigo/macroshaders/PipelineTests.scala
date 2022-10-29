// package indigo.macroshaders

// class PipelineTests extends munit.FunSuite {

//   val f: Int => String =
//     (i: Int) => "count: " + i.toString

//   val g: String => Boolean =
//     (s: String) => s.length > 10

//   val x: Int => Boolean =
//     (i: Int) => i > 10

//   test("lift / apply / arr (construction)") {
//     assertEquals((Shader[String, Int](10) |> Pipeline(f)).run(""), "count: 10")
//     assertEquals((Shader[String, Int](20) |> Pipeline.arr(f)).run(""), "count: 20")
//     assertEquals((Shader[String, Int](30) |> Pipeline.lift(f)).run(""), "count: 30")
//   }

//   test("andThen / >>>") {
//     assertEquals((Shader[String, Int](10) |> (Pipeline(f) andThen Pipeline(g))).run(""), false)
//     assertEquals((Shader[String, Int](10000) |> (Pipeline(f) >>> Pipeline(g))).run(""), true)
//   }

//   test("parallel / &&& / and") {
//     assertEquals(
//       (Shader[String, Int](100) |> (Pipeline(f) and Pipeline(x))).run(""),
//       ("count: 100", true)
//     )
//     assertEquals((Shader[String, Int](1) |> (Pipeline(f) &&& Pipeline(x))).run(""), ("count: 1", false))
//   }

//   test("Pipeline should be able to compose pipeline functions") {
//     val f: Pipeline[String, Int, String]     = Pipeline((i: Int) => s"$i")
//     val g: Pipeline[String, String, Boolean] = Pipeline((s: String) => s.length < 2)

//     val h: Pipeline[String, Int, Boolean] = f andThen g

//     assertEquals(h.runWith(Shader[String, Int](1)).run(""), true)
//     assertEquals(h.runWith(Shader[String, Int](1000)).run(""), false)
//   }

//   test("Pipeline should be able to run pipeline functions and parallel") {
//     val f: Pipeline[String, Int, String]  = Pipeline.lift((i: Int) => s"$i")
//     val g: Pipeline[String, Int, Boolean] = Pipeline.lift((i: Int) => i < 10)

//     val h: Pipeline[String, Int, (String, Boolean)] = f and g

//     assertEquals(h.runWith(Shader[String, Int](1)).run(""), ("1", true))
//     assertEquals(h.runWith(Shader[String, Int](1000)).run(""), ("1000", false))
//   }

//   test("env is preserved.") {
//     // This is sort of a non-test, because running 'ask' here is just an identity function on the env.
//     assertEquals((Shader[String, Int](10) |> Pipeline(f)).ask("hello"), "hello")
//   }
// }
