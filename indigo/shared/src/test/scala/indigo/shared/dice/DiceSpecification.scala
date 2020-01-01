package indigo.shared.dice

import org.scalacheck._

class DiceSpecification extends Properties("Dice") {

  property("all dice rolls are in range") = Prop.forAll(Gen.choose(1, Int.MaxValue)) { noOfSides =>
    val value = Dice.diceSidesN(noOfSides, 0).roll

    value >= 1 && value <= noOfSides
  }

  property("all arbitrary dice rolls are in range") = Prop.forAll(Gen.choose(10, Int.MaxValue)) { noOfSides =>
    val value = Dice.arbitrary(10, noOfSides, 0).roll

    value >= 10 && value <= noOfSides
  }

}
