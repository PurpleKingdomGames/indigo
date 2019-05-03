package indigo.shared.dice

import indigo.shared.collections.NonEmptyList

import scala.annotation.tailrec
import scala.util.Random

/**
  * All dice rolls are from value 1 to N inclusive.
  * Like a dice.
  */
trait Dice {
  val seed: Long
  def roll: Int
  def roll(sides: Int): Int
}

object Dice {

  val default: Long => Dice =
    Sides.MaxInt

  val isPositive: Int => Boolean =
    _ > 0

  val sanitise: Int => Int =
    i => Math.max(1, Math.abs(i))

  def roll(dice: Int, sides: Int, seed: Long): Option[NonEmptyList[Int]] = {
    @tailrec
    def rec(remaining: Int, acc: NonEmptyList[Int]): Option[NonEmptyList[Int]] =
      remaining match {
        case 0 =>
          Option(acc)

        case n =>
          rec(n - 1, diceSidesN(sides, seed).roll :: acc)
      }

    if (isPositive(dice) && isPositive(sides))
      rec(dice - 1, NonEmptyList(diceSidesN(sides, seed).roll))
    else
      None
  }

  def loaded(fixedTo: Int): Dice =
    new Dice {
      val seed: Long = 0

      def roll: Int =
        fixedTo

      def roll(sides: Int): Int =
        fixedTo
    }

  def arbitrary(from: Int, to: Int, seedValue: Long): Dice =
    new Dice {
      val seed: Long = seedValue

      def roll: Int =
        new Random(seed).nextInt(sanitise(to) - sanitise(from)) + sanitise(from)

      def roll(sides: Int): Int =
        diceSidesN(sides, seedValue).roll
    }

  def diceSidesN(sides: Int, seedValue: Long): Dice = {
    val r: Random = new Random(seedValue)

    new Dice {
      val seed: Long = seedValue

      def roll: Int =
        r.nextInt(sanitise(sides)) + 1

      def roll(sides: Int): Int =
        r.nextInt(sanitise(sides)) + 1
    }
  }

  val ZeroIndexed: Long => Dice =
    (seed: Long) => arbitrary(0, Int.MaxValue, seed)

  object Sides {
    val MaxInt: Long => Dice   = (seed: Long) => arbitrary(1, Int.MaxValue, seed)
    val One: Long => Dice      = (seed: Long) => diceSidesN(1, seed)
    val Two: Long => Dice      = (seed: Long) => diceSidesN(2, seed)
    val Three: Long => Dice    = (seed: Long) => diceSidesN(3, seed)
    val Four: Long => Dice     = (seed: Long) => diceSidesN(4, seed)
    val Five: Long => Dice     = (seed: Long) => diceSidesN(5, seed)
    val Six: Long => Dice      = (seed: Long) => diceSidesN(6, seed)
    val Seven: Long => Dice    = (seed: Long) => diceSidesN(7, seed)
    val Eight: Long => Dice    = (seed: Long) => diceSidesN(8, seed)
    val Nine: Long => Dice     = (seed: Long) => diceSidesN(9, seed)
    val Ten: Long => Dice      = (seed: Long) => diceSidesN(10, seed)
    val Eleven: Long => Dice   = (seed: Long) => diceSidesN(11, seed)
    val Twelve: Long => Dice   = (seed: Long) => diceSidesN(12, seed)
    val Thirteen: Long => Dice = (seed: Long) => diceSidesN(13, seed)
    val Fourteen: Long => Dice = (seed: Long) => diceSidesN(14, seed)
    val Fifteen: Long => Dice  = (seed: Long) => diceSidesN(15, seed)
    val Sixteen: Long => Dice  = (seed: Long) => diceSidesN(16, seed)
  }

}
