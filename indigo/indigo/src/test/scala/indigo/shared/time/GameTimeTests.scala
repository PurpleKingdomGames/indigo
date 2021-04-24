package indigo.shared.time

class GameTimeTests extends munit.FunSuite {

  test("GameTime should be able to calculate the frame duration of the game") {

    val gameTime: GameTime = GameTime(Seconds.zero, Seconds.zero, targetFPS = GameTime.FPS(30))

    assertEquals(Math.round(gameTime.frameDuration.toDouble), 33L)

  }

}
