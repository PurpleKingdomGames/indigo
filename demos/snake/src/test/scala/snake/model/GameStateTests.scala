package snake.model

import utest._

import indigo.Millis

import snake.model.snakemodel.SnakeDirection

object GameStateTests extends TestSuite {

  val tests: Tests =
    Tests {
      "GameState" - {

        "should do a simple update" - {

          val actual   = GameState.Running.start.updateNow(Millis(100), SnakeDirection.Up)
          val expected = GameState.Running(Millis(100), SnakeDirection.Up)

          actual ==> expected

        }

      }
    }

}
