package snake.gamelogic

import indigo.{GameTime, Millis}
import indigo.shared.events.FrameTick
import indigoexts.grid.{GridPoint, GridSize}

import snake.model.{ControlScheme, GameModel, GameState}
import snake.model.snakemodel.Snake
import snake.model.arena.Arena

import utest._

object ModelLogicTests extends TestSuite {

  val defaultDelay: Millis = Millis(100)
  val maxTime: Int         = 1000
  val lowerBound: Int      = 10

  val model: GameModel =
    ModelLogic.initialModel(
      GridSize.apply(5, 5, 10),
      ControlScheme.directedKeys
    )

  val tests: Tests =
    Tests {
      "basic model updates" - {

        "should advance the game on frame tick" - {
          val actual = ModelLogic.update(GameTime.is(Millis(150)), model)(FrameTick).state
          val expected = model.copy(
            snake = model.snake.copy(start = GridPoint(2, 2)),
            gameState = model.gameState.updateNow(Millis(150), model.gameState.lastSnakeDirection)
          )

          actual.snake ==> expected.snake
        }

      }

    }

}
