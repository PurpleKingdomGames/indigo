package indigo.shared.dice

import indigo.shared.collections.NonEmptyList

import scala.collection.immutable.SortedMap

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class DiceTests extends munit.FunSuite {

  given CanEqual[List[Int], List[Int]] = CanEqual.derived

  def checkDice(roll: Int, to: Int): Boolean =
    roll >= 1 && roll <= to

  def almostEquals(d: Double, d2: Double, p: Double) = (d - d2).abs <= p

  test("diceSidesN") {
    val roll: Int = Dice.diceSidesN(1, 0).roll(10)

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
    val dice   = Dice.fromSeed(0)
    val actual = dice.rollAlphaNumeric

    // Psuedorandom! Seed of 0 produces "CCzLNHBFHuRvbI1i"
    val expected =
      "CCzLNHBFHuRvbI1i"

    assertEquals(actual.length(), 16)
    assertEquals(actual, expected)
  }

  test("shuffle") {
    val dice   = Dice.fromSeed(0)
    val actual = dice.shuffle(List(1, 2, 3, 4, 5))

    // Psuedorandom! Seed of 0 produces List(5, 3, 2, 4, 1)
    val expected =
      List(5, 3, 2, 4, 1)

    assertEquals(actual.length, 5)
    assertEquals(actual, expected)
  }

  test("all dice rolls have an approximately uniform distribution") {
    val diceSides = 64
    val numRuns   = 200_000_000
    val dice      = Dice.diceSidesN(diceSides, 0)
    val generatedNums =
      Array
        .range(0, numRuns)
        .foldLeft(SortedMap[Int, Int]()) { (acc, _) =>
          val roll = dice.roll
          acc.updated(roll, acc.getOrElse(roll, 0) + 1)
        }

    assertEquals(generatedNums.size, diceSides)
    assertEquals(generatedNums.head._1, 1)
    assertEquals(generatedNums.last._1, diceSides)

    val expectedDistribution = 1.0 / diceSides
    generatedNums.foreach { case (num, count) =>
      val distribution = count.toDouble / numRuns
      assert(
        almostEquals(distribution, expectedDistribution, 0.01),
        s"""The distribution for $num was $distribution, but expected $expectedDistribution"""
      )
    }
  }

  test("all dice rolls in rollRange have an approximately uniform distribution") {
    val diceSides = 64
    val halfSides = diceSides / 2
    val numRuns   = 200_000_000
    val dice      = Dice.diceSidesN(diceSides, 0)
    val generatedNums =
      Array
        .range(0, numRuns)
        .foldLeft(SortedMap[Int, Int]()) { (acc, _) =>
          val roll = dice.rollRange(halfSides, diceSides)
          acc.updated(roll, acc.getOrElse(roll, 0) + 1)
        }

    assertEquals(generatedNums.size, halfSides + 1)
    assertEquals(generatedNums.head._1, halfSides)
    assertEquals(generatedNums.last._1, diceSides)

    val expectedDistribution = 1.0 / halfSides
    generatedNums.foreach { case (num, count) =>
      val distribution = count.toDouble / numRuns
      assert(
        almostEquals(distribution, expectedDistribution, 0.01),
        s"""The distribution for $num was $distribution, but expected $expectedDistribution"""
      )
    }
  }

  test("all dice rolls in rollRange(1, 4) have an approximately uniform distribution") {
    val numRuns = 200_000_000
    val dice    = Dice.diceSidesN(4, 0)
    val generatedNums =
      Array
        .range(0, numRuns)
        .foldLeft(SortedMap[Int, Int]()) { (acc, _) =>
          val roll = dice.rollRange(1, 4)
          acc.updated(roll, acc.getOrElse(roll, 0) + 1)
        }

    assertEquals(generatedNums.size, 4)
    assertEquals(generatedNums.head._1, 1)
    assertEquals(generatedNums.last._1, 4)

    val expectedDistribution = 0.25
    generatedNums.foreach { case (num, count) =>
      val distribution = count.toDouble / numRuns
      assert(
        almostEquals(distribution, expectedDistribution, 0.01),
        s"""The distribution for $num was $distribution, but expected $expectedDistribution"""
      )
    }
  }
}
