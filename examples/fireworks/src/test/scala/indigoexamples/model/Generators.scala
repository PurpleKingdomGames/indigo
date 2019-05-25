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
    for {
      x <- Gen.choose(0, Int.MaxValue)
      y <- Gen.choose(0, Int.MaxValue)
    } yield Point(x, y)

  val millisGen: Gen[Millis] =
    Gen.choose(Long.MinValue, Long.MaxValue).map(Millis.apply)
}
