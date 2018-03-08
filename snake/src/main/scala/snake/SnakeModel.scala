package snake

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle
import com.purplekingdomgames.indigoat.grid.{GridPoint, GridSize}
import com.purplekingdomgames.indigoat.ui.{Button, ButtonAssets, ButtonState}
import com.purplekingdomgames.shared.GameViewport
import snake.arenas.GameMap
import snake.datatypes.{CollisionCheckOutcome, Snake}
import snake.screens._

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      currentScreen = MenuScreen,
      menuScreenModel = MenuScreenModel(
        startupData.viewport,
        Button(
          Rectangle(100, 100, 16, 16),
          ButtonState.Up,
          ButtonAssets(
            up = startupData.staticAssets.gameScreen.player1.alive,
            over = startupData.staticAssets.gameScreen.player2.alive,
            down = startupData.staticAssets.gameScreen.player3.alive
          )
        )
//          .withDownAction((_, btn) => btn.toDownState)
//          .withUpAction((_, btn) => btn.toUpState)
//          .withHoverOverAction((_, btn) => if(btn.state.isDown) btn else btn.toHoverState)
//          .withHoverOutAction((_, btn) => if(btn.state.isDown) btn else btn.toUpState)
      ),
      gameScreenModel = GameScreenFunctions.Model.initialModel(startupData)
    )

  def modelUpdate(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = gameEvent =>
    state.currentScreen match {
      case MenuScreen =>
        MenuScreenFunctions.Model.update(gameTime, state)(gameEvent)

      case GameScreen if state.gameScreenModel.running =>
        state.copy(gameScreenModel = GameScreenFunctions.Model.update(gameTime, state.gameScreenModel)(gameEvent))

      case GameScreen =>
        // TODO: Could do this with an event?
        state.copy(currentScreen = GameOverScreen)

      case GameOverScreen =>
        GameOverScreenFunctions.Model.update(state)(gameEvent)
    }

}

case class SnakeModel(currentScreen: Screen, menuScreenModel: MenuScreenModel, gameScreenModel: GameScreenModel)

case class MenuScreenModel(gameViewport: GameViewport, button: Button[SnakeEvent])

case class GameScreenModel(running: Boolean, gridSize: GridSize, staticAssets: StaticAssets, player1: Player, gameMap: GameMap) {
  def reset: GameScreenModel =
    this.copy(
      running = true,
      player1 = Player(
        snake = Snake(
          this.gridSize.centre.x,
          this.gridSize.centre.y
        ).grow.grow,
        tickDelay = 100,
        lastUpdated = 0
      )
    )
}

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
