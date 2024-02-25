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

}
