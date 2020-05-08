package snake.init

import indigoexts.grids.GridSize

object Settings {

  val gridSize: GridSize = GridSize(
    columns = 30,
    rows = 20,
    gridSquareSize = 12
  )

  val footerHeight: Int = 36

  val magnificationLevel: Int = 2

  val viewportWidth: Int =
    (gridSize.gridSquareSize * gridSize.columns) * magnificationLevel

  val viewportHeight: Int =
    ((gridSize.gridSquareSize * gridSize.rows) * magnificationLevel) + footerHeight

}
