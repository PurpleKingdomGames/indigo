package snake

/**
  * Briefly considered trying to do something clever (A* perhaps?),
  * but I have small grids, so I'm doing something crude instead.
  */
object Pathfinding {

  //

}

case class SearchGrid(validationWidth: Int, validationHeight: Int, grid: List[GridSquare]) {

  def isValid: Boolean =
    SearchGrid.isValid(this)

}

object SearchGrid {

  def isValid(searchGrid: SearchGrid): Boolean = {
    searchGrid.grid.lengthCompare(searchGrid.validationWidth * searchGrid.validationHeight) == 0 &&
      searchGrid.grid.exists(_.isStart) && searchGrid.grid.exists(_.isEnd)
  }

  def generate(start: Coords, end: Coords, gridWidth: Int, gridHeight: Int): SearchGrid = {
    val grid: List[GridSquare] = (0 until (gridWidth * gridHeight)).toList.map { index =>
      Coords.fromIndex(index, gridWidth) match {
        case c: Coords if c === start =>
          StartSquare(index, start)

        case c: Coords if c === end =>
          EndSquare(index, end)

        case c: Coords =>
          EmptySquare(index, c, 1)

      }
    }

    SearchGrid(gridWidth, gridHeight, grid)
  }

}

case class Coords(x: Int, y: Int) {

  def toGridPosition(gridWidth: Int): Int =
    Coords.toGridPosition(this, gridWidth)

  def ===(other: Coords): Boolean =
    Coords.equality(this, other)

}

object Coords {

  def toGridPosition(coords: Coords, gridWidth: Int): Int =
    coords.x + (coords.y * gridWidth)

  def fromIndex(index: Int, gridWidth: Int): Coords = {
    Coords(
      x = index % gridWidth,
      y = index / gridWidth
    )
  }

  def equality(a: Coords, b: Coords): Boolean =
    a.x == b.x && a.y == b.y

}

sealed trait GridSquare {
  val index: Int
  val coords: Coords
  val weight: Int
  val name: String
  val isStart: Boolean
  val isEnd: Boolean
}
case class EmptySquare(index: Int, coords: Coords, weight: Int) extends GridSquare {
  val name: String = "empty"
  val isStart: Boolean = false
  val isEnd: Boolean = false
}
case class StartSquare(index: Int, coords: Coords) extends GridSquare {
  val weight: Int = 0
  val name: String = "start"
  val isStart: Boolean = true
  val isEnd: Boolean = false
}
case class EndSquare(index: Int, coords: Coords) extends GridSquare {
  val weight: Int = 0
  val name: String = "end"
  val isStart: Boolean = false
  val isEnd: Boolean = true
}