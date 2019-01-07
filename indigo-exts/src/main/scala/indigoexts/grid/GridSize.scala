package indigoexts.grid

import indigo.PowerOfTwo

final case class GridSize(columns: Int, rows: Int, gridSquareSize: Int) {
  val width: Int  = columns
  val height: Int = rows

  def asPowerOf2: PowerOfTwo =
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
  def asPowerOf2(gridSize: GridSize): PowerOfTwo = {
    def rec(maxLength: Int, size: PowerOfTwo): PowerOfTwo =
      if (size >= maxLength) size
      else if (size === PowerOfTwo.Max) size
      else rec(maxLength, size.doubled)

    rec(if (gridSize.width >= gridSize.height) gridSize.width else gridSize.height, PowerOfTwo._2)
  }
}
