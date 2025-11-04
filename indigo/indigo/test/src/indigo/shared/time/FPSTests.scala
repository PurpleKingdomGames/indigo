package indigo.shared.time

class FPSTests extends munit.FunSuite {

  test("FPS should be able to calculate the frame duration of the game (ms)") {
    assertEquals(FPS(30).toMillis, Millis(33L))
  }

  test("FPS should be able to calculate the frame duration of the game (s)") {
    assert(clue(FPS(30).toSeconds) ~== clue(Millis(33L).toSeconds))
  }

}
