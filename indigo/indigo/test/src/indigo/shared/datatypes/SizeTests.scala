package indigo.shared.datatypes

class SizeTests extends munit.FunSuite {

  test("mod") {
    assertEquals(Size.mod(Size(11, 12), Size(10, 10)), Size(1, 2))
    assertEquals(Size(11, 12) % Size(10, 10), Size(1, 2))
    assertEquals(Size.mod(Size(9, 10), Size(10, 10)), Size(9, 0))
    assertEquals(Size.mod(Size(1, 1), Size(10, 10)), Size(1, 1))
    assertEquals(Size.mod(Size(-11, -12), Size(10, 10)), Size(9, 8))
    assertEquals(Size.mod(Size(-1, -1), Size(10, 10)), Size(9, 9))
    assertEquals(Size.mod(Size(0, 0), Size(10, 10)), Size(0, 0))
    assertEquals(clue(Size.mod(Size(-11), Size(-10))), clue(Size(-1)))
  }

  test("min") {
    assertEquals(Size(11, 12).min(Size(10, 10)), Size(10, 10))
    assertEquals(Size(11, 12).min(10), Size(10, 10))
    assertEquals(Size(11, 12).min(Size(12, 10)), Size(11, 10))
    assertEquals(Size(11, 12).min(5), Size(5, 5))
  }

  test("max") {
    assertEquals(Size(11, 12).max(Size(10, 10)), Size(11, 12))
    assertEquals(Size(11, 12).max(10), Size(11, 12))
    assertEquals(Size(11, 12).max(Size(12, 10)), Size(12, 12))
    assertEquals(Size(11, 12).max(12), Size(12, 12))
  }

}
