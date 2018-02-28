package snake

import org.scalatest.{FunSpec, Matchers}

import scala.language.implicitConversions

class PathfindingSpec extends FunSpec with Matchers {

  val coords: Coords = Coords(0, 0)

  implicit def tupleToCoords(t: (Int, Int)): Coords = Coords(t._1, t._2)

  describe("Finding an unobscured path") {
    
    pending
    
  }
  
  describe("Coords") {

    it("should be able to convert zero indexed coordinates into a one dimensional array position") {

      Coords(0, 0).toGridPosition(4) shouldBe 0
      Coords(1, 1).toGridPosition(4) shouldBe 5
      Coords(2, 3).toGridPosition(4) shouldBe 14
      Coords(2, 2).toGridPosition(3) shouldBe 8

    }

    it("should be able to convert a zero indexed array position into coordinates") {

      Coords.fromIndex(0, 4) shouldBe Coords(0, 0)
      Coords.fromIndex(5, 4) shouldBe Coords(1, 1)
      Coords.fromIndex(14, 4) shouldBe Coords(2, 3)
      Coords.fromIndex(8, 3) shouldBe Coords(2, 2)

    }

  }

  describe("Generating a grid") {

    it("should be able to generate a simple search grid") {

      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(3, 2)
      val impassable: Coords = Coords(2, 2)

      val searchGrid = SearchGrid.generate(start, end, List(impassable), 4, 3)

      withClue("is valid") {
        searchGrid.isValid shouldBe true
      }

      withClue("start") {
        searchGrid.grid(start.toGridPosition(4)) shouldBe StartSquare(5, start)
      }

      withClue("end") {
        searchGrid.grid(end.toGridPosition(4)) shouldBe EndSquare(11, end)
      }

      withClue("impassable") {
        searchGrid.grid(impassable.toGridPosition(4)) shouldBe ImpassableSquare(10, impassable)
      }

    }

  }

  describe("Validating a grid") {

    it("should be able to spot a good grid") {

      val grid: List[GridSquare] = List(
        StartSquare(0, coords),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EndSquare(0, coords)
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe true
    }

    it("should be able to spot a grid missing a start") {

      val grid: List[GridSquare] = List(
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EndSquare(0, coords)
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe false
    }

    it("should be able to spot a grid missing an end") {

      val grid: List[GridSquare] = List(
        StartSquare(0, coords),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None)
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe false

    }

    it("should be able to spot a grid of the wrong size") {

      val grid: List[GridSquare] = List(
        StartSquare(0, coords),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),
        EmptySquare(0, coords, 1, None),

        EmptySquare(0, coords, 1, None),
        //EmptySquare(0, coords, 1, None), //missing on purpose!
        EndSquare(0, coords)
      )

      SearchGrid.isValid(SearchGrid(3, 3, grid)) shouldBe false

    }

  }

}
