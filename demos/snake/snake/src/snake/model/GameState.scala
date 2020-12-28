package snake.model

import snake.model.snakemodel.SnakeDirection
import indigo.Seconds

sealed trait GameState {
  val hasCrashed: Boolean
  val lastSnakeDirection: SnakeDirection
  def updateNow(time: Seconds, currentDirection: SnakeDirection): GameState
}
object GameState {
  final case class Crashed(crashedAt: Seconds, snakeLengthOnCrash: Int, lastUpdated: Seconds, lastSnakeDirection: SnakeDirection)
      extends GameState {
    val hasCrashed: Boolean = true

    def updateNow(time: Seconds, currentDirection: SnakeDirection): GameState.Crashed =
      this.copy(lastUpdated = time, lastSnakeDirection = currentDirection)
  }
  final case class Running(lastUpdated: Seconds, lastSnakeDirection: SnakeDirection) extends GameState {
    val hasCrashed: Boolean = false

    def updateNow(time: Seconds, currentDirection: SnakeDirection): GameState.Running =
      this.copy(lastUpdated = time, lastSnakeDirection = currentDirection)

    def crash(crashedAt: Seconds, snakeLengthOnCrash: Int): GameState.Crashed =
      GameState.Crashed(crashedAt, snakeLengthOnCrash: Int, lastUpdated, lastSnakeDirection)
  }
  object Running {
    val start: Running = GameState.Running(Seconds.zero, SnakeDirection.Up)
  }
}
