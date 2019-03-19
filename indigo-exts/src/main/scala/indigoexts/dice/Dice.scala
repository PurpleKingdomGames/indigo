package indigoexts.dice

import indigoexts.collections.NonEmptyList

import scala.annotation.tailrec
import scala.util.Random

/**
  * All dice rolls are from value 1 to N inclusive.
  * Like a dice.
  */
trait Dice {
  def roll: Int
  def roll(sides: Int): Int
}

object Dice {

  val default: Dice = Sides.MaxInt

  val isPositive: Int => Boolean = _ > 0

  val sanitise: Int => Int = i => Math.max(1, Math.abs(i))

  def roll(dice: Int, sides: Int): Option[NonEmptyList[Int]] = {
    @tailrec
    def rec(remaining: Int, acc: NonEmptyList[Int]): Option[NonEmptyList[Int]] =
      remaining match {
        case 0 =>
          Option(acc)

        case n =>
          rec(n - 1, diceSidesN(sides).roll :: acc)
      }

    if (isPositive(dice) && isPositive(sides))
      rec(dice - 1, NonEmptyList.point(diceSidesN(sides).roll))
    else
      None
  }

  def loaded(fixedTo: Int): Dice =
    new Dice {
      def roll: Int =
        fixedTo

      def roll(sides: Int): Int =
        fixedTo
    }

  def arbitrary(from: Int, to: Int): Dice =
    new Dice {
      def roll: Int =
        Random.nextInt(sanitise(to) - sanitise(from)) + sanitise(from)

      def roll(sides: Int): Int =
        diceSidesN(sides).roll
    }

  def diceSidesN(sides: Int): Dice =
    new Dice {
      def roll: Int =
        Random.nextInt(sanitise(sides - 1)) + 1

      def roll(sides: Int): Int =
        Random.nextInt(sanitise(sides - 1)) + 1
    }

  object Sides {
    val MaxInt: Dice   = arbitrary(1, Int.MaxValue)
    val One: Dice      = diceSidesN(1)
    val Two: Dice      = diceSidesN(2)
    val Three: Dice    = diceSidesN(3)
    val Four: Dice     = diceSidesN(4)
    val Five: Dice     = diceSidesN(5)
    val Six: Dice      = diceSidesN(6)
    val Seven: Dice    = diceSidesN(7)
    val Eight: Dice    = diceSidesN(8)
    val Nine: Dice     = diceSidesN(9)
    val Ten: Dice      = diceSidesN(10)
    val Eleven: Dice   = diceSidesN(11)
    val Twelve: Dice   = diceSidesN(12)
    val Thirteen: Dice = diceSidesN(13)
    val Fourteen: Dice = diceSidesN(14)
    val Fifteen: Dice  = diceSidesN(15)
    val Sixteen: Dice  = diceSidesN(16)
  }

}
