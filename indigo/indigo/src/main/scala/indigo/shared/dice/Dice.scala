package indigo.shared.dice

import indigo.Batch
import indigo.shared.collections.NonEmptyList
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

import scala.annotation.tailrec

/** The Dice primitive supplies a consistent way to get psuedo-random values into your game.
  *
  * A dice instance can be found in the Context object with 'max int' sides, and every frame the dice's seed value is
  * set to the current running time of the game in milliseconds.
  *
  * Dice also serve as a handy proxy to a number of functions found on a normal `Random` instance, like alphanumeric,
  * but with a predicatable seed.
  */
trait Dice:

  /** The seed value of the dice. The dice supplied in the `Context` has the seed set to the current running time of the
    * game in milliseconds.
    *
    * If the seed value is 0, it is replaced with the `DefaultSeed` value in order for the underlying PRNG to work
    * correctly.
    */
  def seed: Long

  /** Roll an Int from 1 to the number of sides on the dice (inclusive)
    */
  def roll: Int

  /** Roll an Int from 1 to the specified number of sides (inclusive), using this dice instance as the seed.
    */
  def roll(sides: Int): Int

  /** Roll a Long from 1 to the number of sides on the dice (inclusive)
    */
  def rollLong: Long

  /** Roll a Long from 1 to the specified number of sides (inclusive), using this dice instance as the seed.
    */
  def rollLong(sides: Int): Long

  /** Roll an Int from 0 to the number of sides on the dice (inclusive)
    */
  def rollFromZero: Int

  /** Roll an Int from 0 to the specified number of sides (inclusive), using this dice instance as the seed.
    */
  def rollFromZero(sides: Int): Int

  /** Roll an Int from the range provided (inclusive), using this dice instance as the seed.
    */
  def rollRange(from: Int, to: Int): Int

  /** Produces a random Float from 0.0f to 1.0f
    */
  def rollFloat: Float

  /** Produces a random Double from 0.0 to 1.0
    */
  def rollDouble: Double

  /** Produces an alphanumeric string of the specified length
    */
  def rollAlphaNumeric(length: Int): String

  /** Produces an alphanumeric string 16 characters long
    */
  def rollAlphaNumeric: String

  /** Produces a random Boolean
    */
  def rollBoolean: Boolean = roll(2) == 1

  /** Shuffles a list of values into a random order
    */
  def shuffle[A](items: List[A]): List[A]

  /** Shuffles a Batch of values into a random order
    */
  def shuffle[A](items: Batch[A]): Batch[A]

  override def toString: String =
    val seedValue =
      if seed == 0L then s"${seed.toString} (substituted with ${Dice.DefaultSeed.toString})"
      else seed.toString

    s"Dice(seed = $seedValue)"

