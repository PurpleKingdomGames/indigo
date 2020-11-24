package indigo.shared.dice

import utest._
import indigo.shared.collections.NonEmptyList
import indigo.shared.TestFail

object DiceTests extends TestSuite {

  def checkDice(roll: Int, to: Int): Boolean =
    roll >= 1 && roll <= to

  val tests: Tests =
    Tests {
      "dice functions" - {

        "diceSidesN" - {
          val roll: Int = Dice.diceSidesN(1, 0).roll(10)

          checkDice(roll, 10) ==> true
        }

        "should have a roll multiple dice function" - {

          Dice.roll(2, 6, 0) match {
            case Some(NonEmptyList(d1, d2 :: Nil)) =>
              checkDice(d1, 6) ==> true

              checkDice(d2, 6) ==> true

            case _ =>
              TestFail.fail("Match fail!")
          }

        }

        "should not roll with invalid values" - {
          Dice.roll(0, 6, 0) ==> None
          Dice.roll(4, 0, 0) ==> None
        }

        "should allow arbitrary rolls" - {
          val dice = Dice.arbitrary(1, 3, 0)

          checkDice(dice.roll, 3) ==> true
          checkDice(dice.roll(3), 3) ==> true
        }

        "should give a different number for each roll of the same dice instance" - {
          val dice         = Dice.arbitrary(0, 512, 0)
          val values       = List.fill(50)(dice.roll)
          val numberOfKeys = values.groupBy(identity).keySet.size

          // 48?! I tried it, got the uniqueness number, and it was 48.
          // Psuedorandom! It's the whole point!
          numberOfKeys ==> 48
        }

        "should be able to produce an alphanumeric string" - {
          val dice   = Dice.fromSeed(0)
          val actual = dice.rollAlphaNumeric

          // Psuedorandom! Seed of 0 produces "CCzLNHBFHuRvbI1i"
          val expected =
            "CCzLNHBFHuRvbI1i"

          actual.length() ==> 16
          actual ==> expected
        }

      }

    }
}
