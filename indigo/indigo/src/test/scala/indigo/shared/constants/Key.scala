package indigo.shared.constants

class KeyTests extends munit.FunSuite {
  private val upperJ = Key(74, "J")
  private val lowerJ = Key(74, "j")

  test("Key.equals is case-insensitive") {
    assertEquals(upperJ, lowerJ)
  }

  test("Key.hashCode is case-insensitive") {
    assertEquals(upperJ.hashCode, lowerJ.hashCode)
  }
}
