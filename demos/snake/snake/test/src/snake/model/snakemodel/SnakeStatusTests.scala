package snake.model.snakemodel

import utest._

object SnakeStatusTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Should know if it's dead or alive" - {
        SnakeStatus.Alive.isDead ==> false
        SnakeStatus.Dead.isDead ==> true
      }

    }
}
