package indigo.shared.time

class GameTimeTests extends munit.FunSuite {

  test("GameTime should be able to calculate the frame duration of the game (set)") {
    val gameTime: GameTime = GameTime(Seconds.zero, Seconds.zero, targetFPS = Option(FPS(30)))
    assertEquals(gameTime.frameDuration.get, Millis(33L))
  }

  test("GameTime should be able to calculate the frame duration of the game (unset)") {
    val gameTime: GameTime = GameTime(Seconds.zero, Seconds.zero, targetFPS = None)
    assertEquals(gameTime.frameDuration, None)
  }

}
