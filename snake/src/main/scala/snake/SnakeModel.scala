package snake

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigoat.grid.{GridPoint, GridSize}
import snake.arenas.GameMap
import snake.datatypes.{CollisionCheckOutcome, Snake}
import snake.screens.{GameScreen, GameScreenFunctions, Screen, TitleScreen}

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      currentScreen = GameScreen,
      titleScreenModel = TitleScreenModel(),
      gameScreenModel = GameScreenFunctions.Model.initialModel(startupData)
    )

  def modelUpdate(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = gameEvent =>
    state.currentScreen match {
      case TitleScreen =>
        state

      case GameScreen =>
        state.copy(gameScreenModel = GameScreenFunctions.Model.update(gameTime, state.gameScreenModel)(gameEvent))
    }

}

case class SnakeModel(currentScreen: Screen, titleScreenModel: TitleScreenModel, gameScreenModel: GameScreenModel)

case class TitleScreenModel()

case class GameScreenModel(running: Boolean, staticAssets: StaticAssets, player1: Player, gameMap: GameMap)

case class Player(snake: Snake, tickDelay: Int, lastUpdated: Double) {

  def update(gameTime: GameTime, gridSize: GridSize, collisionCheck: GridPoint => CollisionCheckOutcome): (Player, CollisionCheckOutcome) =
    snake.update(gridSize, collisionCheck) match {
      case (s, outcome) if gameTime.running >= lastUpdated + tickDelay =>
        (this.copy(snake = s, lastUpdated = gameTime.running), outcome)

      case (_, outcome) =>
        (this, outcome)
    }

  def turnLeft: Player =
    this.copy(snake = snake.turnLeft)

  def turnRight: Player =
    this.copy(snake = snake.turnRight)

}
