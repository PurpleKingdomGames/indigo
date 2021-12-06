package indigo.shared.time

class MillisTests extends munit.FunSuite {

  test("Should be able to convert Millis to Seconds") {
    assertEquals(Millis(1000).toSeconds, Seconds(1))
    assertEquals(Millis(1500).toSeconds, Seconds(1.5))
    assertEquals(Millis(10001).toSeconds, Seconds(10.001))
  }

  test("Operations.modulo") {

    assertEquals(Millis(1) % Millis(2), Millis(1))
    assertEquals(Millis(2) % Millis(2), Millis(0))

  }

  test("abs") {
    assertEquals(Millis(10).abs, Millis(10))
    assertEquals(Millis(-10).abs, Millis(10))
  }

  test("min") {
    assertEquals(Millis(10).min(Millis(0)), Millis(0))
    assertEquals(Millis(10).min(Millis(100)), Millis(10))
  }

  test("max") {
    assertEquals(Millis(10).max(Millis(0)), Millis(10))
    assertEquals(Millis(10).max(Millis(100)), Millis(100))
  }

  test("clamp") {
    assertEquals(Millis(10).clamp(Millis(0), Millis(100)), Millis(10))
    assertEquals(Millis(-10).clamp(Millis(0), Millis(100)), Millis(0))
    assertEquals(Millis(1000).clamp(Millis(0), Millis(100)), Millis(100))
  }

}
