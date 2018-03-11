package snake

import com.purplekingdomgames.indigo.gameengine._
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
        menuItems =
          MenuZipper(
            previous = Nil,
            current = MenuItem("demo mode", makeButton(startupData, MenuScreen), MenuScreen),
            next =
              List(
                MenuItem("1up", makeButton(startupData, GameScreen), GameScreen),
                MenuItem("1up vs cpu", makeButton(startupData, MenuScreen), MenuScreen),
                MenuItem("2up local", makeButton(startupData, MenuScreen), MenuScreen),
                MenuItem("2up network", makeButton(startupData, MenuScreen), MenuScreen)
              )
          )

      ),
      gameScreenModel = GameScreenFunctions.Model.initialModel(startupData)
    )

  private def makeButton(startupData: SnakeStartupData, screen: Screen): Button =
    Button(
      ButtonState.Up,
      ButtonAssets(
        up = startupData.staticAssets.gameScreen.player1.alive,
        over = startupData.staticAssets.gameScreen.player2.alive,
        down = startupData.staticAssets.gameScreen.player3.alive
      )
    ).withUpAction { () =>
      Option(ChangeScreenTo(screen))
    }

  def modelUpdate(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = gameEvent =>
    state.currentScreen match {
      case MenuScreen =>
        MenuScreenFunctions.Model.update(state)(gameEvent)

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
