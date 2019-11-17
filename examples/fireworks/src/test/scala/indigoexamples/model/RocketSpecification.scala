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
import indigo.shared.dice.Dice
import indigoexts.geometry.Bezier

class RocketSpecification extends Properties("Rocket") {

  import Generators._

  property("always creates three control points") = Prop.forAll { (dice: Dice, target: Vertex) =>
    Rocket.createArcControlVertices(dice)(target).length === 3
  }

  property("control points are always in order [start, mid, target]") = Prop.forAll { (dice: Dice, target: Vertex) =>
    Rocket.createArcControlVertices(dice)(target) match {
      case NonEmptyList(s, _ :: e :: Nil) =>
        s === Vertex.zero && e === target

      case _ =>
        false
    }
  }

  property("arc mid control point y is always in line with the target y position") = Prop.forAll { (dice: Dice, target: Vertex) =>
    Rocket.createArcControlVertices(dice)(target) match {
      case NonEmptyList(s, m :: e :: Nil) =>
        m.y === e.y

      case _ =>
        false
    }
  }

  property("arc mid control point x is always more than half way between 0 and 1") = Prop.forAll { (dice: Dice, target: Vertex) =>
    val vertices =
      Rocket.createArcControlVertices(dice)(target)

    "Vertices X's: " + vertices.toList.map(_.x).mkString("[", ", ", "]") |: Prop.all(
      vertices match {
        case NonEmptyList(s, m :: e :: Nil) =>
          Math.abs(m.x) >= (Math.abs(e.x) - Math.abs(s.x)) / 2

        case _ =>
          false
      }
    )
  }

  property("arc signal should always produce a value inside the beziers bounds") = Prop.forAll { (dice: Dice, target: Vertex, time: Millis) =>
    val vertices: NonEmptyList[Vertex] =
      Rocket.createArcControlVertices(dice)(target)

    val bounds: BoundingBox =
      Bezier.fromVerticesNel(vertices).bounds

    val signal: Signal[Vertex] =
      Rocket.createArcSignal(Millis(1000))(vertices)

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

  // Previous test guarantees we're always inside the bounds, so we can limit the test cases a bit here.
  property("Over time, the signal should always generate values moving closer to the target") = Prop.forAll { (dice: Dice, target: Vertex) =>
    Prop.forAll(nowNextMillis(0, 1000)) {
      case (now, next) =>
        val vertices: NonEmptyList[Vertex] =
          Rocket.createArcControlVertices(dice)(target)

        val signal: Signal[Vertex] =
          Rocket.createArcSignal(Millis(1000))(vertices)

        val v1: Vertex =
          signal.at(now)

        val v2: Vertex =
          signal.at(next)

        "t1: " + now +
          "\nt2: " + next +
          "\nv1: " + v1 +
          "\nv2: " + v2 +
          "\ntarget: " + target +
          "\nv1 distance: " + v1.distanceTo(target) +
          "\nv2 distance: " + v2.distanceTo(target) +
          "" |: v2.distanceTo(target) < v1.distanceTo(target)
    }
  }

  /*
  There are some things to know here.
  Screen coords are TL to BR.
  The start points are assumed to be at the bottom of the screen somewhere, which is several hundred pixels,
  thus target points must be higher (rockets fly up!) which is a y value < start.y and > 0 + some margin.
  The x should be proportial to the height i.e. at most a 45 degree drift.
   */
  // for a window of 3000 x 2000
  property("able to generate a good target vertex based on a start point") = Prop.forAll { dice: Dice =>
    val target: Vertex =
      Rocket.pickEndPoint(dice)

    "target: " + target |: Prop.all(
      s"y:  ${target.y} < 1.0" |: target.y < 1,
      s"y: ${target.y} >= 0.5" |: target.y >= 0.5,
      s"x: ${target.x} >= -0.5" |: target.x >= -0.5d,
      s"x: ${target.x} <= 0.5" |: target.x <= 0.5d
    )
  }

  property("able to generate a flight time") = Prop.forAll { dice: Dice =>
    val flightTime: Millis =
      Rocket.pickFlightTime(dice)

    Prop.all(
      flightTime >= Millis(1000),
      flightTime <= Millis(2000)
    )
  }

}
