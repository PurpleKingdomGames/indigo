package indigo.shared.time

class GameTimeTests extends munit.FunSuite {

  test("GameTime should be able to calculate the frame duration of the game") {
    val gameTime: GameTime = GameTime(Seconds.zero, Seconds.zero, targetFPS = FPS(30))
    assertEquals(gameTime.frameDuration, Millis(33L))
  }

}
