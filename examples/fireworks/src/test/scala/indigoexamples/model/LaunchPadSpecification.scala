package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import ingidoexamples.model.LaunchPad
import indigo.shared.datatypes.Point
import indigo.EqualTo._

class LaunchPadSpecification extends Properties("LaunchPad") {

  import Generators._

  def launchPadGen: Gen[LaunchPad] =
    for {
      dice   <- diceGen
      points <- pointsOnALineGen
    } yield LaunchPad.generateLaunchPad(dice, points.start, points.end)

  def pointsOnALineGen: Gen[PointsOnLine] =
    for {
      x1 <- Gen.choose(0, Int.MaxValue)
      x2 <- Gen.choose(x1, Int.MaxValue)
      y  <- Gen.choose(0, Int.MaxValue)
    } yield PointsOnLine(Point(x1, y), Point(x2, y))

  final case class PointsOnLine(start: Point, end: Point)

  property("generate a launch pad with a timer up to 1.5 seconds") = Prop.forAll(launchPadGen) { launchPad =>
    launchPad.length.value >= 1 && launchPad.length.value <= 1500
  }

  property("generate a launch pad point along the base line") = Prop.forAll(diceGen, pointsOnALineGen) { (dice, points) =>
    val launchPad: LaunchPad =
      LaunchPad.generateLaunchPad(dice, points.start, points.end)

    launchPad.position.y === points.end.y && launchPad.position.x >= points.start.x && launchPad.position.x <= points.end.x
  }

}
