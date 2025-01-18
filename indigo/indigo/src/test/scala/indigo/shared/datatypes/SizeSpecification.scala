package indigo.shared.datatypes

import indigo.shared.dice.Dice
import org.scalacheck.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class SizeSpecification extends Properties("Dice") {

  property("all random values are within the max range and >= 0") = Prop.forAll(Gen.choose(0, 500)) { max =>
    val dice  = Dice.default
    val value = Size.random(dice, max)

    value.width >= 0 && value.height >= 0 && value.width <= max && value.height <= max
  }

  property("all random values are within the max range (Size) and >= 0") = Prop.forAll(Gen.choose(0, 500)) { max =>
    val dice  = Dice.default
    val value = Size.random(dice, Size(max))

    value.width >= 0 && value.height >= 0 && value.width <= Size(max).width && value.height <= Size(max).height
  }

  property("all random values are within the min / max range") = Prop.forAll(Gen.choose(-500, 0), Gen.choose(0, 500)) {
    (min, max) =>
      val dice  = Dice.default
      val value = Size.random(dice, min, max)

      value.width >= min && value.height >= min && value.width <= max && value.height <= max
  }

  property("all random values are within the min / max range (Size)") =
    Prop.forAll(Gen.choose(-500, 0), Gen.choose(0, 500)) { (min, max) =>
      val dice  = Dice.default
      val value = Size.random(dice, Size(min), Size(max))

      value.width >= Size(min).width && value.height >= Size(min).height && value.width <= Size(
        max
      ).width && value.height <= Size(max).height
    }

}
