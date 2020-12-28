package snake.model

import indigo._
import snake.model.arena.GameMap
import snake.model.snakemodel.{CollisionCheckOutcome, Snake}
import indigoextras.geometry.Vertex
import indigoextras.geometry.BoundingBox

final case class GameModel(
    gridSize: BoundingBox,
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
      gridSize: BoundingBox,
      collisionCheck: Vertex => CollisionCheckOutcome
  ): (GameModel, CollisionCheckOutcome) =
    snake.update(gridSize, collisionCheck) match {
      case (s, outcome) =>
        (this.copy(snake = s, gameState = gameState.updateNow(gameTime.running, snake.direction)), outcome)
    }
}
