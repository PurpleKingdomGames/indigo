package snake.model

import indigo.Millis

import snake.model.snakemodel.SnakeDirection

sealed trait GameState {
  val hasCrashed: Boolean
  val lastSnakeDirection: SnakeDirection
  def updateNow(time: Millis, currentDirection: SnakeDirection): GameState
}
object GameState {
  case class Crashed(crashedAt: Millis, snakeLengthOnCrash: Int, lastUpdated: Millis, lastSnakeDirection: SnakeDirection)
      extends GameState {
    val hasCrashed: Boolean = true

    def updateNow(time: Millis, currentDirection: SnakeDirection): GameState.Crashed =
      this.copy(lastUpdated = time, lastSnakeDirection = currentDirection)
  }
  case class Running(lastUpdated: Millis, lastSnakeDirection: SnakeDirection) extends GameState {
    val hasCrashed: Boolean = false

    def updateNow(time: Millis, currentDirection: SnakeDirection): GameState.Running =
      this.copy(lastUpdated = time, lastSnakeDirection = currentDirection)

    def crash(crashedAt: Millis, snakeLengthOnCrash: Int): GameState.Crashed =
      GameState.Crashed(crashedAt, snakeLengthOnCrash: Int, lastUpdated, lastSnakeDirection)
  }
  object Running {
    val start: Running = GameState.Running(Millis.zero, SnakeDirection.Up)
  }
}
