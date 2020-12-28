package snake.gamelogic

import indigo._
import snake.init.{GameAssets, Settings, StaticAssets}
import snake.model.arena.GameMap
import snake.model.{GameModel, SnakeViewModel}
import indigoextras.geometry.Vertex
import indigoextras.geometry.BoundingBox

object ViewLogic {

  def gridPointToPoint(gridPoint: Vertex, gridSize: BoundingBox): Point =
    Point((gridPoint.x * Settings.gridSquareSize).toInt, (((gridSize.height - 1) - gridPoint.y) * Settings.gridSquareSize).toInt)

  def update(model: GameModel, snakeViewModel: SnakeViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        gameLayer(
          model,
          snakeViewModel.staticAssets,
          snakeViewModel.walls
        )
      )
    )

  def gameLayer(
      currentState: GameModel,
      staticAssets: StaticAssets,
      walls: Group
  ): List[SceneGraphNode] =
    walls ::
      drawApple(currentState.gameMap, staticAssets) ++
        drawSnake(currentState, staticAssets.snake) ++
      drawScore(currentState.score)

  def drawApple(gameMap: GameMap, staticAssets: StaticAssets): List[Graphic] =
    gameMap.findApples.map(
      a => staticAssets.apple.moveTo(gridPointToPoint(a.gridPoint, gameMap.gridSize))
    )

  def drawSnake(currentState: GameModel, snakeAsset: Graphic): List[Graphic] =
    currentState.snake.givePath.map { pt =>
      snakeAsset.moveTo(gridPointToPoint(pt, currentState.gameMap.gridSize))
    }

  def drawScore(score: Int): List[SceneGraphNode] =
    List(
      Text(
        score.toString,
        (Settings.viewportWidth / Settings.magnificationLevel) - 3,
        (Settings.viewportHeight / Settings.magnificationLevel) - Settings.footerHeight + 21,
        1,
        GameAssets.fontKey
      ).alignRight
    )

}
