package indigo.shared.time

class GameTimeTests extends munit.FunSuite {

  import GameTime._
  import indigo.shared.EqualTo._

  test("GameTime should be able to calculate the frame duration of the game") {

    val gameTime: GameTime = GameTime(Seconds.zero, Seconds.zero, targetFPS = FPS(30))

    assertEquals(Math.round(gameTime.frameDuration.value), 33)

  }

}
