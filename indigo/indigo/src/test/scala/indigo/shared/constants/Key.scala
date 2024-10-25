package indigo.shared.constants

class KeyTests extends munit.FunSuite {
  private val upperJ = Key(KeyCode.KeyJ, "J", KeyLocation.Standard)
  private val lowerJ = Key(KeyCode.KeyJ, "j", KeyLocation.Standard)

  test("Key.equals is case-insensitive") {
    assertEquals(upperJ, lowerJ)
  }

  test("Key.hashCode is case-insensitive") {
    assertEquals(upperJ.hashCode, lowerJ.hashCode)
  }
}
