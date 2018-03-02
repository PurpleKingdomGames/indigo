package snake

import scala.annotation.tailrec

/**
  * This is very crude and inefficient, but should be ok for the Snake use case,
  * in that:
  *
  * 1) Snake has small grids, so the amount of the grid to search requires no optimisation.
  * 2) Snake does not do diagonals, so scoring does not expand diagonally either.
  * 3) Snake does not have different types of terrain, so there is no weighting
  */
object Pathfinding {

  //

}

case class SearchGrid(validationWidth: Int, validationHeight: Int, start: Coords, end: Coords, grid: List[GridSquare]) {

  def isValid: Boolean =
    SearchGrid.isValid(this)

  def sampleAt(coords: Coords): List[GridSquare] =
    SearchGrid.sampleAt(this, coords, validationWidth)

  def score: SearchGrid =
    SearchGrid.score(this)

  def locatePath: List[Coords] =
    SearchGrid.locatePath

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
      coords + Coords.relativeUp,
      coords + Coords.relativeLeft,
      coords + Coords.relativeRight,
      coords + Coords.relativeDown
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
          EmptySquare(index, c, None)

      }
    }

    SearchGrid(gridWidth, gridHeight, start, end, grid)
  }

  def scoreGridSquares(searchGrid: SearchGrid): List[GridSquare] = {
    @tailrec
    def rec(target: Coords, unscored: List[GridSquare], scoreValue: Int, lastCoords: List[Coords], scored: List[GridSquare]): List[GridSquare] = {
      (unscored, lastCoords) match {
        case (Nil, _) | (_, Nil) =>
          scored

        case (_, last) if last.exists(_ === target) =>
          scored

        case (remainingSquares, lastScoredLocations) =>

          // Find the squares from the remaining pile that the previous scores squares touched.
          val roughEdges: List[List[GridSquare]] =
            lastScoredLocations.map(c => sampleAt(searchGrid, c, searchGrid.validationWidth))

          // Filter out any squares that aren't in the remainingSquares list
          val edges: List[GridSquare] =
            roughEdges.flatMap(_.filter(c => remainingSquares.contains(c)))

          // Deduplicate and score
          val next: List[GridSquare] =
            edges.foldLeft[List[GridSquare]](Nil) { (l, x) =>
              if(l.exists(p => p.coords === x.coords)) l else l :+ x
            }.map(_.withScore(scoreValue))

          rec(
            target = target,
            unscored = remainingSquares.filter(p => !next.exists(q => q.coords === p.coords)),
            scoreValue = scoreValue + 1,
            lastCoords = next.map(_.coords),
            scored = next ++ scored
          )
      }
    }
    
    val (done, todo) = searchGrid.grid.partition(_.isEnd)

    rec(searchGrid.start, todo, 1, List(searchGrid.end), done).sortBy(_.index)
  }

  def score(searchGrid: SearchGrid): SearchGrid =
    searchGrid.copy(grid = scoreGridSquares(searchGrid))

  def locatePath: List[Coords] =
    List(Coords(1, 1))

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
  val name: String
  val isStart: Boolean
  val isEnd: Boolean
  val score: Option[Int]
  def withScore(score: Int): GridSquare
}
case class EmptySquare(index: Int, coords: Coords, score: Option[Int]) extends GridSquare {
  val name: String = "empty"
  val isStart: Boolean = false
  val isEnd: Boolean = false
  def withScore(score: Int): EmptySquare = this.copy(score = Option(score))
}
case class ImpassableSquare(index: Int, coords: Coords) extends GridSquare {
  val name: String = "impassable"
  val isStart: Boolean = false
  val isEnd: Boolean = false
  val score: Option[Int] = Some(GridSquare.max)
  def withScore(score: Int): ImpassableSquare = this
}
case class StartSquare(index: Int, coords: Coords) extends GridSquare {
  val name: String = "start"
  val isStart: Boolean = true
  val isEnd: Boolean = false
  val score: Option[Int] = Some(0)
  def withScore(score: Int): StartSquare = this
}
case class EndSquare(index: Int, coords: Coords) extends GridSquare {
  val name: String = "end"
  val isStart: Boolean = false
  val isEnd: Boolean = true
  val score: Option[Int] = Some(GridSquare.max)
  def withScore(score: Int): EndSquare = this
}

