package indigo.shared.dice

import indigo.shared.collections.NonEmptyList

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DiceTests extends munit.FunSuite {

  def checkDice(roll: Int, to: Int): Boolean =
    roll >= 1 && roll <= to

  test("diceSidesN") {
    val roll: Int = Dice.diceSidesN(1, 0).roll(10)

    assertEquals(checkDice(roll, 10), true)
  }

  test("should have a roll multiple dice function") {

    Dice.roll(2, 6, 0) match {
      case Some(NonEmptyList(d1, d2 :: Nil)) =>
        assertEquals(checkDice(d1, 6), true)

        assertEquals(checkDice(d2, 6), true)

      case _ =>
        throw new java.lang.AssertionError("Match fail!")
    }

  }

  test("should not roll with invalid values") {
    assertEquals(Dice.roll(0, 6, 0), None)
    assertEquals(Dice.roll(4, 0, 0), None)
  }

  test("should allow arbitrary rolls") {
    val dice = Dice.arbitrary(1, 3, 0)

    assertEquals(checkDice(dice.roll, 3), true)
    assertEquals(checkDice(dice.roll(3), 3), true)
  }

  test("should give a different number for each roll of the same dice instance") {
    val dice         = Dice.arbitrary(0, 512, 0)
    val values       = List.fill(50)(dice.roll)
    val numberOfKeys = values.groupBy(identity).keySet.size

    // 48?! I tried it, got the uniqueness number, and it was 48.
    // Psuedorandom! It's the whole point!
    assertEquals(numberOfKeys, 48)
  }

  test("should be able to produce an alphanumeric string") {
    val dice   = Dice.fromSeed(0)
    val actual = dice.rollAlphaNumeric

    // Psuedorandom! Seed of 0 produces "CCzLNHBFHuRvbI1i"
    val expected =
      "CCzLNHBFHuRvbI1i"

    assertEquals(actual.length(), 16)
    assertEquals(actual, expected)
  }

}
