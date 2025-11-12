package indigo.shared

import scala.annotation.nowarn

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
@nowarn("msg=unused")
class QuickCacheTests extends munit.FunSuite {

  test("cache behaviour checks") {
    implicit val cache = QuickCache.empty[Int]

    assertEquals(QuickCache("ten")(10), 10)

    assertEquals(cache.keys.map(_.toString), List("ten"))

    assertEquals(cache.all.map(p => (p._1.toString, p._2)), List("ten" -> 10))

    assertEquals(cache.fetch(CacheKey("ten")), Some(10))

    assertEquals(QuickCache("ten")(20), 10)

    cache.purge(CacheKey("ten"))

    assertEquals(cache.fetch(CacheKey("ten")), None)

    assertEquals(QuickCache("ten")(20), 20)

    assertEquals(cache.fetch(CacheKey("ten")), Some(20))
  }

  test("values are lazily evaluated") {

    var message: String = "nada"

    implicit val cache = QuickCache.empty[Int]

    assertEquals(
      QuickCache("ten") {
        message = "a"
        10
      },
      10
    )

    assertEquals(message, "a")

    assertEquals(
      QuickCache("ten") {
        message = "b"
        20
      },
      10
    )

    assertEquals(message, "a")

  }

  test("Export values to Map") {
    implicit val cache = QuickCache.empty[Int]

    QuickCache("a")(1)
    QuickCache("b")(2)
    QuickCache("c")(3)

    val expected =
      Map(
        "a" -> 1,
        "b" -> 2,
        "c" -> 3
      )

    val actual: Map[String, Int] =
      cache.toMap(_.toString)

    assertEquals(actual, expected)
  }

  test("quickcache can be disabled") {
    implicit val cache = QuickCache.empty[Int]

    assertEquals(QuickCache("ten")(10), 10)
    assertEquals(QuickCache("ten", true)(20), 20) // disabled)

    assertEquals(cache.keys.map(_.toString), List("ten"))

    assertEquals(cache.all.map(p => (p._1.toString, p._2)), List("ten" -> 10))
  }

}
