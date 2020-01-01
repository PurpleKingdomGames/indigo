package snake.model

import indigo._
import snake.gamelogic.ViewLogic
import snake.init.{SnakeStartupData, StaticAssets}

final case class SnakeViewModel(walls: Group, staticAssets: StaticAssets)
object SnakeViewModel {
  def initialViewModel(startupData: SnakeStartupData, snakeModel: SnakeGameModel): SnakeViewModel =
    SnakeViewModel(
      walls = Group(
        snakeModel.gameModel.gameMap.findWalls.map { wall =>
          startupData.staticAssets.wall
            .moveTo(ViewLogic.gridPointToPoint(wall.gridPoint, startupData.gridSize))
        }
      ),
      staticAssets = startupData.staticAssets
    )
}
