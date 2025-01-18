package indigo.shared.dice

import org.scalacheck.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class DiceSpecification extends Properties("Dice") {

  property("all dice rolls are in range (fixed sides)") = Prop.forAll(Gen.choose(1, 500)) { noOfSides =>
    val value = Dice.diceSidesN(noOfSides, 0).roll

    value >= 1 && value <= noOfSides
  }

  property("all dice rolls are in range (given sides)") = Prop.forAll(Gen.choose(1, 500)) { noOfSides =>
    val value = Dice.diceSidesN(noOfSides, 0).roll(noOfSides)

    value >= 1 && value <= noOfSides
  }

  property("all dice rolls from zero are in range (fixed sides)") = Prop.forAll(Gen.choose(1, 20)) { noOfSides =>
    val value = Dice.diceSidesN(noOfSides, 0).rollFromZero

    value >= 0 && value <= noOfSides - 1
  }

  property("all dice rolls from zero are in range (given sides)") = Prop.forAll(Gen.choose(1, 20)) { noOfSides =>
    val value = Dice.diceSidesN(noOfSides, 0).rollFromZero(noOfSides)

    value >= 0 && value <= noOfSides - 1
  }

  property("all dice rollRange throws are in the range given") = Prop.forAll(Gen.choose(1, 20), Gen.choose(21, 40)) {
    (from, to) =>
      val value = Dice.diceSidesN(16, 0).rollRange(from, to)

      from <= value && value <= to
  }

}
