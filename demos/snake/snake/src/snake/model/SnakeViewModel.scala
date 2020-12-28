package snake.model

import indigo._
import snake.scenes.GameView
import snake.init.{SnakeStartupData, StaticAssets}

final case class SnakeViewModel(walls: Group, staticAssets: StaticAssets)
object SnakeViewModel {
  def initialViewModel(startupData: SnakeStartupData, snakeModel: SnakeGameModel): SnakeViewModel =
    SnakeViewModel(
      walls = Group(
        snakeModel.gameModel.gameMap.findWalls.map { wall =>
          startupData.staticAssets.wall
            .moveTo(
              GameView.gridPointToPoint(wall.gridPoint, startupData.viewConfig.gridSize, startupData.viewConfig.gridSquareSize)
            )
        }
      ),
      staticAssets = startupData.staticAssets
    )
}
