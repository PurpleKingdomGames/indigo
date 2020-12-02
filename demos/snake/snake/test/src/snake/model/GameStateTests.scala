package snake.model

import indigo.Seconds

import snake.model.snakemodel.SnakeDirection

class GameStateTests extends munit.FunSuite {

  test("GameState should do a simple update") {

    val actual   = GameState.Running.start.updateNow(Seconds(0.1), SnakeDirection.Up)
    val expected = GameState.Running(Seconds(0.1), SnakeDirection.Up)

    assertEquals(actual, expected)

  }

}
