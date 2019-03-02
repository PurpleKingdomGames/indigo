package indigo.shared

import org.scalatest.FunSpec

class EqSpec extends FunSpec {

  import Eq._

  describe("Equality checking") {

    it("should be able to compare strings") {
      assert("hello" === "hello")
      assert("hello" !== "h")
    }

    it("should be able to compare ints") {
      assert(10 === 10)
      assert(1 !== 2)
    }

    it("should be able to compare float") {
      assert(10f === 10f)
      assert(1f !== 2f)
    }

    it("should be able to compare double") {
      assert(10d === 10d)
      assert(1d !== 2d)
    }

    it("should be able to compare boolean") {
      assert(false === false)
      assert(false !== true)
    }

    it("should be able to compare tuple 2s") {
      assert(("a", 1) === ("a", 1))
      assert(("a", 1) !== ("b", 2))
    }

    it("should be able to compare lists") {
      assert(List(1, 2, 3) === List(1, 2, 3))
      assert(List(1, 2, 3) !== List(2, 3))
    }

    it("should be able to compare options") {
      assert(Option(10) === Option(10))
      assert(Option(10) !== None)
    }

    it("should be able to compare eithers") {
      assert(Right(1) === Right(1))
      assert(Left("a") === Left("a"))
      assert(Right(1) !== Left("a"))
      assert(Right(1) !== Left("a"))
    }

  }

}
