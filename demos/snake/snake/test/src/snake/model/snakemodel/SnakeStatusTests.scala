package snake.model.snakemodel

class SnakeStatusTests extends munit.FunSuite {

  test("Should know if it's dead or alive") {
    assertEquals(SnakeStatus.Alive.isDead, false)
    assertEquals(SnakeStatus.Dead.isDead, true)
  }

}
