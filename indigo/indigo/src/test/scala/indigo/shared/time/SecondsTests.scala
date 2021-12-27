package indigo.shared.time

class SecondsTests extends munit.FunSuite {

  test("Should be able to convert Seconds to Millis") {

    assertEquals(Seconds(10).toMillis, Millis(10000))
    assertEquals(Seconds(1.5).toMillis, Millis(1500))
    assertEquals(Seconds(1).toMillis, Millis(1000))

  }

  test("Can operate on doubles") {
    assertEquals(Seconds(1) + 2, Seconds(3))
    assertEquals(Seconds(1) - 2, Seconds(-1))
    assertEquals(Seconds(2) * 2, Seconds(4))
    assertEquals(Seconds(4) / 2, Seconds(2))
    assertEquals(Seconds(3) % 2, Seconds(1))
  }

  test("abs") {
    assertEquals(Seconds(10).abs, Seconds(10))
    assertEquals(Seconds(-10).abs, Seconds(10))
  }

  test("min") {
    assertEquals(Seconds(10).min(Seconds(0)), Seconds(0))
    assertEquals(Seconds(10).min(Seconds(100)), Seconds(10))
  }

  test("max") {
    assertEquals(Seconds(10).max(Seconds(0)), Seconds(10))
    assertEquals(Seconds(10).max(Seconds(100)), Seconds(100))
  }

  test("clamp") {
    assertEquals(Seconds(10).clamp(Seconds(0), Seconds(100)), Seconds(10))
    assertEquals(Seconds(-10).clamp(Seconds(0), Seconds(100)), Seconds(0))
    assertEquals(Seconds(1000).clamp(Seconds(0), Seconds(100)), Seconds(100))
  }

  test("approx equal") {
    assert(Seconds(0.033) ~== Seconds(0.0333))
  }

}
