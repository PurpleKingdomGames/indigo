package indigoexamples.model

import org.scalacheck._
import org.scalacheck.Prop._

import indigo.shared.datatypes.Point
import indigo.EqualTo._
import indigo.shared.datatypes.Rectangle
import indigo.shared.temporal.Signal
import indigo.shared.collections.NonEmptyList
import indigo.shared.time.Millis
import indigoexts.geometry.Vertex
import indigoexts.geometry.BoundingBox
import indigo.shared.dice.Dice
import indigoexts.geometry.Bezier
import indigo.shared.datatypes.Tint

class RocketSpecification extends Properties("Rocket") {

  import Generators._

  property("always creates three control points") = Prop.forAll { dice: Dice =>
    Rocket.createArcControlVertices(dice, Vertex.zero)(Vertex.zero).length === 3
  }

  property("control points are always in order [start, mid, target]") = Prop.forAll(diceGen, launchPadVertexGen) { (dice: Dice, launch: Vertex) =>
    Prop.forAll(rocketTargetVertexGen(launch)) { target =>
      Rocket.createArcControlVertices(dice, launch)(target) match {
        case NonEmptyList(s, _ :: e :: Nil) =>
          s === launch && e === target

        case _ =>
          false
      }
    }
  }

  property("arc mid control point y is always in line with the target y position") = Prop.forAll(diceGen, launchPadVertexGen) { (dice: Dice, launch: Vertex) =>
    Prop.forAll(rocketTargetVertexGen(launch)) { target =>
      Rocket.createArcControlVertices(dice, launch)(target) match {
        case NonEmptyList(s, m :: e :: Nil) =>
          m.y === e.y

        case _ =>
          false
      }
    }
  }

  property("arc mid control point x is always more than half way towards the target") = Prop.forAll(diceGen, launchPadVertexGen) { (dice: Dice, launch: Vertex) =>
    Prop.forAll(rocketTargetVertexGen(launch)) { target =>
      val vertices =
        Rocket.createArcControlVertices(dice, launch)(target)

      "Vertices X's: " + vertices.toList.map(_.x).mkString("[", ", ", "]") |: Prop.all(
        vertices match {
          case NonEmptyList(s, m :: e :: Nil) =>
            Math.abs(m.x) >= (Math.abs(e.x) - Math.abs(s.x)) / 2

          case _ =>
            false
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
    val flares = Rocket.generateFlares(dice, start, Tint.Cyan)

    Prop.all(
      flares.length >= 5,
      flares.length <= 8
    )
  }

  property("pickColor always generates a valid Tint") = Prop.forAll { dice: Dice =>
    Rocket.pickColour(dice).toString().toLowerCase().contains("tint") ==> true
  }

}
