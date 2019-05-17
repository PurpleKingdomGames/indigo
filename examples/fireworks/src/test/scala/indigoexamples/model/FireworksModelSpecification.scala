package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import indigo.shared.datatypes.Point
import indigo.EqualTo._

class FireworksModelSpecification extends Properties("FireworksModel") {

  /*
  calculate the middle control point for a given start and end

  create a bezier between the start, end and a
   */

  //------------------------------
  // Fuse
  //------------------------------
  // val diceGen: Gen[Dice] =
  //   for {
  //     sides <- Gen.choose(1, Int.MaxValue)
  //     seed  <- Gen.choose(0, Long.MaxValue)
  //   } yield Dice.diceSidesN(sides, seed)

  // def pointsGen(maxX: Int, maxY: Int): Gen[PointsOnLine] =
  //   for {
  //     x1 <- Gen.choose(0, Int.MaxValue)
  //     x2 <- Gen.choose(x1, Int.MaxValue)
  //     y  <- Gen.choose(0, Int.MaxValue)
  //   } yield PointsOnLine(Point(x1, y), Point(x2, y))

  // final case class PointsOnLine(start: Point, end: Point)

  // property("generate a fuse point along the base line") = Prop.forAll(diceGen, pointsGen(1920, 1080)) { (dice, points) =>
  //   val position: Fuse =
  //     FireworksModel.generateFuse(dice, points.start, points.end)

  //   position.y === points.end.y && position.x >= points.start.x && position.x <= points.end.x
  // }

}
