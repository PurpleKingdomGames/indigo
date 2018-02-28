package snake

import org.scalatest.{FunSpec, Matchers}

import scala.language.implicitConversions

class PathfindingSpec extends FunSpec with Matchers {

  implicit def tupleToCoorcs(t: (Int, Int)): Coords = Coords(t._1, t._2)

  describe("Coords") {

    it("should be able to convert zero indexed coordinates into a one dimensional array position") {

      Coords(0, 0).toGridPosition(3) shouldBe 0

    }

  }

  describe("Generating a grid") {

    it("should be able to generate a simple search grid") {

      val searchGrid = SearchGrid.generate((1, 1), (2, 1), 3, 3)

      searchGrid.isValid shouldBe true

      searchGrid.grid(4) shouldBe StartSquare
      searchGrid.grid(5) shouldBe EndSquare

    }

  }

  describe("Validating a grid") {

    it("should be able to spot a good grid") {

      val grid: List[GridSquare] = List(
        StartSquare,
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        EmptySquare(1),
        EndSquare
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe true
    }

    it("should be able to spot a grid missing a start") {

      val grid: List[GridSquare] = List(
        EmptySquare(1),
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        EmptySquare(1),
        EndSquare
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe false
    }

    it("should be able to spot a grid missing an end") {

      val grid: List[GridSquare] = List(
        StartSquare,
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        EmptySquare(1),
        EmptySquare(1)
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe false

    }

    it("should be able to spot a grid of the wrong size") {

      val grid: List[GridSquare] = List(
        StartSquare,
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        EmptySquare(1),
        EmptySquare(1),

        EmptySquare(1),
        //EmptySquare(1), //missing on purpose!
        EndSquare
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe false

    }

  }

}
