package indigo.shared.datatypes

import indigo.shared.dice.Dice
import org.scalacheck.*

import scala.annotation.nowarn

@nowarn("msg=unused")
class PointSpecification extends Properties("Dice") {

  property("all random values are within the max range and >= 0") = Prop.forAll(Gen.choose(0, 500)) { max =>
    val dice  = Dice.default
    val value = Point.random(dice, max)

    value.x >= 0 && value.y >= 0 && value.x <= max && value.y <= max
  }

  property("all random values are within the max range (Point) and >= 0") = Prop.forAll(Gen.choose(0, 500)) { max =>
    val dice  = Dice.default
    val value = Point.random(dice, Point(max))

    value.x >= 0 && value.y >= 0 && value.x <= Point(max).x && value.y <= Point(max).y
  }

  property("all random values are within the min / max range") = Prop.forAll(Gen.choose(-500, 0), Gen.choose(0, 500)) {
    (min, max) =>
      val dice  = Dice.default
      val value = Point.random(dice, min, max)

      value.x >= min && value.y >= min && value.x <= max && value.y <= max
  }

  property("all random values are within the min / max range (Point)") =
    Prop.forAll(Gen.choose(-500, 0), Gen.choose(0, 500)) { (min, max) =>
      val dice  = Dice.default
      val value = Point.random(dice, Point(min), Point(max))

      value.x >= Point(min).x && value.y >= Point(min).y && value.x <= Point(max).x && value.y <= Point(max).y
    }

}
