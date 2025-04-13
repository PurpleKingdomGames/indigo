package indigo.shared.dice

import indigo.shared.collections.NonEmptyList

import scala.collection.immutable.SortedMap

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DiceTests extends munit.FunSuite {
  // If you're worried it's not working, use this config instead.
  // val numRuns = 200_000_000
  val numRuns = 2_000_000

  given CanEqual[List[Int], List[Int]] = CanEqual.derived

  def checkDice(roll: Int, to: Int): Boolean =
    roll >= 1 && roll <= to

  def almostEquals(d: Double, d2: Double, p: Double) = (d - d2).abs <= p

  test("diceSidesN") {
    val roll: Int = Dice.diceSidesN(1, 1000).roll(10)

    assertEquals(checkDice(roll, 10), true)
  }

  test("should have a roll multiple dice function") {
    Dice.rollMany(2, 6, 0) match {
      case Some(NonEmptyList(d1, d2 :: Nil)) =>
        assertEquals(checkDice(d1, 6), true)

        assertEquals(checkDice(d2, 6), true)

      case _ =>
        throw new java.lang.AssertionError("Match fail!")
    }
  }

  test("should not roll with invalid values") {
    assertEquals(Dice.rollMany(0, 6, 0), None)
    assertEquals(Dice.rollMany(4, 0, 0), None)
  }

  test("should be able to produce an alphanumeric string") {
    val dice   = Dice.default
    val actual = dice.rollAlphaNumeric

    val expected =
      "IoLMAKmKvLY5MSfL"

    assertEquals(actual.length(), 16)
    assertEquals(actual, expected)
  }

  test("shuffle") {
    val dice   = Dice.default
    val actual = dice.shuffle(List(1, 2, 3, 4, 5))

    val expected =
      List(2, 5, 4, 1, 3)

    assertEquals(actual.length, 5)
    assertEquals(actual, expected)
  }

  test("should be able to produce boolean values") {
    val dice   = Dice.default
    val actual = List.fill(10)(dice.rollBoolean)

    val expected: List[Boolean] =
      List(
        true, true, false, true, true, true, true, true, false, false
      )

    assertEquals(actual, expected)
  }

  test("should be able to produce Int values") {
    val dice   = Dice.default
    val actual = List.fill(10)(dice.roll)

    val expected: List[Int] =
      List(
        11355433, 1458948949, 476557060, 646921281, 534983741, 1441438135, 581500457, 1863322963, 1174750318, 1067267640
      )

    assertEquals(actual, expected)
  }

  test("should be able to produce Long values") {
    val dice   = Dice.default
    val actual = List.fill(10)(dice.rollLong)

    val expected: List[Long] =
      List(
        711245566, 306192841, 1776012994, 878840226, 1282232996, 1380324889, 1361527385, 705894190, 1128366901,
        1044750824
      )

    assertEquals(actual, expected)
  }

  test("should be able to produce Double values") {
    val dice   = Dice.default
    val actual = List.fill(10)(dice.rollDouble)

    val expected: List[Double] =
      List(
        0.005287785390537222, 0.2219141739650522, 0.7508787830991721, 0.729217749352642, 0.4529642552363186,
        0.39186976607271856, 0.4249471892873046, 0.9117737604549992, 0.25252137333629515, 0.6249562369807594
      )

    assertEquals(actual, expected)
  }

  test("should be able to produce Float values") {
    val dice   = Dice.default
    val actual = List.fill(10)(dice.rollFloat)

    val expected: List[Float] =
      List(
        0.002643892541527748f, 0.6603119969367981f, 0.1109570860862732f, 0.849376916885376f, 0.8754394054412842f,
        0.335610955953598f, 0.864608883857727f, 0.5661613345146179f, 0.7264821529388428f, 0.24849261343479156f
      )

    def floatsEqual(a: Float, b: Float): Boolean =
      Math.abs(a - b) < 0.0001

    assert(actual.length == expected.length)
    assert(
      actual.zip(expected).forall { case (a, b) =>
        floatsEqual(a, b)
      }
    )
  }

  test("should be able to roll within a range") {
    val dice   = Dice.default
    val actual = List.fill(10)(dice.rollRange(10, 20))

    val expected: List[Int] =
      List(
        10, 16, 10, 15, 15, 14, 19, 16, 14, 19
      )

    assert(actual.forall(i => i >= 10 && i <= 20))
    assertEquals(actual, expected)
  }

  test("all dice rolls have an approximately uniform distribution") {
    val diceSides            = 63
    val expectedDistribution = 1.0 / diceSides
    val generatedNums =
      Array
        .range(0, numRuns)
        .foldLeft(SortedMap[Int, Int]()) { (acc, _) =>
          val roll = Dice.diceSidesN(diceSides, scala.util.Random.nextLong()).roll
          acc.updated(roll, acc.getOrElse(roll, 0) + 1)
        }

    // Ensure that we have the right numbers generated (they should all have been created)
    assertEquals(generatedNums.size, diceSides)
    assertEquals(generatedNums.head._1, 1)
    assertEquals(generatedNums.last._1, diceSides)

    // Check the even distribution of the generated numbers
    generatedNums.foreach { case (num, count) =>
      val distribution = count.toDouble / numRuns
      assert(
        almostEquals(distribution, expectedDistribution, 0.01),
        s"""The distribution for $num was $distribution, but expected $expectedDistribution"""
      )
    }
  }

  test("all dice rolls in rollRange have an approximately uniform distribution") {
    val diceSides            = 63
    val halfSides            = Math.floor(diceSides / 2.0).toInt
    val expectedDistribution = 1.0 / halfSides
    val generatedNums =
      Array
        .range(0, numRuns)
        .foldLeft(SortedMap[Int, Int]()) { (acc, _) =>
          val roll = Dice.diceSidesN(diceSides, scala.util.Random.nextLong()).rollRange(halfSides, diceSides)
          acc.updated(roll, acc.getOrElse(roll, 0) + 1)
        }

    // Ensure that we have the right numbers generated (only numbers from just before half way through the number of sides should have een created)
    assertEquals(generatedNums.size, (diceSides - halfSides) + 1)
    assertEquals(generatedNums.head._1, halfSides)
    assertEquals(generatedNums.last._1, diceSides)

    // Check the even distribution of the generated numbers
    generatedNums.foreach { case (num, count) =>
      val distribution = count.toDouble / numRuns
      assert(
        almostEquals(distribution, expectedDistribution, 0.01),
        s"""The distribution for $num was $distribution, but expected $expectedDistribution"""
      )
    }
  }

  test("all dice rolls in rollRange(1, 4) have an approximately uniform distribution") {
    val expectedDistribution = 0.25
    val generatedNums =
      Array
        .range(0, numRuns)
        .foldLeft(SortedMap[Int, Int]()) { (acc, _) =>
          val roll = Dice.diceSidesN(4, scala.util.Random.nextLong()).rollRange(1, 4)
          acc.updated(roll, acc.getOrElse(roll, 0) + 1)
        }

    // Ensure that we have the right numbers generated (they should all have been created)
    assertEquals(generatedNums.size, 4)
    assertEquals(generatedNums.head._1, 1)
    assertEquals(generatedNums.last._1, 4)

    // Check the even distribution of the generated numbers
    generatedNums.foreach { case (num, count) =>
      val distribution = count.toDouble / numRuns
      assert(
        almostEquals(distribution, expectedDistribution, 0.01),
        s"""The distribution for $num was $distribution, but expected $expectedDistribution"""
      )
    }
  }
}
