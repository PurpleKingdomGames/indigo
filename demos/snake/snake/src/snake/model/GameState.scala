package snake.model

import snake.model.snakemodel.SnakeDirection
import indigo.Seconds

enum GameState(val hasCrashed: Boolean, val lastSnakeDirection: SnakeDirection):
  case Crashed(
      crashedAt: Seconds,
      snakeLengthOnCrash: Int,
      lastUpdated: Seconds,
      lastDirection: SnakeDirection
  ) extends GameState(true, lastDirection)

  case Running(
      lastUpdated: Seconds,
      lastDirection: SnakeDirection
  ) extends GameState(false, lastDirection)

object GameState:

  val start: GameState.Running =
    GameState.Running(Seconds.zero, SnakeDirection.Up)

  extension (gs: GameState)
    def updateNow(time: Seconds, currentDirection: SnakeDirection): GameState =
      gs match
        case Crashed(crashedAt, snakeLengthOnCrash, lastUpdated, lastDirection) =>
          Crashed(crashedAt, snakeLengthOnCrash, time, currentDirection)

        case Running(lastUpdated, lastDirection) =>
          Running(time, currentDirection)

    def crash(crashedAt: Seconds, snakeLengthOnCrash: Int): GameState.Crashed =
      gs match
        case c @ Crashed(_, _, _, _) => c
        case Running(lastUpdated, lastDirection) =>
          GameState.Crashed(crashedAt, snakeLengthOnCrash: Int, lastUpdated, gs.lastSnakeDirection)
