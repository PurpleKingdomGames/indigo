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

  def sampleAt(coords: Coords): List[GridSquare] =
    SearchGrid.sampleAt(this, coords, validationWidth)

}

object SearchGrid {

  def isValid(searchGrid: SearchGrid): Boolean = {
    searchGrid.grid.lengthCompare(searchGrid.validationWidth * searchGrid.validationHeight) == 0 &&
      searchGrid.grid.exists(_.isStart) && searchGrid.grid.exists(_.isEnd)
  }

  def coordsWithinGrid(searchGrid: SearchGrid, coords: Coords): Boolean =
    coords.x >= 0 && coords.y >= 0 && coords.x < searchGrid.validationWidth && coords.y < searchGrid.validationHeight

  def sampleAt(searchGrid: SearchGrid, coords: Coords, gridWidth: Int): List[GridSquare] = {
    List(
      coords + Coords.relativeUpLeft,
      coords + Coords.relativeUp,
      coords + Coords.relativeUpRight,
      coords + Coords.relativeLeft,
      coords + Coords.relativeRight,
      coords + Coords.relativeDownLeft,
      coords + Coords.relativeDown,
      coords + Coords.relativeDownRight
    ).filter(c => coordsWithinGrid(searchGrid, c)).map(c => searchGrid.grid(c.toGridPosition(gridWidth)))
  }

  def generate(start: Coords, end: Coords, impassable: List[Coords], gridWidth: Int, gridHeight: Int): SearchGrid = {
    val grid: List[GridSquare] = (0 until (gridWidth * gridHeight)).toList.map { index =>
      Coords.fromIndex(index, gridWidth) match {
        case c: Coords if c === start =>
          StartSquare(index, start)

        case c: Coords if c === end =>
          EndSquare(index, end)

        case c: Coords if impassable.contains(c) =>
          ImpassableSquare(index, c)

        case c: Coords =>
          EmptySquare(index, c, 1, None)

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

  def +(other: Coords): Coords =
    Coords.add(this, other)

}

object Coords {

  val relativeUpLeft: Coords = Coords(-1, -1)
  val relativeUp: Coords = Coords(0, -1)
  val relativeUpRight: Coords = Coords(1, -1)
  val relativeLeft: Coords = Coords(-1, 0)
  val relativeRight: Coords = Coords(1, 0)
  val relativeDownLeft: Coords = Coords(-1, 1)
  val relativeDown: Coords = Coords(0, 1)
  val relativeDownRight: Coords = Coords(1, 1)

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

  def add(a: Coords, b: Coords): Coords =
    Coords(a.x + b.x, a.y + b.y)

}

object GridSquare {
  val max: Int = 99999999
}

sealed trait GridSquare {
  val index: Int
  val coords: Coords
  val weight: Int
  val name: String
  val isStart: Boolean
  val isEnd: Boolean
  val score: Option[Int]
}
case class EmptySquare(index: Int, coords: Coords, weight: Int, score: Option[Int]) extends GridSquare {
  val name: String = "empty"
  val isStart: Boolean = false
  val isEnd: Boolean = false
}
case class ImpassableSquare(index: Int, coords: Coords) extends GridSquare {
  val name: String = "impassable"
  val isStart: Boolean = false
  val isEnd: Boolean = false
  val score: Option[Int] = Some(GridSquare.max)
  val weight: Int = GridSquare.max
}
case class StartSquare(index: Int, coords: Coords) extends GridSquare {
  val weight: Int = GridSquare.max
  val name: String = "start"
  val isStart: Boolean = true
  val isEnd: Boolean = false
  val score: Option[Int] = Some(0)
}
case class EndSquare(index: Int, coords: Coords) extends GridSquare {
  val weight: Int = GridSquare.max
  val name: String = "end"
  val isStart: Boolean = false
  val isEnd: Boolean = true
  val score: Option[Int] = Some(GridSquare.max)
}

