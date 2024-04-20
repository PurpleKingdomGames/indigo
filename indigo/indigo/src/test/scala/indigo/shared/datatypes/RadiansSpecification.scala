package indigo.shared.datatypes

import indigo.shared.dice.Dice
import org.scalacheck._

class RadiansSpecification extends Properties("Dice") {

  property("all random values are within the range of TAU / 2*PI") = Prop.forAll(Gen.long) { seed =>
    val dice  = Dice.fromSeed(seed)
    val value = Radians.random(dice)

    value.toDouble >= 0.0 && value.toDouble <= Radians.TAU.toDouble
  }

}
