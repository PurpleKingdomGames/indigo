package snake.init

import indigo._
import indigoextras.geometry.BoundingBox

object SnakeStartupData {

  def initialise(
      viewConfig: ViewConfig
  ): Outcome[Startup[SnakeStartupData]] =
    Outcome(
      Startup.Success(createStartupData(viewConfig))
    )

  def createStartupData(viewConfig: ViewConfig): SnakeStartupData = {
    val blockSize = viewConfig.gridSquareSize

    SnakeStartupData(
      viewConfig = viewConfig,
      staticAssets = StaticAssets(
        apple = Graphic(0, 0, blockSize, blockSize, 2, GameAssets.snakeMaterial)
          .withCrop(blockSize, 0, blockSize, blockSize),
        snake = Graphic(0, 0, blockSize, blockSize, 2, GameAssets.snakeMaterial),
        wall = Graphic(0, 0, blockSize, blockSize, 2, GameAssets.snakeMaterial)
          .withCrop(blockSize * 2, 0, blockSize, blockSize)
      )
    )
  }

}

final case class SnakeStartupData(viewConfig: ViewConfig, staticAssets: StaticAssets)

final case class StaticAssets(apple: Graphic, snake: Graphic, wall: Graphic)

final case class ViewConfig(
    gridSize: BoundingBox,
    gridSquareSize: Int,
    footerHeight: Int,
    magnificationLevel: Int,
    viewport: GameViewport
) {
  val horizontalCenter: Int = (viewport.width / magnificationLevel) / 2
  val verticalMiddle: Int   = (viewport.height / magnificationLevel) / 2
}
object ViewConfig {

  val default: ViewConfig = {
    val gridSquareSize = 12
    val gridSize = BoundingBox(
      x = 0,
      y = 0,
      width = 30,
      height = 20
    )
    val magnificationLevel = 2
    val footerHeight       = 36

    ViewConfig(
      gridSize,
      gridSquareSize,
      footerHeight,
      magnificationLevel,
      GameViewport(
        (gridSquareSize * gridSize.width.toInt) * magnificationLevel,
        ((gridSquareSize * gridSize.height.toInt) * magnificationLevel) + footerHeight
      )
    )
  }

}
