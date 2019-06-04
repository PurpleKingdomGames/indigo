package indigoexamples.model

import org.scalacheck._

import indigo._

object Generators {

  val diceGen: Gen[Dice] =
    for {
      sides <- Gen.choose(1, Int.MaxValue)
      seed  <- Gen.choose(0, Long.MaxValue)
    } yield Dice.diceSidesN(sides, seed)

  val pointGen: Gen[Point] =
    pointGenWithBounds(0, Int.MaxValue, 0, Int.MaxValue)

  def pointGenWithBounds(minX: Int, maxX: Int, minY: Int, maxY: Int): Gen[Point] =
    for {
      x <- Gen.choose(minX, maxX)
      y <- Gen.choose(minY, maxY)
    } yield Point(x, y)

  def pointsOnALineGen: Gen[PointsOnLine] =
    for {
      x1 <- Gen.choose(0, Int.MaxValue)
      x2 <- Gen.choose(x1, Int.MaxValue)
      y  <- Gen.choose(0, Int.MaxValue)
    } yield PointsOnLine(Point(x1, y), Point(x2, y))

  final case class PointsOnLine(start: Point, end: Point)

  val millisGen: Gen[Millis] =
    Gen.choose(Long.MinValue, Long.MaxValue).map(Millis.apply)
}
