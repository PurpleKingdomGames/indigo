package indigo.shared.time

class MillisTests extends munit.FunSuite {

  test("Should be able to convert Millis to Seconds") {
    assertEquals(Millis(1000).toSeconds, Seconds(1))
    assertEquals(Millis(1500).toSeconds, Seconds(1.5))
    assertEquals(Millis(10001).toSeconds, Seconds(10.001))
  }

  test("Operations.modulo") {

    assertEquals(Millis(1) % Millis(2) === Millis(1), true)
    assertEquals(Millis(2) % Millis(2) === Millis(0), true)

  }

}
