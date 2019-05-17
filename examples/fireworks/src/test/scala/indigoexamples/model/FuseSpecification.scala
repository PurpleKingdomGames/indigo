package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import ingidoexamples.model.Fuse
import indigo.shared.datatypes.Point
import indigo.EqualTo._

class FuseSpecification extends Properties("Fuse") {

  val diceGen: Gen[Dice] =
    for {
      sides <- Gen.choose(1, Int.MaxValue)
      seed  <- Gen.choose(0, Long.MaxValue)
    } yield Dice.diceSidesN(sides, seed)

  def pointsGen(maxX: Int, maxY: Int): Gen[PointsOnLine] =
    for {
      x1 <- Gen.choose(0, Int.MaxValue)
      x2 <- Gen.choose(x1, Int.MaxValue)
      y  <- Gen.choose(0, Int.MaxValue)
    } yield PointsOnLine(Point(x1, y), Point(x2, y))

  final case class PointsOnLine(start: Point, end: Point)

  property("generate a fuse point along the base line") = Prop.forAll(diceGen, pointsGen(1920, 1080)) { (dice, points) =>
    val fuse: Fuse =
      Fuse.generateFuse(dice, points.start, points.end)

    fuse.length.value >= 1 && fuse.length.value <= 1000

    fuse.position.y === points.end.y && fuse.position.x >= points.start.x && fuse.position.x <= points.end.x
  }

}
