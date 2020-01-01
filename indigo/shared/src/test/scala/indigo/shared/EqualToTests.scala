package indigo.shared

import utest._

object EqualToTests extends TestSuite {

  import EqualTo._

  val tests: Tests =
    Tests {
      "should be able to compare strings" - {
        assert("hello" === "hello")
        assert("hello" !== "h")
      }

      "should be able to compare ints" - {
        assert(10 === 10)
        assert(1 !== 2)
      }

      "should be able to compare float" - {
        assert(10f === 10f)
        assert(1f !== 2f)
      }

      "should be able to compare double" - {
        assert(10d === 10d)
        assert(1d !== 2d)
      }

      "should be able to compare boolean" - {
        assert(false === false)
        assert(false !== true)
      }

      "should be able to compare tuple 2s" - {
        assert(("a", 1) === ("a", 1))
        assert(("a", 1) !== ("b", 2))
      }

      "should be able to compare lists" - {
        assert(List(1, 2, 3) === List(1, 2, 3))
        assert(List(1, 2, 3) !== List(2, 3))
      }

      "should be able to compare options" - {
        assert(Option(10) === Some(10))
        assert(Option(10) !== None)
      }

      "should be able to compare eithers" - {
        assert(Right(1) === Right(1))
        assert(Left("a") === Left("a"))
      }
    }

}
