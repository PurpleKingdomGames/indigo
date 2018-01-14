package snake

import com.purplekingdomgames.indigo.gameengine._
import snake.MapElement.{Apple, Player1Start}

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      running = true,
      staticAssets = startupData.staticAssets,
      snake = Snake(8, 8).grow.grow,
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

  def updateModel(state: SnakeModel): GameEvent => SnakeModel = {
    case FrameTick =>
      state.snake.update(state.gameMap.gridSize, hitTest(state.snake.givePath)) match {
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

        case (s, _) =>
          state.copy(
            snake = s
          )
      }

    case KeyDown(Keys.LEFT_ARROW) =>
      state.copy(snake = state.snake.turnLeft)

    case KeyDown(Keys.RIGHT_ARROW) =>
      state.copy(snake = state.snake.turnRight)

    case _ =>
      state
  }

}

case class SnakeModel(running: Boolean, staticAssets: StaticAssets, snake: Snake, gameMap: GameMap)
