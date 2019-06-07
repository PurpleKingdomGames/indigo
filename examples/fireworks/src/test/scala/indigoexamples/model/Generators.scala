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

  val vertexGen: Gen[Vertex] =
    for {
      x <- Gen.choose(0d, 1d)
      y <- Gen.choose(0d, 1d)
    } yield Vertex(x, y)

  val millisGen: Gen[Millis] =
    Gen.choose(Long.MinValue, Long.MaxValue).map(Millis.apply)
}
