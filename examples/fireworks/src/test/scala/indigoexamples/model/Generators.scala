package indigoexamples.model

import org.scalacheck._

import indigo._
import indigoexts.geometry.Vertex

object Generators {

  // Dice
  val diceGen: Gen[Dice] =
    for {
      sides <- Gen.choose(1, Int.MaxValue)
      seed  <- Gen.choose(0, Long.MaxValue)
    } yield Dice.diceSidesN(sides, seed)

  implicit val arbDice: Arbitrary[Dice] =
    Arbitrary(diceGen)

  // Vertex
  val vertexGen: Gen[Vertex] =
    for {
      x <- Gen.choose(-1.0d, 1.0d)
      y <- Gen.choose(-1.0d, 1.0d)
    } yield Vertex(x, y)

  implicit val arbVertex: Arbitrary[Vertex] =
    Arbitrary(vertexGen)

  val launchPadVertexGen: Gen[Vertex] =
    Gen.choose(-1d, 1d).map(v => Vertex(v, 0))

  def rocketTargetVertexGen(launchVertex: Vertex): Gen[Vertex] =
    for {
      x <- Gen.choose(-0.5d, 0.5d)
      y <- Gen.choose(0d, 2d)
    } yield Vertex(launchVertex.x + x, y)

  def vertexClamped(minX: Double, maxX: Double, minY: Double, maxY: Double): Gen[Vertex] =
    for {
      x <- Gen.choose(minX, maxX)
      y <- Gen.choose(minY, maxY)
    } yield Vertex(x, y)

  // Millis
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

  // Doubles
  def minMaxDoubles(lower: Double, upper: Double): Gen[(Double, Double)] =
    for {
      d1 <- Gen.choose(lower, upper - 1)
      d2 <- Gen.choose(d1, upper)
    } yield (d1, d2)

  // Radians
  val radiansGen: Gen[Radians] =
    Gen.choose(0d, (2 * Math.PI)).map(Radians.apply)

  implicit val arbRadians: Arbitrary[Radians] =
    Arbitrary(radiansGen)

  // Points
  val pointGen: Gen[Point] =
    for {
      x <- Gen.choose(-10000, 10000)
      y <- Gen.choose(-10000, 10000)
    } yield Point(x, y)

  implicit val arbPoint: Arbitrary[Point] =
    Arbitrary(pointGen)

  // Radius
  final case class Radius(value: Double)

  val clampedRadiusGen: Gen[Radius] =
    Gen.choose(10.0d, 100.0d).map(Radius.apply)
}
