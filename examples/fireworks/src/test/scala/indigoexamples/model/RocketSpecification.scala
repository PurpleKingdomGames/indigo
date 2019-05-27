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

class RocketSpecification extends Properties("Rocket") {

  import Generators._

  property("always creates three control points") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end).length === 3
  }

  property("control points are always in order [start, mid, end]") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end) match {
      case NonEmptyList(s, m :: e :: Nil) =>
        s === start && e === end

      case _ =>
        false
    }
  }

  property("arc mid control point y is always in line with the end y position") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end) match {
      case NonEmptyList(s, m :: e :: Nil) =>
        m.y === e.y

      case _ =>
        false
    }
  }

  property("arc mid control point x is always between the start and end x positions") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end) match {
      case NonEmptyList(s, m :: e :: Nil) =>
        val min = if (s.x <= e.x) s.x else e.x
        val max = if (e.x >= s.x) e.x else s.x

        m.x >= min && m.x <= max

      case _ =>
        false
    }
  }

  property("arc signal should always produce a value inside the beziers bounds") = Prop.forAll(diceGen, pointGen, pointGen, millisGen) { (dice, start, end, time) =>
    val bounds: Rectangle =
      Rocket.createRocketArcBezier(dice, start, end).bounds

    val signal: Signal[Point] =
      Rocket.createRocketArcSignal(dice, start, end, Millis(1000))

    val point: Point =
      signal.at(time)

    "-- Bounds: " + bounds.asString +
      "\n-- Bounds (left): " + bounds.left.toString +
      "\n-- Bounds (right): " + bounds.right.toString +
      "\n-- Bounds (top): " + bounds.top.toString +
      "\n-- Bounds (bottom): " + bounds.bottom.toString +
      "\n-- start: " + start.asString +
      "\n-- end: " + end.asString +
      "\n-- Point: " + point.asString +
      "\n-- Time: " + time.asString +
      "" |: true =? (bounds + Rectangle(0, 0, 1, 1)).isPointWithin(signal.at(time))
  }

  /*
  There are some things to know here.
  Screen coords are TL to BR.
  The start points are assumed to be at the bottom of the screen somewhere, which is several hundred pixels,
  thus end points must be higher (rockets fly up!) which is a y value < start.y and > 0 + some margin.
  The x should be proportial to the height i.e. at most a 45 degree drift.
   */
  // for a window of 3000 x 2000
  property("able to generate a good endpoint based on a start point") = Prop.forAll(diceGen, pointGenWithBounds(1000, 2000, 1000, 2000)) { (dice, start) =>
    val end: Point =
      Rocket.pickEndPoint(dice, start, Rectangle(Point.zero, Point(3000, 2000)))

    "start: " + start + ", end: " + end |: Prop.all(
      s"y:  ${end.y} < ${start.y}" |: end.y < start.y,
      s"y: ${end.y} >= 0" |: end.y >= 0,
      s"x: ${end.x} >= ${start.x - (start.y / 2)}" |: end.x >= start.x - (start.y / 2),
      s"x: ${end.x} <= ${start.x + (start.y / 2)}" |: end.x <= start.x + (start.y / 2)
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
