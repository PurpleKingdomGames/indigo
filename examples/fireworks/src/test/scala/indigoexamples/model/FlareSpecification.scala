package indigoexamples.model

import org.scalacheck._
import org.scalacheck.Prop._
import indigo.shared.dice.Dice
import indigo.shared.time.Millis
import ingidoexamples.model.Flare
import indigoexts.geometry.Vertex
import indigo.shared.datatypes.Radians
import indigo.shared.EqualTo._
import indigo.shared.datatypes.Point

class FlareSpecification extends Properties("Flare") {

  import Generators._

  property("always creates two control points") = Prop.forAll { target: Vertex =>
    Flare.createArcControlVertices(Vertex.zero)(target).length === 2
  }

  property("Supplied vertex is always first") = Prop.forAll { target: Vertex =>
    Flare.createArcControlVertices(Vertex.zero)(target).head === Vertex.zero
  }

  property("able to generate a good target vertex based on a start point") = Prop.forAll(radiansGen, diceGen, clampedRadiusGen) { (angle: Radians, dice: Dice, radius: Radius) =>

    val maxX: Double = 1.21d
    val maxY: Double = 0.61d

    val target: Vertex =
      Flare.pickEndPoint(angle)(dice)

    val distance: Double =
      Vertex.distanceBetween(Vertex.zero, target)

    val maxDistance: Double =
      Vertex.zero.distanceTo(Vertex(maxX, maxY))

    "target: " + target |: Prop.all(
      s"y: ${target.y} <=  $maxY" |: target.y <= maxY,
      s"y: ${target.y} >= -$maxY" |: target.y >= -maxY,
      s"x: ${target.x} >= -$maxX" |: target.x >= -maxX,
      s"x: ${target.x} <=  $maxX" |: target.x <= maxX,
      s"distance: $distance <= maxDistance: $maxDistance" |: distance <= maxDistance
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
