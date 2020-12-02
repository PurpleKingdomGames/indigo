package indigo.shared.time

class SecondsTests extends munit.FunSuite {

  test("Should be able to convert Seconds to Millis") {

    assertEquals(Seconds(10).toMillis, Millis(10000))
    assertEquals(Seconds(1.5).toMillis, Millis(1500))
    assertEquals(Seconds(1).toMillis, Millis(1000))

  }

}
