package snake.model

import indigo.*
import snake.scenes.GameView
import snake.init.StartupData

final case class ViewModel(walls: Group)
object ViewModel:
  def initialViewModel(startupData: StartupData, model: GameModel): ViewModel =
    ViewModel(
      walls = Group(
        Batch.fromList(model.gameMap.findWalls).map { wall =>
          startupData.staticAssets.wall
            .moveTo(
              GameView.gridPointToPoint(wall.gridPoint, startupData.viewConfig.gridSize, startupData.viewConfig.gridSquareSize)
            )
        }
      )
    )