object Dice:

  val DefaultSeed: Long = 42L

  /** Construct a 'max int' sided dice using a time in seconds (converted to millis) as the seed.
    */
  def fromSeconds(time: Seconds): Dice =
    Sides.MaxInt(time.toMillis.toLong)

  /** Construct a 'max int' sided dice using a time in milliseconds as the seed.
    */
  def fromMillis(time: Millis): Dice =
    Sides.MaxInt(time.toLong)

  /** Construct a 'max int' sided dice from a given seed value. This is the default dice presented by the `Context`.
    *
    * The implementation of this method uses the Xorshift algorithm to generate random numbers, which has a problem: A
    * seed value of 0 will produce a value of 0. This is a known issue with the algorithm, and while it is not a bug, it
    * will cause unexpected behaviour. Hence, if the seed value is 0, it is replaced with the `DefaultSeed` value.
    */
  def fromSeed(seed: Long): Dice =
    Sides.MaxInt(seed)

  def default: Dice =
    Sides.MaxInt(DefaultSeed)

  private val isPositive: Int => Boolean =
    _ > 0

  /** Rolls a number of dice.
    *
    * @param numberOfDice
    *   How many dice to roll
    * @param sides
    *   How many sides the dice all have
    * @param seed
    *   The seed value to based the dice on
    * @return
    *   Returns and Optional NonEmtpyList of Int's, where None is produced when the input values are invalid.
    */
  def rollMany(numberOfDice: Int, sides: Int, seed: Long): Option[NonEmptyList[Int]] =
    @tailrec
    def rec(remaining: Int, acc: NonEmptyList[Int]): Option[NonEmptyList[Int]] =
      remaining match {
        case 0 =>
          Option(acc)

        case n =>
          rec(n - 1, diceSidesN(sides, seed).roll :: acc)
      }

    if isPositive(numberOfDice) && isPositive(sides) then
      rec(numberOfDice - 1, NonEmptyList(diceSidesN(sides, seed).roll))
    else None

  /** Constructs a 'loaded' dice where everything returns a fixed value. Intended for use during testing.
    */
  def loaded(fixedTo: Int): Dice =
    new Dice {
      val seed: Long = Dice.DefaultSeed

      def roll: Int =
        fixedTo

      def roll(sides: Int): Int =
        fixedTo

      def rollLong: Long =
        fixedTo.toLong

      def rollLong(sides: Int): Long =
        fixedTo.toLong

      def rollFromZero: Int =
        fixedTo

      def rollFromZero(sides: Int): Int =
        fixedTo

      def rollRange(from: Int, to: Int): Int =
        fixedTo

      def rollFloat: Float =
        if (fixedTo == 0) 0 else 1

      def rollDouble: Double =
        if (fixedTo == 0) 0 else 1

      def rollAlphaNumeric(length: Int): String =
        List.fill(length)(fixedTo.toString()).mkString.take(length)

      def rollAlphaNumeric: String =
        rollAlphaNumeric(16)

      def shuffle[A](items: List[A]): List[A] =
        items

      def shuffle[A](items: Batch[A]): Batch[A] =
        items
    }

  /** Constructs a dice with a given number of sides and a seed value.
    */
  def diceSidesN(sides: Int, seedValue: Long): Dice =
    new Dice {

      // The Xorshift algorithm requires a seed value that is not 0, as a seed of 0 will produce a value of 0.
      val seed: Long = if seedValue == 0L then DefaultSeed else seedValue

      private val r: RandomImpl = new RandomImpl(seed.toInt)

      /** Roll an Int from 1 to the number of sides on the dice (inclusive)
        *
        * @return
        */
      def roll: Int =
        r.nextInt(sides) + 1

      /** Roll an Int from 1 to the specified number of sides (inclusive)
        *
        * @param sides
        * @return
        */
      def roll(sides: Int): Int =
        r.nextInt(sides) + 1

      /** Roll a Long from 1 to the number of sides on the dice (inclusive)
        */
      def rollLong: Long =
        r.nextLong(sides) + 1

      /** Roll a Long from 1 to the specified number of sides (inclusive), using this dice instance as the seed.
        */
      def rollLong(sides: Int): Long =
        r.nextLong(sides) + 1

      /** Roll an Int from 0 to the number of sides on the dice (exclusive)
        *
        * @return
        */
      def rollFromZero: Int =
        rollFromZero(sides)

      /** Roll an Int from 0 to the specified number of sides (exclusive)
        *
        * @param sides
        * @return
        */
      def rollFromZero(sides: Int): Int =
        r.nextInt(sides)

      /** Roll an Int from the range provided (inclusive)
        *
        * @param from
        * @param to
        * @return
        */
      def rollRange(from: Int, to: Int): Int =
        val f = Math.min(from, to)
        val t = Math.max(from, to)
        roll(t - f + 1) + f - 1

      /** Produces a random Float from 0.0 to 1.0
        *
        * @return
        */
      def rollFloat: Float =
        r.nextFloat()

      /** Produces a random Double from 0.0 to 1.0
        *
        * @return
        */
      def rollDouble: Double =
        r.nextDouble()

      /** Produces a random alphanumeric string of the specified length
        *
        * @param length
        * @return
        */
      def rollAlphaNumeric(length: Int): String =
        r.alphanumeric(length)

      /** Produces a random alphanumeric string 16 characters long
        *
        * @return
        */
      def rollAlphaNumeric: String =
        rollAlphaNumeric(16)

      /** Shuffles a Batch of values into a random order
        *
        * @param items
        * @return
        */
      def shuffle[A](items: Batch[A]): Batch[A] =
        r.shuffle(items)

      /** Shuffles a List of values into a random order
        *
        * @param items
        * @return
        */
      def shuffle[A](items: List[A]): List[A] =
        r.shuffle(Batch.fromList(items)).toList
    }

  /** Pre-constructed dice with a fixed number of sides, rolls are includive and start at 1, not 0. You need to provide
    * a seed value.
    */
  object Sides:

    /** A dice with 2147483647 - 1 (no zero) sides
      */
    def MaxInt(seed: Long): Dice = diceSidesN(Int.MaxValue, seed)

    /** A one-sided dice.
      */
    def One(seed: Long): Dice = diceSidesN(1, seed)

    /** A two-sided dice.
      */
    def Two(seed: Long): Dice = diceSidesN(2, seed)

    /** A three-sided dice.
      */
    def Three(seed: Long): Dice = diceSidesN(3, seed)

    /** A four-sided dice.
      */
    def Four(seed: Long): Dice = diceSidesN(4, seed)

    /** A five-sided dice.
      */
    def Five(seed: Long): Dice = diceSidesN(5, seed)

    /** A six-sided dice.
      */
    def Six(seed: Long): Dice = diceSidesN(6, seed)

    /** A seven-sided dice.
      */
    def Seven(seed: Long): Dice = diceSidesN(7, seed)

    /** An eight-sided dice.
      */
    def Eight(seed: Long): Dice = diceSidesN(8, seed)

    /** A nine-sided dice.
      */
    def Nine(seed: Long): Dice = diceSidesN(9, seed)

    /** A ten-sided dice.
      */
    def Ten(seed: Long): Dice = diceSidesN(10, seed)

    /** An eleven-sided dice.
      */
    def Eleven(seed: Long): Dice = diceSidesN(11, seed)

    /** A twelve-sided dice.
      */
    def Twelve(seed: Long): Dice = diceSidesN(12, seed)

    /** A thirteen-sided dice.
      */
    def Thirteen(seed: Long): Dice = diceSidesN(13, seed)

    /** A forteen-sided dice.
      */
    def Fourteen(seed: Long): Dice = diceSidesN(14, seed)

    /** A fifteen-sided dice.
      */
    def Fifteen(seed: Long): Dice = diceSidesN(15, seed)

    /** A sixteen-sided dice.
      */
    def Sixteen(seed: Long): Dice = diceSidesN(16, seed)

  /** A simple random number generator based on the Xorshift algorithm.
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  final class RandomImpl(_seed: Int) {

    private var seed: Int = _seed

    // Xorshift for 32-bit integers, the backbone of this random number generator
    def nextInt(): Int = {
      var x = seed
      x ^= x << 13
      x ^= x >>> 17
      x ^= x << 5
      seed = x
      x
    }

    // Generate an Int up to a limit value
    def nextInt(limit: Int): Int =
      Math.abs(nextInt()) % limit

    // Generate a Long by combining two nextInt() calls
    def nextLong(): Long =
      (nextInt().toLong << 32) | (nextInt().toLong & 0xffffffffL)

    // Generate a Long up to a limit value
    def nextLong(limit: Int): Long =
      Math.abs(nextLong()) % limit

    // Generates a Float between 0.0 and 1.0
    // Use of the bit mask ensures the result is positive while preserving the number distribution.
    def nextFloat(): Float =
      (nextInt().toLong & 0xffffffffL) / (1L << 32).toFloat

    // Generates a Double between 0.0 and 1.0
    def nextDouble(): Double =
      (nextLong() & Long.MaxValue) / (Long.MaxValue.toDouble + 1)

    // Generates a Boolean
    def nextBoolean(): Boolean =
      nextInt() % 2 == 0

    // Fisher-Yates shuffle for List[A]
    // More efficient but less readable than `items.sortBy(_ => nextInt())`
    def shuffle[A](items: Batch[A]): Batch[A] =
      val array = items.toJSArray

      for (i <- array.indices.reverse) {
        val j    = nextInt().abs % (i + 1)
        val temp = array(i)
        array(i) = array(j)
        array(j) = temp
      }

      Batch(array)

    // Generate an alphanumeric string of a given length
    def alphanumeric(limit: Int): String =
      val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
      (1 to limit).map(_ => chars(nextInt().abs % chars.length)).mkString

    // Method to reset the seed for reproducibility
    def setSeed(newSeed: Int): Unit =
      seed = newSeed
  }
