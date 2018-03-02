package snake

import org.scalatest.{FunSpec, Matchers}

import scala.language.implicitConversions

class PathfindingSpec extends FunSpec with Matchers {

  val coords: Coords = Coords(0, 0)

  implicit def tupleToCoords(t: (Int, Int)): Coords = Coords(t._1, t._2)

  describe("Finding an unobscured path") {
    
    pending
    
  }

  describe("Scoring the grid") {

    it("should be able to score a grid") {
      val start: Coords = Coords(2, 1)
      val end: Coords = Coords(0, 2)
      val impassable: Coords = Coords(1, 0)

      val searchGrid = SearchGrid.generate(start, end, List(impassable), 3, 3)

      val expected: List[GridSquare] =
        List(
          EmptySquare(0, Coords(0, 0), Some(2)),
          ImpassableSquare(1, Coords(1, 0)),
//          EmptySquare(2, Coords(2, 0), None), // Unscored squares are not returned
          EmptySquare(3, Coords(0, 1), Some(1)),
          EmptySquare(4, Coords(1, 1), Some(2)),
          StartSquare(5, Coords(2, 1)),
          EndSquare(6, Coords(0, 2)),
          EmptySquare(7, Coords(1, 2), Some(1)),
          EmptySquare(8, Coords(2, 2), Some(2))
        )

      searchGrid.score shouldEqual SearchGrid(3, 3, start, end, expected)

    }

  }

  describe("Sampling the grid") {

    it("should be able to take a sample in the middle of the map") {
      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(3, 2)
      val impassable: Coords = Coords(2, 2)

      val searchGrid = SearchGrid.generate(start, end, List(impassable), 4, 3)

      val expected: List[GridSquare] =
        List(
          EmptySquare(2, Coords(2, 0), None),
          StartSquare(5, Coords(1, 1)),
          //Sample point
          EmptySquare(7, Coords(3, 1), None),
          ImpassableSquare(10, Coords(2, 2))
        )

      searchGrid.sampleAt(Coords(2, 1)) shouldEqual expected
    }

    it("should be able to take a sample at the edge of the map") {
      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(3, 2)
      val impassable: Coords = Coords(2, 2)

      val searchGrid = SearchGrid.generate(start, end, List(impassable), 4, 3)

      val expected: List[GridSquare] =
        List(
          EmptySquare(3, Coords(3, 0), None),
          EmptySquare(6, Coords(2, 1), None),
          //Sample point
          EndSquare(11, Coords(3, 2))
        )

      searchGrid.sampleAt(Coords(3, 1)) shouldEqual expected
    }

    it("should be able to take a sample at the top left of the map") {
      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(3, 2)
      val impassable: Coords = Coords(2, 2)

      val searchGrid = SearchGrid.generate(start, end, List(impassable), 4, 3)

      val expected: List[GridSquare] =
      List(
        //Sample point
        EmptySquare(1, Coords(1, 0), None),
        EmptySquare(4, Coords(0, 1), None)
      )

      searchGrid.sampleAt(Coords(0, 0)) shouldEqual expected
    }

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
      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(2, 2)

      val grid: List[GridSquare] = List(
        StartSquare(0, start),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EndSquare(0, end)
      )

      SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)) shouldBe true
    }

    it("should be able to spot a grid missing a start") {
      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(2, 2)

      val grid: List[GridSquare] = List(
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EndSquare(0, end)
      )

      SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)) shouldBe false
    }

    it("should be able to spot a grid missing an end") {
      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(2, 2)

      val grid: List[GridSquare] = List(
        StartSquare(0, start),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None)
      )

      SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)) shouldBe false

    }

    it("should be able to spot a grid of the wrong size") {
      val start: Coords = Coords(1, 1)
      val end: Coords = Coords(2, 2)

      val grid: List[GridSquare] = List(
        StartSquare(0, start),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),
        EmptySquare(0, coords, None),

        EmptySquare(0, coords, None),
        //EmptySquare(0, coords, None), //missing on purpose!
        EndSquare(0, end)
      )

      SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)) shouldBe false

    }

  }

}
