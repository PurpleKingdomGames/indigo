package indigoextras.pathfinding

import indigo.shared.collections.Batch
import indigo.shared.dice.Dice
import indigoextras.pathfinding.GridSquare.EmptySquare
import indigoextras.pathfinding.GridSquare.EndSquare
import indigoextras.pathfinding.GridSquare.ImpassableSquare
import indigoextras.pathfinding.GridSquare.StartSquare

import scala.annotation.tailrec

final case class SearchGrid(
    validationWidth: Int,
    validationHeight: Int,
    start: Coords,
    end: Coords,
    grid: Batch[GridSquare]
) derives CanEqual {

  def isValid: Boolean =
    SearchGrid.isValid(this)

  def locatePath(dice: Dice): Batch[Coords] =
    SearchGrid.locatePath(dice, SearchGrid.score(this))

}

object SearchGrid {

  def isValid(searchGrid: SearchGrid): Boolean =
    searchGrid.grid.lengthCompare(searchGrid.validationWidth * searchGrid.validationHeight) == 0 &&
      searchGrid.grid.exists(_.isStart) && searchGrid.grid.exists(_.isEnd)

  def coordsWithinGrid(searchGrid: SearchGrid, coords: Coords): Boolean =
    coords.x >= 0 && coords.y >= 0 && coords.x < searchGrid.validationWidth && coords.y < searchGrid.validationHeight

  def sampleAt(searchGrid: SearchGrid, coords: Coords, gridWidth: Int): Batch[GridSquare] =
    Batch(
      coords + Coords.relativeUp,
      coords + Coords.relativeLeft,
      coords + Coords.relativeRight,
      coords + Coords.relativeDown
    ).filter(c => coordsWithinGrid(searchGrid, c)).map(c => searchGrid.grid(c.toGridPosition(gridWidth)))

  def generate(start: Coords, end: Coords, impassable: Batch[Coords], gridWidth: Int, gridHeight: Int): SearchGrid = {
    val grid: Batch[GridSquare] = Batch.fromRange(0 until (gridWidth * gridHeight)).map { index =>
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

  def scoreGridSquares(searchGrid: SearchGrid): Batch[GridSquare] = {
    @tailrec
    def rec(
        target: Coords,
        unscored: Batch[GridSquare],
        scoreValue: Int,
        lastCoords: Batch[Coords],
        scored: Batch[GridSquare]
    ): Batch[GridSquare] =
      (unscored, lastCoords) match {
        case (Batch.Empty, _) | (_, Batch.Empty) =>
          scored ++ unscored

        case (_, last) if last.exists(_ == target) =>
          scored ++ unscored

        case (remainingSquares, lastScoredLocations) =>
          // Find the squares from the remaining pile that the previous scores squares touched.
          val roughEdges: Batch[Batch[GridSquare]] =
            lastScoredLocations.map(c => sampleAt(searchGrid, c, searchGrid.validationWidth))

          // Filter out any squares that aren't in the remainingSquares list
          val edges: Batch[GridSquare] =
            roughEdges.flatMap(_.filter(c => remainingSquares.contains(c)))

          // Deduplicate and score
          val next: Batch[GridSquare] =
            edges
              .foldLeft[Batch[GridSquare]](Batch.Empty) { (l, x) =>
                if (l.exists(p => p.coords == x.coords)) l else l ++ Batch(x)
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

    rec(searchGrid.start, todo, 1, Batch(searchGrid.end), done).sortBy(_.index)
  }

  def score(searchGrid: SearchGrid): SearchGrid =
    searchGrid.copy(grid = scoreGridSquares(searchGrid))

  def locatePath(dice: Dice, searchGrid: SearchGrid): Batch[Coords] = {
    @tailrec
    def rec(
        currentPosition: Coords,
        currentScore: Int,
        target: Coords,
        grid: SearchGrid,
        width: Int,
        acc: Batch[Coords]
    ): Batch[Coords] =
      import Batch.Unapply.*
      if (currentPosition == target) acc
      else
        sampleAt(grid, currentPosition, width).filter(c => c.score.getOrElse(GridSquare.max) < currentScore) match {
          case Batch.Empty =>
            acc

          case next :: Batch.Empty =>
            rec(next.coords, next.score.getOrElse(GridSquare.max), target, grid, width, acc ++ Batch(next.coords))

          case xs =>
            val next = xs(dice.rollFromZero(xs.length))
            rec(next.coords, next.score.getOrElse(GridSquare.max), target, grid, width, acc ++ Batch(next.coords))
        }

    rec(
      searchGrid.start,
      GridSquare.max,
      searchGrid.end,
      searchGrid,
      searchGrid.validationWidth,
      Batch(searchGrid.start)
    )
  }

}
