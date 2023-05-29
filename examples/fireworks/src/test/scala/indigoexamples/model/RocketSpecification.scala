package indigoexamples.model

import indigo.Bezier
import indigo.BoundingBox
import indigo.Vertex
import indigo.shared.collections.NonEmptyBatch
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.temporal.Signal
import indigo.shared.time.Millis
import org.scalacheck.Prop._
import org.scalacheck._

class RocketSpecification extends Properties("Rocket") {

  import Generators._

  property("always creates three control points") = Prop.forAll { (dice: Dice) =>
    Rocket.createArcControlVertices(dice, Vertex.zero)(Vertex.zero).length == 3
  }

  property("control points are always in order [start, mid, target]") = Prop.forAll(diceGen, launchPadVertexGen) { (dice: Dice, launch: Vertex) =>
    Prop.forAll(rocketTargetVertexGen(launch)) { target =>
      Rocket.createArcControlVertices(dice, launch)(target) match {
        case NonEmptyBatch(s, t) =>
          s == launch && t(1) == target
      }
    }
  }

  property("arc mid control point y is always in line with the target y position") = Prop.forAll(diceGen, launchPadVertexGen) { (dice: Dice, launch: Vertex) =>
    Prop.forAll(rocketTargetVertexGen(launch)) { target =>
      Rocket.createArcControlVertices(dice, launch)(target) match {
        case NonEmptyBatch(s, t) =>
          t(0).y == t(1).y
      }
    }
  }

  property("arc mid control point x is always more than half way towards the target") = Prop.forAll(diceGen, launchPadVertexGen) { (dice: Dice, launch: Vertex) =>
    Prop.forAll(rocketTargetVertexGen(launch)) { target =>
      val vertices =
        Rocket.createArcControlVertices(dice, launch)(target)

      "Vertices X's: " + vertices.toList.map(_.x).mkString("[", ", ", "]") |: Prop.all(
        vertices match {
          case NonEmptyBatch(s, t) =>
            Math.abs(t(0).x) >= (Math.abs(t(1).x) - Math.abs(s.x)) / 2
        }
      )
    }
  }

  property("able to generate a good target vertex based on a start point") = Prop.forAll(diceGen, launchPadVertexGen) { (dice: Dice, launch: Vertex) =>
    val target: Vertex =
      Rocket.pickEndPoint(dice, launch)

    "target: " + target |: Prop.all(
      s"y:  ${target.y} <= 1.0" |: target.y <= 1.0d,
      s"y: ${target.y} >= 0.5" |: target.y >= 0.5d,
      s"x: ${target.x - launch.x} >= -0.5" |: target.x - launch.x >= -0.5d,
      s"x: ${target.x - launch.x} <= 0.5" |: target.x - launch.x <= 0.5d
    )
  }

  // Flare generation

  property("creates between 5 and 8 flares that all share the rockets end point and blast radius") = Prop.forAll { (dice: Dice, start: Vertex) =>
    val flares = Rocket.generateFlares(dice, start, RGBA.Cyan)

    Prop.all(
      flares.length >= 5,
      flares.length <= 8
    )
  }

  property("pickColor always generates a valid RGBA") = Prop.forAll { (dice: Dice) =>
    Rocket.pickColour(dice).toString().toLowerCase().contains("rgba") ==> true
  }

}
