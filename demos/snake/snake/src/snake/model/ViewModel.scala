package snake.model

import indigo._
import snake.scenes.GameView
import snake.init.SnakeStartupData

final case class ViewModel(walls: Group)
object ViewModel {
  def initialViewModel(startupData: SnakeStartupData, model: GameModel): ViewModel =
    ViewModel(
      walls = Group(
        model.gameMap.findWalls.map { wall =>
          startupData.staticAssets.wall
            .moveTo(
              GameView.gridPointToPoint(wall.gridPoint, startupData.viewConfig.gridSize, startupData.viewConfig.gridSquareSize)
            )
        }
      )
    )
}
