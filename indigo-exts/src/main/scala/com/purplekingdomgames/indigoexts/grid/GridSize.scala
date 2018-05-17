package com.purplekingdomgames.indigoexts.grid

case class GridSize(columns: Int, rows: Int, gridSquareSize: Int) {
  val width: Int  = columns
  val height: Int = rows

  def asPowerOf2: Int =
    GridSize.asPowerOf2(this)

  val centre: GridPoint =
    GridPoint(columns / 2, rows / 2)

  val topLeft: GridPoint =
    GridPoint(0, 0)

  val topRight: GridPoint =
    GridPoint(width - 1, 0)

  val bottomLeft: GridPoint =
    GridPoint(0, height - 1)

  val bottomRight: GridPoint =
    GridPoint(width - 1, height - 1)
}

object GridSize {
  def asPowerOf2(gridSize: GridSize): Int = {
    def rec(maxLength: Int, size: Int): Int =
      if (size >= maxLength) size else rec(maxLength, size * 2)

    rec(if (gridSize.width >= gridSize.height) gridSize.width else gridSize.height, 2)
  }
}
