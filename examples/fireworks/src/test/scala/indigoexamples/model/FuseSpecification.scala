package indigoexamples.model

import org.scalacheck._

import indigo.Dice
import ingidoexamples.model.Fuse
import indigo.shared.datatypes.Point
import indigo.EqualTo._

class FuseSpecification extends Properties("Fuse") {

  import Generators._

  def fuseGen: Gen[Fuse] =
    for {
      dice   <- diceGen
      points <- pointsOnALineGen
    } yield Fuse.generateFuse(dice, points.start, points.end)

  def pointsOnALineGen: Gen[PointsOnLine] =
    for {
      x1 <- Gen.choose(0, Int.MaxValue)
      x2 <- Gen.choose(x1, Int.MaxValue)
      y  <- Gen.choose(0, Int.MaxValue)
    } yield PointsOnLine(Point(x1, y), Point(x2, y))

  final case class PointsOnLine(start: Point, end: Point)

  property("generate a fuse with a timer up to 1.5 seconds") = Prop.forAll(fuseGen) { fuse =>
    fuse.length.value >= 1 && fuse.length.value <= 1500
  }

  property("generate a fuse point along the base line") = Prop.forAll(diceGen, pointsOnALineGen) { (dice, points) =>
    val fuse: Fuse =
      Fuse.generateFuse(dice, points.start, points.end)

    fuse.position.y === points.end.y && fuse.position.x >= points.start.x && fuse.position.x <= points.end.x
  }

}
