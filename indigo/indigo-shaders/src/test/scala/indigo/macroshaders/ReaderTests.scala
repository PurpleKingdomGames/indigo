package indigo.macroshaders

class ReaderTests extends munit.FunSuite {

  test("apply & run") {
    val f = Reader[String, Int](_.length)
    assert(f.run("count me") == 8)
  }

  test("pure & run") {
    val f = Reader.pure[String, Int](10)
    assert(f.run("") == 10)
  }

  test("map") {
    val f = Reader.pure[String, Int](10).map(_ * 10)
    assert(f.run("") == 100)
  }

  test("ap") {
    val f = Reader.pure[String, Int](10).ap(Reader.pure[String, Int => String](_.toString))
    assert(f.run("") == "10")
  }

  test("flatten") {
    val f = Reader.pure[String, Reader[String, Int]](
      Reader.pure[String, Int](10)
    )
    assert(Reader.join(f).run("") == 10)
    assert(f.flatten.run("") == 10)
  }

  test("flatMap") {
    val f = Reader.pure[String, Int](10).flatMap(i => Reader.pure[String, Int](i * 10))
    assert(f.run("") == 100)
  }

  test("ask") {
    val f = Reader.pure[String, Int](10)
    assert(f.ask.run("hello") == "hello")
  }

  test("asks") {
    val f = Reader.pure[String, Int](10)
    assert(f.asks((str: String) => str.length()).run("hello") == 5)
  }

}
