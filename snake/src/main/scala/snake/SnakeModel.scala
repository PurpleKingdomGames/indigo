package snake

import com.purplekingdomgames.indigo.gameengine._
import snake.CollisionCheckOutcome.{Crashed, PickUp}

import scala.util.Random

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      running = true,
      gridSize = startupData.gridSize,
      staticAssets = startupData.staticAssets,
      snake = Snake(8, 8).grow.grow,
      apple = Apple(2, 13)
    )

  private val hitTest: List[SnakePoint] => Apple => SnakePoint => CollisionCheckOutcome = body => apple => {
      case pt @ SnakePoint(0, _) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt @ SnakePoint(_, 0) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt @ SnakePoint(15, _) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt @ SnakePoint(_, 15) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt if pt.x == apple.x && pt.y == apple.y =>
        CollisionCheckOutcome.PickUp(pt)

      case pt if body.contains(pt) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt =>
        CollisionCheckOutcome.NoCollision(pt)
    }

  def updateModel(state: SnakeModel): GameEvent => SnakeModel = {
    case FrameTick =>
      state.snake.update(state.gridSize, hitTest(state.snake.givePath)(state.apple)) match {
        case (s, Crashed(_)) =>
          state.copy(
            running = false,
            snake = s
          )

        case (s, PickUp(_)) =>
          state.copy(
            snake = s,
            apple = Apple.spawn(state.gridSize)
          )

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

case class SnakeModel(running: Boolean, gridSize: GridSize, staticAssets: StaticAssets, snake: Snake, apple: Apple)

case class Apple(x: Int, y: Int)

object Apple {

  //TODO: ScalaCheck? The properties are that apples should always be 1 to 14
  def spawn(gridSize: GridSize): Apple = {
    def rand(max: Int, border: Int): Int =
      ((max - (border * 2)) * Random.nextFloat()).toInt + border

    Apple(rand(gridSize.columns, 1), rand(gridSize.rows, 1))
  }

}