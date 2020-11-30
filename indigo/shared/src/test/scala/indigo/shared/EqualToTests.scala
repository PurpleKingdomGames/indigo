package indigo.shared

class EqualToTests extends munit.FunSuite {

  import EqualTo._

  test("should be able to compare strings") {
    assert("hello" === "hello")
    assert("hello" !== "h")
  }

  test("should be able to compare ints") {
    assert(10 === 10)
    assert(1 !== 2)
  }

  test("should be able to compare float") {
    assert(10f === 10f)
    assert(1f !== 2f)
  }

  test("should be able to compare double") {
    assert(10d === 10d)
    assert(1d !== 2d)
  }

  test("should be able to compare boolean") {
    assert(false === false)
    assert(false !== true)
  }

  test("should be able to compare tuple 2s") {
    assert(("a", 1) === ("a", 1))
    assert(("a", 1) !== ("b", 2))
  }

  test("should be able to compare lists") {
    assert(List(1, 2, 3) === List(1, 2, 3))
    assert(List(1, 2, 3) !== List(2, 3))
  }

  test("should be able to compare options") {
    assert(Option(10) === Some(10))
    assert(Option(10) !== None)
  }

  test("should be able to compare eithers") {
    assert(Right(1) === Right(1))
    assert(Left("a") === Left("a"))
  }

}
