package snake

import com.purplekingdomgames.indigo.gameengine._
import snake.CollisionCheckOutcome.PickUp

import scala.util.Random

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      startupData.gridSize,
      startupData.staticAssets,
      Snake(8, 8).grow.grow,
      Apple(2, 13)
    )

  private val hitTest: Apple => SnakePoint => CollisionCheckOutcome = apple => {
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

      case pt =>
        CollisionCheckOutcome.NoCollision(pt)
    }

  def updateModel(state: SnakeModel): GameEvent => SnakeModel = {
    case FrameTick =>
      state.snake.update(state.gridSize, hitTest(state.apple)) match {
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

case class SnakeModel(gridSize: GridSize, staticAssets: StaticAssets, snake: Snake, apple: Apple)

case class Apple(x: Int, y: Int)

object Apple {

  //TODO: ScalaCheck? The properties are that apples should always be 1 to 14
  def spawn(gridSize: GridSize): Apple = {
    def rand(max: Int, border: Int): Int =
      ((max - (border * 2)) * Random.nextFloat()).toInt + border

    Apple(rand(gridSize.x, 1), rand(gridSize.y, 1))
  }

}