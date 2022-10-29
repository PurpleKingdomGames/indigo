// package indigo.macroshaders

// class ShaderTests extends munit.FunSuite {

//   test("apply & run") {
//     val f = Shader[String, Int](_.length)
//     assert(f.run("count me") == 8)
//   }

//   test("pure & run") {
//     val f = Shader[String, Int](10)
//     assert(f.run("") == 10)
//   }

//   test("map") {
//     val f = Shader.pure[String, Int](10).map(_ * 10)
//     assert(f.run("") == 100)
//   }

//   test("ap") {
//     val f = Shader.pure[String, Int](10).ap(Shader.pure[String, Int => String](_.toString))
//     assert(f.run("") == "10")
//   }

//   test("flatten") {
//     val f = Shader.pure[String, Shader[String, Int]](
//       Shader.pure[String, Int](10)
//     )
//     assert(Shader.join(f).run("") == 10)
//     assert(f.flatten.run("") == 10)
//   }

//   test("flatMap") {
//     val f = Shader.pure[String, Int](10).flatMap(i => Shader.pure[String, Int](i * 10))
//     assert(f.run("") == 100)
//   }

//   test("ask") {
//     val f = Shader.pure[String, Int](10)
//     assert(f.ask.run("hello") == "hello")
//   }

//   test("asks") {
//     val f = Shader.pure[String, Int](10)
//     assert(f.asks((str: String) => str.length()).run("hello") == 5)
//   }

//   test("combine") {
//     val f = Shader.pure[String, Int](10) |*| Shader(20)
//     assert(f.run("") == (10, 20))
//   }

//   test("merge") {
//     val f = (Shader.pure[String, Int](10) merge Shader(20))(_ + _)
//     assert(f.run("") == 30)
//   }

//   test("pipe") {
//     val f = Shader.pure[String, Int](10) |> Pipeline(_ + 20)
//     assert(f.run("") == 30)
//   }

//   test("Can read environment") {
//     val f = Shader[String, List[String]](word => List.fill(3)(word))
//     assert(f("foo").length == 3)
//     assert(f("foo").forall(_ == "foo"))
//   }

// }
