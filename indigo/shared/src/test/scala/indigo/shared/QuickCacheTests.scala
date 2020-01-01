package indigo.shared

import utest._

object QuickCacheTests extends TestSuite {

  val tests: Tests =
    Tests {

      "cache behaviour checks" - {
        implicit val cache = QuickCache.empty[Int]

        QuickCache("ten")(10) ==> 10

        cache.keys.map(_.value) ==> List("ten")

        cache.all.map(p => (p._1.value, p._2)) ==> List(("ten" -> 10))

        cache.fetch(CacheKey("ten")) ==> Some(10)

        QuickCache("ten")(20) ==> 10

        cache.purge(CacheKey("ten"))

        cache.fetch(CacheKey("ten")) ==> None

        QuickCache("ten")(20) ==> 20

        cache.fetch(CacheKey("ten")) ==> Some(20)
      }

      "values are lazily evaluated" - {

        var message: String = "nada"

        implicit val cache = QuickCache.empty[Int]

        QuickCache("ten") {
          message = "a"
          10
        } ==> 10

        message ==> "a"

        QuickCache("ten") {
          message = "b"
          20
        } ==> 10

        message ==> "a"

      }

    }

}
