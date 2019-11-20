package indigoexamples.model

import org.scalacheck._
import org.scalacheck.Prop._
import indigo.shared.dice.Dice
import indigo.shared.time.Millis
import ingidoexamples.model.Flare
import indigoexts.geometry.Vertex
import indigo.shared.datatypes.Radians
import indigo.shared.EqualTo._

class FlareSpecification extends Properties("Flare") {

  import Generators._

  property("always creates two control points") = Prop.forAll { target: Vertex =>
    Flare.createArcControlVertices(target).length === 2
  }

  property("Zero vertex is always first") = Prop.forAll { target: Vertex =>
    Flare.createArcControlVertices(target).head === Vertex.zero
  }

  property("able to generate a good target vertex based on a start point") = Prop.forAll { (angle: Radians, dice: Dice) =>
    val target: Vertex =
      Flare.pickEndPoint(angle)(dice)

    "target: " + target |: Prop.all(
      s"y: ${target.y} <=  1.0" |: target.y <= 1.0d,
      s"y: ${target.y} >= -1.0" |: target.y >= -1.0d,
      s"x: ${target.x} >= -1.0" |: target.x >= -1.0d,
      s"x: ${target.x} <=  1.0" |: target.x <= 1.0d
    )
  }

  property("able to 'wobble' a value") = Prop.forAll(diceGen, minMaxDoubles(-10000, 10000)) { (dice: Dice, minMax: (Double, Double)) =>
    val (min, max) = minMax

    val wobbled =
      Flare.wobble(dice, min, max)

    Prop.all(
      wobbled >= min,
      wobbled <= max
    )

  }
}
