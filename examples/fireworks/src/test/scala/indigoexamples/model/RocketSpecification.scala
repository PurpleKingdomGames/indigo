package indigoexamples.model

import org.scalacheck._
import org.scalacheck.Prop._

import indigo.shared.datatypes.Point
import ingidoexamples.model.Rocket
import indigo.EqualTo._
import indigo.shared.datatypes.Rectangle
import indigo.shared.temporal.Signal
import indigo.shared.collections.NonEmptyList
import indigo.shared.time.Millis
import indigoexts.geometry.Vertex
import indigoexts.geometry.BoundingBox

class RocketSpecification extends Properties("Rocket") {

  import Generators._

  property("always creates three control points") = Prop.forAll(diceGen, vertexGen) { (dice, target) =>
    Rocket.createArcControlVertices(dice, target).length === 3
  }

  property("control points are always in order [start, mid, target]") = Prop.forAll(diceGen, vertexGen) { (dice, target) =>
    Rocket.createArcControlVertices(dice, target) match {
      case NonEmptyList(s, _ :: e :: Nil) =>
        s === Vertex.zero && e === target

      case _ =>
        false
    }
  }

  property("arc mid control point y is always in line with the target y position") = Prop.forAll(diceGen, vertexGen) { (dice, target) =>
    Rocket.createArcControlVertices(dice, target) match {
      case NonEmptyList(s, m :: e :: Nil) =>
        m.y === e.y

      case _ =>
        false
    }
  }

  property("arc mid control point x is always between 0 and target vertex x") = Prop.forAll(diceGen, vertexGen) { (dice, target) =>
    val vertices =
      Rocket.createArcControlVertices(dice, target)

    "Vertices X's: " + vertices.toList.map(_.x).mkString("[", ", ", "]") |: Prop.all(
      vertices match {
        case NonEmptyList(s, m :: e :: Nil) =>
          val min = if (e.x < 0) e.x else 0
          val max = if (e.x > 0) e.x else 0

          m.x >= min && m.x <= max

        case _ =>
          false
      }
    )
  }

  property("arc signal should always produce a value inside the beziers bounds") = Prop.forAll(diceGen, vertexGen, millisGen) { (dice, target, time) =>
    val bounds: BoundingBox =
      Rocket.createRocketArcBezier(dice, target).bounds

    val signal: Signal[Vertex] =
      Rocket.createRocketArcSignal(dice, target, Millis(1000))

    val point: Vertex =
      signal.at(time)

    "-- Bounds: " + bounds.asString +
      "\n-- Bounds (left): " + bounds.left.toString +
      "\n-- Bounds (right): " + bounds.right.toString +
      "\n-- Bounds (top): " + bounds.top.toString +
      "\n-- Bounds (bottom): " + bounds.bottom.toString +
      "\n-- target: " + target.asString +
      "\n-- Point: " + point.asString +
      "\n-- Time: " + time.asString +
      "" |: true =? (bounds + BoundingBox(0, 0, 1, 1)).isVertexWithin(signal.at(time))
  }

  /*
  There are some things to know here.
  Screen coords are TL to BR.
  The start points are assumed to be at the bottom of the screen somewhere, which is several hundred pixels,
  thus target points must be higher (rockets fly up!) which is a y value < start.y and > 0 + some margin.
  The x should be proportial to the height i.e. at most a 45 degree drift.
   */
  // for a window of 3000 x 2000
  property("able to generate a good target vertex based on a start point") = Prop.forAll(diceGen) { (dice) =>
    val target: Vertex =
      Rocket.pickEndPoint(dice)

    "target: " + target |: Prop.all(
      s"y:  ${target.y} < 1.0" |: target.y < 1,
      s"y: ${target.y} >= 0.5" |: target.y >= 0.5,
      s"x: ${target.x} >= -1.0" |: target.x >= -1.0d,
      s"x: ${target.x} <= 1.0" |: target.x <= 1.0d
    )
  }

  property("able to generate a flight time") = Prop.forAll(diceGen) { dice =>
    val flightTime: Millis =
      Rocket.pickFlightTime(dice)

    Prop.all(
      flightTime >= Millis(1000),
      flightTime <= Millis(3000)
    )
  }

}
