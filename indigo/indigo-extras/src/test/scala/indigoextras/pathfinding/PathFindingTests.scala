package indigoextras.pathfinding

import indigo.shared.dice.Dice
import indigoextras.pathfinding.GridSquare.EmptySquare
import indigoextras.pathfinding.GridSquare.EndSquare
import indigoextras.pathfinding.GridSquare.ImpassableSquare
import indigoextras.pathfinding.GridSquare.StartSquare

class PathFindingTests extends munit.FunSuite {

  val coords: Coords = Coords(0, 0)

  test("Finding an unobscured path.should be able to find a route") {

    val start: Coords      = Coords(2, 1)
    val end: Coords        = Coords(0, 2)
    val impassable: Coords = Coords(1, 0)

    val searchGrid = SearchGrid.generate(start, end, List(impassable), 3, 3)

    val path: List[Coords] = searchGrid.locatePath(Dice.fromSeed(0))

    val possiblePaths: List[List[Coords]] = List(
      List(start, Coords(2, 2), Coords(1, 2), end),
      List(start, Coords(1, 1), Coords(0, 1), end),
      List(start, Coords(1, 1), Coords(1, 2), end)
    )

    assertEquals(possiblePaths.contains(path), true)

  }

  test("Scoring the grid.should be able to score a grid") {
    val start: Coords      = Coords(2, 1)
    val end: Coords        = Coords(0, 2)
    val impassable: Coords = Coords(1, 0)

    val searchGrid = SearchGrid.generate(start, end, List(impassable), 3, 3)

    val expected: List[GridSquare] =
      List(
        EmptySquare(0, Coords(0, 0), Some(2)),
        ImpassableSquare(1, Coords(1, 0)),
        EmptySquare(2, Coords(2, 0), None), // Unscored squares are returned to keep sampleAt working correctly
        EmptySquare(3, Coords(0, 1), Some(1)),
        EmptySquare(4, Coords(1, 1), Some(2)),
        StartSquare(5, Coords(2, 1)),
        EndSquare(6, Coords(0, 2)),
        EmptySquare(7, Coords(1, 2), Some(1)),
        EmptySquare(8, Coords(2, 2), Some(2))
      )

    assertEquals(SearchGrid.score(searchGrid), SearchGrid(3, 3, start, end, expected))

  }

  test("Sampling the grid.should be able to take a sample in the middle of the map") {
    val start: Coords      = Coords(1, 1)
    val end: Coords        = Coords(3, 2)
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

    assertEquals(SearchGrid.sampleAt(searchGrid, Coords(2, 1), searchGrid.validationWidth), expected)
  }

  test("Sampling the grid.should be able to take a sample at the edge of the map") {
    val start: Coords      = Coords(1, 1)
    val end: Coords        = Coords(3, 2)
    val impassable: Coords = Coords(2, 2)

    val searchGrid = SearchGrid.generate(start, end, List(impassable), 4, 3)

    val expected: List[GridSquare] =
      List(
        EmptySquare(3, Coords(3, 0), None),
        EmptySquare(6, Coords(2, 1), None),
        //Sample point
        EndSquare(11, Coords(3, 2))
      )

    assertEquals(SearchGrid.sampleAt(searchGrid, Coords(3, 1), searchGrid.validationWidth), expected)
  }

  test("Sampling the grid.should be able to take a sample at the top left of the map") {
    val start: Coords      = Coords(1, 1)
    val end: Coords        = Coords(3, 2)
    val impassable: Coords = Coords(2, 2)

    val searchGrid = SearchGrid.generate(start, end, List(impassable), 4, 3)

    val expected: List[GridSquare] =
      List(
        //Sample point
        EmptySquare(1, Coords(1, 0), None),
        EmptySquare(4, Coords(0, 1), None)
      )

    assertEquals(SearchGrid.sampleAt(searchGrid, Coords(0, 0), searchGrid.validationWidth), expected)
  }

  test("Coords.should be able to convert zero indexed coordinates into a one dimensional array position") {

    assertEquals(Coords(0, 0).toGridPosition(4), 0)
    assertEquals(Coords(1, 1).toGridPosition(4), 5)
    assertEquals(Coords(2, 3).toGridPosition(4), 14)
    assertEquals(Coords(2, 2).toGridPosition(3), 8)

  }

  test("Coords.should be able to convert a zero indexed array position into coordinates") {

    assertEquals(Coords.fromIndex(0, 4), Coords(0, 0))
    assertEquals(Coords.fromIndex(5, 4), Coords(1, 1))
    assertEquals(Coords.fromIndex(14, 4), Coords(2, 3))
    assertEquals(Coords.fromIndex(8, 3), Coords(2, 2))

  }

  val start: Coords      = Coords(1, 1)
  val end: Coords        = Coords(3, 2)
  val impassable: Coords = Coords(2, 2)

  val searchGrid = SearchGrid.generate(start, end, List(impassable), 4, 3)

  test("Generating a grid.should be able to generate a simple search grid.is valid") {
    assertEquals(searchGrid.isValid, true)
  }

  test("Generating a grid.should be able to generate a simple search grid.start") {
    assertEquals(searchGrid.grid(start.toGridPosition(4)), StartSquare(5, start))
  }

  test("Generating a grid.should be able to generate a simple search grid.end") {
    assertEquals(searchGrid.grid(end.toGridPosition(4)), EndSquare(11, end))
  }

  test("Generating a grid.should be able to generate a simple search grid.impassable") {
    assertEquals(searchGrid.grid(impassable.toGridPosition(4)), ImpassableSquare(10, impassable))
  }

  test("Validating a grid.should be able to spot a good grid") {
    val start: Coords = Coords(1, 1)
    val end: Coords   = Coords(2, 2)

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

    assertEquals(SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)), true)
  }

  test("Validating a grid.should be able to spot a grid missing a start") {
    val start: Coords = Coords(1, 1)
    val end: Coords   = Coords(2, 2)

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

    assertEquals(SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)), false)
  }

  test("Validating a grid.should be able to spot a grid missing an end") {
    val start: Coords = Coords(1, 1)
    val end: Coords   = Coords(2, 2)

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

    assertEquals(SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)), false)

  }

  test("Validating a grid.should be able to spot a grid of the wrong size") {
    val start: Coords = Coords(1, 1)
    val end: Coords   = Coords(2, 2)

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

    assertEquals(SearchGrid.isValid(SearchGrid(3, 3, start, end, grid)), false)

  }

}
