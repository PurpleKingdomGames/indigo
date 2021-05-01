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

}
