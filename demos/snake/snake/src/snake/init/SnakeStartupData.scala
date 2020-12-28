package snake.init

import indigo._
import indigoextras.geometry.BoundingBox

object SnakeStartupData {

  def initialise(
      viewport: GameViewport,
      gridSize: BoundingBox
  ): Outcome[Startup[SnakeStartupData]] =
    Outcome(
      Startup.Success(createStartupData(viewport, gridSize, Settings.gridSquareSize))
    )

  def createStartupData(viewport: GameViewport, gridSize: BoundingBox, blockSize: Int): SnakeStartupData =
    SnakeStartupData(
      viewport = viewport,
      gridSize = gridSize,
      staticAssets = StaticAssets(
        apple = Graphic(0, 0, blockSize, blockSize, 2, GameAssets.snakeMaterial)
          .withCrop(blockSize, 0, blockSize, blockSize),
        snake = Graphic(0, 0, blockSize, blockSize, 2, GameAssets.snakeMaterial),
        wall = Graphic(0, 0, blockSize, blockSize, 2, GameAssets.snakeMaterial)
          .withCrop(blockSize * 2, 0, blockSize, blockSize)
      )
    )

}

case class SnakeStartupData(viewport: GameViewport, gridSize: BoundingBox, staticAssets: StaticAssets)

case class StaticAssets(apple: Graphic, snake: Graphic, wall: Graphic)
