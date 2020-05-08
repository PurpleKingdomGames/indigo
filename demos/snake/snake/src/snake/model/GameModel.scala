package snake.model

import indigo.{GameTime, Seconds}
import indigoexts.grids.{GridPoint, GridSize}
import snake.model.arena.GameMap
import snake.model.snakemodel.{CollisionCheckOutcome, Snake}

case class GameModel(
    gridSize: GridSize,
    snake: Snake,
    gameState: GameState,
    gameMap: GameMap,
    score: Int,
    tickDelay: Seconds,
    controlScheme: ControlScheme,
    lastUpdated: Seconds
) {

  def resetLastUpdated(time: Seconds): GameModel =
    this.copy(lastUpdated = time)

  def update(
      gameTime: GameTime,
      gridSize: GridSize,
      collisionCheck: GridPoint => CollisionCheckOutcome
  ): (GameModel, CollisionCheckOutcome) =
    snake.update(gridSize, collisionCheck) match {
      case (s, outcome) =>
        (this.copy(snake = s, gameState = gameState.updateNow(gameTime.running, snake.direction)), outcome)
    }
}
