package snake.init

import indigoextras.geometry.BoundingBox

object Settings {

  val gridSize: BoundingBox = BoundingBox(
    x = 0,
    y = 0,
    width = 30,
    height = 20
  )

  val gridSquareSize: Int = 12

  val footerHeight: Int = 36

  val magnificationLevel: Int = 2

  val viewportWidth: Int =
    (gridSquareSize * gridSize.width.toInt) * magnificationLevel

  val viewportHeight: Int =
    ((gridSquareSize * gridSize.height.toInt) * magnificationLevel) + footerHeight

}
