package indigoexamples.model

import org.scalacheck._

import indigo.shared.datatypes.Point
import ingidoexamples.model.Rocket
import indigo.EqualTo._

class RocketSpecification extends Properties("Rocket") {

  import Generators._

  property("always creates three control points") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end).length === 3
  }

  property("control points are always in order [start, mid, end]") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end) match {
      case s :: _ :: e :: Nil =>
        s === start && e === end

      case _ =>
        false
    }
  }

  property("arc mid control point y is always in line with the end y position") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end) match {
      case s :: m :: e :: Nil =>
        m.y === e.y

      case _ =>
        false
    }
  }

  property("arc mid control point x is always between the start and end x positions") = Prop.forAll(diceGen, pointGen, pointGen) { (dice, start, end) =>
    Rocket.createArcControlPoints(dice, start, end) match {
      case s :: m :: e :: Nil =>
        val min = if (s.x <= e.x) s.x else e.x
        val max = if (e.x >= s.x) e.x else s.x

        m.x >= min && m.x <= max

      case _ =>
        false
    }
  }

}
