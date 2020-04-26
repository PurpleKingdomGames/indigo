package snake.gamelogic

import indigo.{GameTime, Seconds}
import indigo.shared.events.FrameTick
import indigoexts.grid.{GridPoint, GridSize}

import snake.model.{ControlScheme, GameModel, GameState}
import snake.model.snakemodel.Snake
import snake.model.arena.Arena

import utest._

object ModelLogicTests extends TestSuite {

  val defaultDelay: Seconds = Seconds(0.1)

  val model: GameModel =
    ModelLogic.initialModel(
      GridSize.apply(5, 5, 10),
      ControlScheme.directedKeys
    )

  val tests: Tests =
    Tests {
      "basic model updates" - {

        "should advance the game on frame tick" - {
          val actual = ModelLogic.update(GameTime.is(Seconds(0.15)), model)(FrameTick).state
          val expected = model.copy(
            snake = model.snake.copy(start = GridPoint(2, 2)),
            gameState = model.gameState.updateNow(Seconds(0.15), model.gameState.lastSnakeDirection)
          )

          actual.snake ==> expected.snake
        }

      }

    }

}
