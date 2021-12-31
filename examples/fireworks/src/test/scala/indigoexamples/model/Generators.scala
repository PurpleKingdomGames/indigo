package indigoexamples.model

import indigo._
import indigoextras.geometry.Vertex
import org.scalacheck._

object Generators {

  // Dice
  val diceGen: Gen[Dice] =
    for {
      sides <- Gen.choose(1, Int.MaxValue)
      seed  <- Gen.choose(0L, Long.MaxValue)
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

  // Seconds
  val millisGen: Gen[Seconds] =
    clampedSecondsGen(Long.MinValue, Long.MaxValue)

  implicit val arbSeconds: Arbitrary[Seconds] =
    Arbitrary(millisGen)

  def clampedSecondsGen(start: Double, end: Double): Gen[Seconds] =
    Gen.choose(start, end).map(s => Seconds(s))

  def nowNextSeconds(min: Double, max: Double): Gen[(Seconds, Seconds)] =
    for {
      t1 <- clampedSecondsGen(min, max - 1)
      t2 <- clampedSecondsGen(t1.toDouble + 1, max)
    } yield (t1, t2)

  // Doubles
  def minMaxDoubles(lower: Double, upper: Double): Gen[(Double, Double)] =
    for {
      d1 <- Gen.choose(lower, upper - 1)
      d2 <- Gen.choose(d1, upper)
    } yield (d1, d2)

  // Radians
  val radiansGen: Gen[Radians] =
    Gen.choose(0d, (2 * Math.PI)).map(r => Radians(r))

  // Radius
  final case class Radius(value: Double)

  val clampedRadiusGen: Gen[Radius] =
    Gen.choose(0.0d, 1.0d).map(Radius.apply)
}
