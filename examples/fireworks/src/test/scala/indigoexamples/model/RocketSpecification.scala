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

}
