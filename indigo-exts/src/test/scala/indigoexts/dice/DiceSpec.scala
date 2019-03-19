package indigoexts.dice

import org.scalatest.{FunSpec, Matchers}
import indigoexts.collections.NonEmptyList

class DiceSpec extends FunSpec with Matchers {

  def checkDice(roll: Int, to: Int): Boolean =
    roll >= 1 && roll <= to

  describe("dice functions") {

    it("diceSidesN") {
      val roll: Int = Dice.diceSidesN(1).roll(10)

      checkDice(roll, 10) shouldEqual true
    }

    it("should have a roll multiple dice function") {

      Dice.roll(2, 6) match {
        case Some(NonEmptyList(d1, d2 :: Nil)) =>
          withClue("d1: " + d1) {
            checkDice(d1, 6) shouldEqual true
          }

          withClue("d2: " + d2) {
            checkDice(d2, 6) shouldEqual true
          }

        case _ =>
          fail("bad pattern match")
      }

    }

    it("should not roll with invalid values") {
      Dice.roll(0, 6) shouldEqual None
      Dice.roll(4, 0) shouldEqual None
    }

    it("should allow arbitrary rolls") {
      val dice = Dice.arbitrary(1, 3)

      checkDice(dice.roll, 3) shouldEqual true
      checkDice(dice.roll(3), 3) shouldEqual true
    }

  }

}
