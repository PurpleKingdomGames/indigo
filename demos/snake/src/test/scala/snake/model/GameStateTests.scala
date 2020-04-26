package snake.model

import utest._

import indigo.Seconds

import snake.model.snakemodel.SnakeDirection

object GameStateTests extends TestSuite {

  val tests: Tests =
    Tests {
      "GameState" - {

        "should do a simple update" - {

          val actual   = GameState.Running.start.updateNow(Seconds(0.1), SnakeDirection.Up)
          val expected = GameState.Running(Seconds(0.1), SnakeDirection.Up)

          actual ==> expected

        }

      }
    }

}
