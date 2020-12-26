package indigo.shared.temporal

import indigo.shared.time.Seconds

class SignalReaderTests extends munit.FunSuite {

  test("contruction methods and value retrieval") {
    // Normal
    assertEquals(
      SignalReader((name: String) => Signal(t => s"name was '$name' and time was '${t.toLong}'"))
        .toSignal("Fred")
        .at(Seconds(10)),
      "name was 'Fred' and time was '10'"
    )

    // Quick creation from a value
    assertEquals(
      SignalReader.fixed[Unit, Int](100).toSignal(()).at(Seconds.zero),
      100
    )

    // When time isn't of interest
    assertEquals(
      SignalReader.fromSignal[Unit, Int](Signal.fixed(10)).toSignal(()).at(Seconds.zero),
      10
    )
  }

  test("map") {
    val s = SignalReader((i: Int) => Signal.fixed("count: " + i.toString()))
      .map((msg: String) => msg + ", " + msg)

    assertEquals(s.toSignal(10).at(Seconds.zero), "count: 10, count: 10")
  }

  test("ap") {
    val apf: SignalReader[Unit, Int => String] =
      SignalReader.fixed((i: Int) => "count: " + i)

    val sr: SignalReader[Unit, Int] =
      SignalReader.fixed(10)

    assertEquals(
      sr.ap(apf).toSignal(()).at(Seconds.zero),
      "count: 10"
    )
  }

  test("flatMap") {

    val message = "amount: "

    val actual: SignalReader[String, (String, Int)] =
      for {
        a <- SignalReader.fromSignal(Signal(_.value))
        b <- SignalReader((msg: String) => Signal.fixed(msg + a.toString()))
        c <- SignalReader.fixed((b, b.length))
      } yield c

    val expected =
      (message + "1", 9)

    assertEquals(actual.toSignal(message).at(Seconds(1)), expected)
  }

  test("merge") {
    val a = SignalReader.fixed[Unit, Int](100)
    val b = SignalReader.fixed[Unit, String]("hello")

    val actual =
      a.merge(b)(_.toString() + ", " + _).run(()).at(Seconds.zero)

    val expected = "100, hello"

    assertEquals(actual, expected)
  }

  test("combine") {
    val a = SignalReader.fixed[Unit, Int](100)
    val b = SignalReader.fixed[Unit, String]("hello")

    val actual =
      a.combine(b).run(()).at(Seconds.zero)

    val expected =
      (100, "hello")

    assertEquals(actual, expected)
  }
}
