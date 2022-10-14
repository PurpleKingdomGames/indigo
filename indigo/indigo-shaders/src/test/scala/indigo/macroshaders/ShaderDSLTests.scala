package indigo.macroshaders

import ShaderDSL.*

class ShaderDSLTests extends munit.FunSuite {

  import ShaderAST.*

  test("Fragment: apply & run") {
    val f = Fragment[String, Int](_.length)
    assert(f.run("count me") == 8)
  }

  test("Fragment: pure & run") {
    val f = Fragment.pure[String, Int](10)
    assert(f.run("") == 10)
  }

  test("Fragment: map") {
    val f = Fragment.pure[String, Int](10).map(_ * 10)
    assert(f.run("") == 100)
  }

  test("Fragment: ap") {
    val f = Fragment.pure[String, Int](10).ap(Fragment.pure[String, Int => String](_.toString))
    assert(f.run("") == "10")
  }

  test("Fragment: flatten") {
    val f = Fragment.pure[String, Fragment[String, Int]](
      Fragment.pure[String, Int](10)
    )
    assert(Fragment.join(f).run("") == 10)
    assert(f.flatten.run("") == 10)
  }

  test("Fragment: flatMap") {
    val f = Fragment.pure[String, Int](10).flatMap(i => Fragment.pure[String, Int](i * 10))
    assert(f.run("") == 100)
  }

  test("Fragment: ask") {
    val f = Fragment.pure[String, Int](10)
    assert(f.ask.run("hello") == "hello")
  }

  test("Fragment: asks") {
    val f = Fragment.pure[String, Int](10)
    assert(f.asks((str: String) => str.length()).run("hello") == 5)
  }

}
