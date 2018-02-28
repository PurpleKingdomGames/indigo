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
    searchGrid.grid.length == searchGrid.validationWidth * searchGrid.validationHeight &&
      searchGrid.grid.exists(_.isStart) && searchGrid.grid.exists(_.isEnd)
  }

  def generate(start: Coords, end: Coords, gridWidth: Int, gridHeight: Int): SearchGrid = {
    SearchGrid(4, 4, Nil)
  }

}

case class Coords(x: Int, y: Int) {
  def toGridPosition(gridWidth: Int): Int =
    Coords.toGridPosition(this, gridWidth)
}

object Coords {

  def toGridPosition(coords: Coords, gridWidth: Int): Int = {
    0
  }

}

sealed trait GridSquare {
  val weight: Int
  val name: String
  val isStart: Boolean
  val isEnd: Boolean
}
case class EmptySquare(weight: Int) extends GridSquare {
  val name: String = "empty"
  val isStart: Boolean = false
  val isEnd: Boolean = false
}
case object StartSquare extends GridSquare {
  val weight: Int = 0
  val name: String = "start"
  val isStart: Boolean = true
  val isEnd: Boolean = false
}
case object EndSquare extends GridSquare {
  val weight: Int = 0
  val name: String = "end"
  val isStart: Boolean = false
  val isEnd: Boolean = true
}