package snake.gamelogic

import indigo.{GameTime, Seconds}
import indigo.shared.events.FrameTick

import snake.model.{ControlScheme, GameModel, GameState}
import snake.model.snakemodel.Snake
import snake.model.arena.Arena

import indigo.shared.dice.Dice
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

class ModelLogicTests extends munit.FunSuite {

  val defaultDelay: Seconds = Seconds(0.1)

  val model: GameModel =
    ModelLogic.initialModel(
      BoundingBox(0, 0, 5, 5), // grid square size 10
      ControlScheme.directedKeys
    )

  test("basic model updates should advance the game on frame tick") {
    val actual = ModelLogic.update(GameTime.is(Seconds(0.15)), Dice.loaded(1), model)(FrameTick).unsafeGet
    val expected = model.copy(
      snake = model.snake.copy(start = Vertex(2, 2)),
      gameState = model.gameState.updateNow(Seconds(0.15), model.gameState.lastSnakeDirection)
    )

    assertEquals(actual.snake, expected.snake)
  }

}
