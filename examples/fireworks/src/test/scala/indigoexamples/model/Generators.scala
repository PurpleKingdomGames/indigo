package indigoexamples.model

import org.scalacheck._

import indigo._
import indigoexts.geometry.Vertex

object Generators {

  val diceGen: Gen[Dice] =
    for {
      sides <- Gen.choose(1, Int.MaxValue)
      seed  <- Gen.choose(0, Long.MaxValue)
    } yield Dice.diceSidesN(sides, seed)

  implicit val arbDice: Arbitrary[Dice] =
    Arbitrary(diceGen)

  val vertexGen: Gen[Vertex] =
    for {
      x <- Gen.choose(0d, 1d)
      y <- Gen.choose(0d, 1d)
    } yield Vertex(x, y)

  implicit val arbVertex: Arbitrary[Vertex] =
    Arbitrary(vertexGen)

  val millisGen: Gen[Millis] =
    clampedMillisGen(Long.MinValue, Long.MaxValue)

  implicit val arbMillis: Arbitrary[Millis] =
    Arbitrary(millisGen)

  def clampedMillisGen(start: Long, end: Long): Gen[Millis] =
    Gen.choose(start, end).map(Millis.apply)

  def nowNextMillis(min: Long, max: Long): Gen[(Millis, Millis)] =
    for {
      t1 <- clampedMillisGen(min, max - 1)
      t2 <- clampedMillisGen(t1.value + 1, max)
    } yield (t1, t2)

  def minMaxDoubles(lower: Double, upper: Double): Gen[(Double, Double)] =
    for {
      d1 <- Gen.choose(lower, upper - 1)
      d2 <- Gen.choose(d1, upper)
    } yield (d1, d2)

  val radiansGen: Gen[Radians] =
    Gen.choose(0d, (2 * Math.PI)).map(Radians.apply)

  implicit val arbRadians: Arbitrary[Radians] =
    Arbitrary(radiansGen)

  val pointGen: Gen[Point] =
    for {
      x <- Gen.choose(-10000, 10000)
      y <- Gen.choose(-10000, 10000)
    } yield Point(x, y)

  implicit val arbPoint: Arbitrary[Point] =
    Arbitrary(pointGen)

  final case class Radius(value: Double)

  val clampedRadiusGen: Gen[Radius] =
    Gen.choose(10.0d, 100.0d).map(Radius.apply)
}
