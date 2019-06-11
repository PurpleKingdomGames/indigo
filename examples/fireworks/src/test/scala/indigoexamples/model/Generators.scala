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
}
