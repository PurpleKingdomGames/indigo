package snake

import com.purplekingdomgames.indigo.gameengine._

object SnakeModel {

  def initialModel(startupData: SnakeStartupData): SnakeModel =
    SnakeModel(
      startupData.gridSize,
      startupData.staticAssets,
      Snake(8, 8)
    )

  private val hitTest: SnakePoint => CollisionCheckOutcome = {
      case pt @ SnakePoint(0, _) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt @ SnakePoint(_, 0) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt @ SnakePoint(15, _) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt @ SnakePoint(_, 15) =>
        CollisionCheckOutcome.Crashed(pt)

      case pt @ SnakePoint(32, 32) =>
        CollisionCheckOutcome.PickUp(pt)

      case pt =>
        CollisionCheckOutcome.NoCollision(pt)
    }

  def updateModel(state: SnakeModel): GameEvent => SnakeModel = {
    case FrameTick =>
      state.copy(
        snake = state.snake.update(state.gridSize, hitTest)
      )

//    case KeyDown(Keys.LEFT_ARROW) =>
//      state.copy(dude = state.dude.walkLeft)
//
//    case KeyDown(Keys.RIGHT_ARROW) =>
//      state.copy(dude = state.dude.walkRight)
//
//    case KeyDown(Keys.UP_ARROW) =>
//      state.copy(dude = state.dude.walkUp)
//
//    case KeyDown(Keys.DOWN_ARROW) =>
//      state.copy(dude = state.dude.walkDown)
//
//    case KeyUp(_) =>
//      state.copy(dude = state.dude.idle)

    case _ =>
      state
  }

}

case class SnakeModel(gridSize: GridSize, staticAssets: StaticAssets, snake: Snake)
