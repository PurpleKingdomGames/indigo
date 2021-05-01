package indigoextras.pathfinding

import indigoextras.pathfinding.GridSquare.{EmptySquare, EndSquare, ImpassableSquare, StartSquare}

import scala.annotation.tailrec
import indigo.shared.dice.Dice

final case class SearchGrid(validationWidth: Int, validationHeight: Int, start: Coords, end: Coords, grid: List[GridSquare]) derives CanEqual {

  def isValid: Boolean =
    SearchGrid.isValid(this)

  def locatePath(dice: Dice): List[Coords] =
    SearchGrid.locatePath(dice, SearchGrid.score(this))

}

object SearchGrid {

  def isValid(searchGrid: SearchGrid): Boolean =
    searchGrid.grid.lengthCompare(searchGrid.validationWidth * searchGrid.validationHeight) == 0 &&
      searchGrid.grid.exists(_.isStart) && searchGrid.grid.exists(_.isEnd)

  def coordsWithinGrid(searchGrid: SearchGrid, coords: Coords): Boolean =
    coords.x >= 0 && coords.y >= 0 && coords.x < searchGrid.validationWidth && coords.y < searchGrid.validationHeight

  def sampleAt(searchGrid: SearchGrid, coords: Coords, gridWidth: Int): List[GridSquare] =
    List(
      coords + Coords.relativeUp,
      coords + Coords.relativeLeft,
      coords + Coords.relativeRight,
      coords + Coords.relativeDown
    ).filter(c => coordsWithinGrid(searchGrid, c)).map(c => searchGrid.grid(c.toGridPosition(gridWidth)))

  def generate(start: Coords, end: Coords, impassable: List[Coords], gridWidth: Int, gridHeight: Int): SearchGrid = {
    val grid: List[GridSquare] = (0 until (gridWidth * gridHeight)).toList.map { index =>
      Coords.fromIndex(index, gridWidth) match {
        case c: Coords if c == start =>
          StartSquare(index, start)

        case c: Coords if c == end =>
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
    def rec(target: Coords, unscored: List[GridSquare], scoreValue: Int, lastCoords: List[Coords], scored: List[GridSquare]): List[GridSquare] =
      (unscored, lastCoords) match {
        case (Nil, _) | (_, Nil) =>
          scored ++ unscored

        case (_, last) if last.exists(_ == target) =>
          scored ++ unscored

        case (remainingSquares, lastScoredLocations) =>
          // Find the squares from the remaining pile that the previous scores squares touched.
          val roughEdges: List[List[GridSquare]] =
            lastScoredLocations.map(c => sampleAt(searchGrid, c, searchGrid.validationWidth))

          // Filter out any squares that aren't in the remainingSquares list
          val edges: List[GridSquare] =
            roughEdges.flatMap(_.filter(c => remainingSquares.contains(c)))

          // Deduplicate and score
          val next: List[GridSquare] =
            edges
              .foldLeft[List[GridSquare]](Nil) { (l, x) =>
                if (l.exists(p => p.coords == x.coords)) l else l ++ List(x)
              }
              .map(_.withScore(scoreValue))

          rec(
            target = target,
            unscored = remainingSquares.filter(p => !next.exists(q => q.coords == p.coords)),
            scoreValue = scoreValue + 1,
            lastCoords = next.map(_.coords),
            scored = next ++ scored
          )
      }

    val (done, todo) = searchGrid.grid.partition(_.isEnd)

    rec(searchGrid.start, todo, 1, List(searchGrid.end), done).sortBy(_.index)
  }

  def score(searchGrid: SearchGrid): SearchGrid =
    searchGrid.copy(grid = scoreGridSquares(searchGrid))

  def locatePath(dice: Dice, searchGrid: SearchGrid): List[Coords] = {
    @tailrec
    def rec(currentPosition: Coords, currentScore: Int, target: Coords, grid: SearchGrid, width: Int, acc: List[Coords]): List[Coords] =
      if (currentPosition == target) acc
      else
        sampleAt(grid, currentPosition, width).filter(c => c.score.getOrElse(GridSquare.max) < currentScore) match {
          case Nil =>
            acc

          case next :: Nil =>
            rec(next.coords, next.score.getOrElse(GridSquare.max), target, grid, width, acc ++ List(next.coords))

          case xs =>
            val next = xs(dice.rollFromZero(xs.length))
            rec(next.coords, next.score.getOrElse(GridSquare.max), target, grid, width, acc ++ List(next.coords))
        }

    rec(searchGrid.start, GridSquare.max, searchGrid.end, searchGrid, searchGrid.validationWidth, List(searchGrid.start))
  }

}
