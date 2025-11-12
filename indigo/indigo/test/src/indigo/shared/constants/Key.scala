package indigo.shared.constants

class KeyTests extends munit.FunSuite {
  private val upperJ = Key(KeyCode.KeyJ, "J", KeyLocation.Standard)
  private val lowerJ = Key(KeyCode.KeyJ, "j", KeyLocation.Standard)
  private val digits = KeyCode.values.filter(_.toString.startsWith("Digit")).map(Key(_))

  test("Key.equals is case-insensitive") {
    assertEquals(upperJ, lowerJ)
  }

  test("Key.hashCode is case-insensitive") {
    assertEquals(upperJ.hashCode, lowerJ.hashCode)
  }

  test("Key.isNumeric is true for digits") {
    digits.foreach { key =>
      assert(key.isNumeric)
    }
  }

  test("Key.asNumeric returns the correct value for digits") {
    digits.foreach { key =>
      assertEquals(key.asNumeric, Some(key.code.toString.last - '0'))
    }
  }

  test("Keys with different locations are not equal") {
    val leftJ = Key(KeyCode.KeyJ, "J", KeyLocation.Left)
    assert(upperJ != leftJ)
  }

  test("When one key is Invariant they are equal if the codes match") {
    val invariantJ = Key(KeyCode.KeyJ, "J", KeyLocation.Invariant)
    assert(upperJ == invariantJ)
  }
}
