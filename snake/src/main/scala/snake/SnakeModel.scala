package snake

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.constants.{KeyCode, Keys}
import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, KeyboardEvent}
import com.purplekingdomgames.indigoexts.grid.{GridPoint, GridSize}
import com.purplekingdomgames.indigoexts.ui.Button
import com.purplekingdomgames.shared.GameViewport
import snake.arenas.GameMap
import snake.datatypes.{CollisionCheckOutcome, Snake}
import snake.screens._

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      startupData = startupData,
      currentScreen = MenuScreen,
      menuScreenModel = MenuScreenFunctions.Model.initialModel(startupData),
      gameScreenModel = GameScreenFunctions.Model.initialModel(startupData)
    )

  def modelUpdate(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = gameEvent =>
    state.currentScreen match {
      case MenuScreen =>
        MenuScreenFunctions.Model.update(state)(gameEvent)

      case GameScreen if state.gameScreenModel.running =>
        state.copy(gameScreenModel = GameScreenFunctions.Model.update(gameTime, state.gameScreenModel)(gameEvent))

      case GameScreen =>
        state.copy(currentScreen = GameOverScreen)

      case GameOverScreen =>
        GameOverScreenFunctions.Model.update(state)(gameEvent)
    }

}

case class SnakeModel(startupData: SnakeStartupData, currentScreen: Screen, menuScreenModel: MenuScreenModel, gameScreenModel: GameScreenModel)

case class MenuScreenModel(gameViewport: GameViewport, menuItems: MenuZipper)

case class MenuZipper(previous: List[MenuItem], current: MenuItem, next: List[MenuItem]) {

  val length: Int = previous.length + 1 + next.length
  val positionOfCurrent: Int = previous.length

  def forward: MenuZipper =
    next match {
      case Nil =>
        this

      case x :: xs =>
        MenuZipper(current :: previous, x, xs)
    }

  def back: MenuZipper =
    previous match {
      case Nil =>
        this

      case x :: xs =>
        MenuZipper(xs, x, current :: next)
    }

}
case class MenuItem(text: String, button: Button, goToScreen: Screen)

case class GameScreenModel(running: Boolean, gridSize: GridSize, staticAssets: StaticAssets, player1: Player, gameMap: GameMap)

case class Player(snake: Snake, tickDelay: Int, lastUpdated: Double, playerType: PlayerType, controlScheme: ControlScheme) {

  def update(gameTime: GameTime, gridSize: GridSize, collisionCheck: GridPoint => CollisionCheckOutcome): (Player, CollisionCheckOutcome) =
    snake.update(gridSize, collisionCheck) match {
      case (s, outcome) if gameTime.running >= lastUpdated + tickDelay =>
        (this.copy(snake = s, lastUpdated = gameTime.running), outcome)

      case (_, outcome) =>
        (this, outcome)
    }

  def goUp: Player =
    this.copy(snake = snake.goUp)

  def goRight: Player =
    this.copy(snake = snake.goRight)

  def goDown: Player =
    this.copy(snake = snake.goDown)

  def goLeft: Player =
    this.copy(snake = snake.goLeft)

  def turnLeft: Player =
    this.copy(snake = snake.turnLeft)

  def turnRight: Player =
    this.copy(snake = snake.turnRight)

}

sealed trait PlayerType
case object Human extends PlayerType
case object Computer extends PlayerType

sealed trait ControlScheme {
  def instructPlayer(keyboardEvent: KeyboardEvent, player: Player): Player =
    (this, keyboardEvent) match {
      case (Turning(left, _), KeyboardEvent.KeyPress(code)) if code === left =>
        player.turnLeft

      case (Turning(_, right), KeyboardEvent.KeyPress(code)) if code === right =>
        player.turnRight

      case (Directed(up, _, _, _), KeyboardEvent.KeyPress(code)) if code === up =>
        player.goUp

      case (Directed(_, down, _, _), KeyboardEvent.KeyPress(code)) if code === down =>
        player.goDown

      case (Directed(_, _, left, _), KeyboardEvent.KeyPress(code)) if code === left =>
        player.goLeft

      case (Directed(_, _, _, right), KeyboardEvent.KeyPress(code)) if code === right =>
        player.goRight

      case _ =>
        player
    }
}
case class Turning(left: KeyCode, right: KeyCode) extends ControlScheme
case class Directed(up: KeyCode, down: KeyCode, left: KeyCode, right: KeyCode) extends ControlScheme

object ControlScheme {
  val turning: Turning = Turning(Keys.LEFT_ARROW, Keys.RIGHT_ARROW)
  val directed: Directed = Directed(Keys.UP_ARROW, Keys.DOWN_ARROW, Keys.LEFT_ARROW, Keys.RIGHT_ARROW)
}