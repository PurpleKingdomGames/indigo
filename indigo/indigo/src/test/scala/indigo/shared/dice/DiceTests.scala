package indigo.shared.dice

import indigo.shared.collections.NonEmptyList

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DiceTests extends munit.FunSuite {

  given CanEqual[List[Int], List[Int]] = CanEqual.derived

  def checkDice(roll: Int, to: Int): Boolean =
    roll >= 1 && roll <= to

  test("diceSidesN") {
    val roll: Int = Dice.diceSidesN(1, 0).roll(10)

    assertEquals(checkDice(roll, 10), true)
  }

  test("should have a roll multiple dice function") {
    Dice.rollMany(2, 6, 0) match {
      case Some(NonEmptyList(d1, d2 :: Nil)) =>
        assertEquals(checkDice(d1, 6), true)

        assertEquals(checkDice(d2, 6), true)

      case _ =>
        throw new java.lang.AssertionError("Match fail!")
    }
  }

  test("should not roll with invalid values") {
    assertEquals(Dice.rollMany(0, 6, 0), None)
    assertEquals(Dice.rollMany(4, 0, 0), None)
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

  test("shuffle") {
    val dice   = Dice.fromSeed(0)
    val actual = dice.shuffle(List(1, 2, 3, 4, 5))

    // Psuedorandom! Seed of 0 produces List(5, 3, 2, 4, 1)
    val expected =
      List(5, 3, 2, 4, 1)

    assertEquals(actual.length, 5)
    assertEquals(actual, expected)
  }

}
