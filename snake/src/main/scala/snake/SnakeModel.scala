package snake

import com.purplekingdomgames.indigo.gameengine._
import snake.MapElement.{Apple, Player1Start}

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      running = true,
      staticAssets = startupData.staticAssets,
      player1 = Player(Snake(8, 8).grow.grow, 100, 0),
      gameMap = genLevel(startupData.gridSize)
    )

  private def genLevel(gridSize: GridSize): GameMap =
    GameMap(gridSize)
      .insertElement(Player1Start(gridSize.centre))
      .insertElement(Apple(gridSize.centre + ((3, 2))))


  private val hitTest: List[SnakePoint] => SnakePoint => CollisionCheckOutcome = body => {
//      case pt @ SnakePoint(0, _) =>
//        CollisionCheckOutcome.Crashed(pt)
//
//      case pt @ SnakePoint(_, 0) =>
//        CollisionCheckOutcome.Crashed(pt)
//
//      case pt @ SnakePoint(15, _) =>
//        CollisionCheckOutcome.Crashed(pt)
//
//      case pt @ SnakePoint(_, 15) =>
//        CollisionCheckOutcome.Crashed(pt)
//
//      case pt if pt.x == apple.x && pt.y == apple.y =>
//        CollisionCheckOutcome.PickUp(pt)
//
//      case pt if body.contains(pt) =>
//        CollisionCheckOutcome.Crashed(pt)

      case pt =>
        CollisionCheckOutcome.NoCollision(pt)
    }

  def updateModel(gameTime: GameTime, state: SnakeModel): GameEvent => SnakeModel = {
    case FrameTick =>
      state.copy(
        player1 = state.player1.update(gameTime, state.gameMap.gridSize, hitTest(state.player1.snake.givePath))
      )

    case KeyDown(Keys.LEFT_ARROW) =>
      state.copy(
        player1 = state.player1.turnLeft
      )

    case KeyDown(Keys.RIGHT_ARROW) =>
      state.copy(
        player1 = state.player1.turnRight
      )

    case _ =>
      state
  }

}

case class SnakeModel(running: Boolean, staticAssets: StaticAssets, player1: Player, gameMap: GameMap)

case class Player(snake: Snake, tickDelay: Int, lastUpdated: Double) {

  def update(gameTime: GameTime, gridSize: GridSize, collisionCheck: SnakePoint => CollisionCheckOutcome): Player =
   snake.update(gridSize, collisionCheck) match {
      //        case (s, Crashed(_)) =>
      //          state.copy(
      //            running = false,
      //            snake = s
      //          )
      //
      //        case (s, PickUp(_)) =>
      //          state.copy(
      //            snake = s,
      //            apple = Apple.spawn(state.gridSize)
      //          )

      case (s, _) if gameTime.running >= lastUpdated + tickDelay =>
        this.copy(
          snake = s,
          lastUpdated = gameTime.running
        )

      case (_, _) =>
        this
    }

  def turnLeft: Player =
    this.copy(snake = snake.turnLeft)

  def turnRight: Player =
    this.copy(snake = snake.turnRight)

}
