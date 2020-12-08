package indigo.shared.temporal

import indigo.shared.time.Seconds

class SignalStateTests extends munit.FunSuite {

  test("contruction methods and value retrieval") {
    // Normal
    assertEquals(
      SignalState((name: String) => Signal(t => (name, s"name was '$name' and time was '${t.toLong}'")))
        .toSignal("Fred")
        .at(Seconds(10)),
      "name was 'Fred' and time was '10'"
    )

    // Quick creation from a value
    assertEquals(
      SignalState.fixed[Unit, Int](100).toSignal(()).at(Seconds.zero),
      100
    )

    // When time isn't of interest
    assertEquals(
      SignalState.fromSignal[Unit, Int](Signal.fixed(10)).toSignal(()).at(Seconds.zero),
      10
    )
  }

  test("modify") {
    val sig = SignalState((count: Int) => Signal.fixed(count + 1, "foo"))

    assertEquals(sig.modify(_ * 10).get.run(10).at(Seconds.zero)._2, 110)
  }

  test("get") {
    val sig = SignalState((count: Int) => Signal.fixed(count + 1, "foo"))

    assertEquals(sig.get.run(1).at(Seconds.zero)._2, 2)
  }

  test("set") {
    val sig = SignalState((count: Int) => Signal.fixed(count + 1, "foo"))

    assertEquals(sig.set(200).get.run(0).at(Seconds.zero)._2, 200)
  }

  test("map") {
    val signal =
      SignalState((name: String) => Signal.fixed((name, s"name: $name")))

    assertEquals(
      signal.toSignal("Fred").at(Seconds.zero),
      "name: Fred"
    )

    assertEquals(
      signal.map(_ + " Smith").toSignal("Fred").at(Seconds.zero),
      "name: Fred Smith"
    )

    assertEquals(
      signal.map(_ + " Smith").map(_ => "Bob").toSignal("Fred").at(Seconds.zero),
      "Bob"
    )

  }

  test("ap") {
    val apf = SignalState((str: String) => Signal.fixed((str + "f", (str2: String) => str2 + "bar")))
    val ss  = SignalState((str: String) => Signal.fixed((str + "s", "foo")))

    assertEquals(
      ss.ap(apf).run("order: ").at(Seconds.zero),
      ("order: sf", "foobar")
    )
  }

  test("flatMap") {

    val res =
      SignalState((count: Int) => Signal.fixed((count + 1, "foo"))).flatMap { (str: String) =>
        SignalState((c: Int) => Signal.fixed((c + 1, str + str)))
      }

    val (s, v) = res.run(0).at(Seconds(0))

    assertEquals(s, 2)
    assertEquals(v, "foofoo")

  }

  test("for comp") {
    val a = SignalState((count: Int) => Signal.fixed(count + 1, "foo"))
    val b = SignalState((count: Int) => Signal.fixed(count + 1, "bar"))
    val c = SignalState((count: Int) => Signal.fixed(count + 1, "baz"))

    val res =
      for {
        aa <- a
        bb <- b.map(_ + s"($aa)")
        cc <- c.map(_ + s"($aa)[$bb]")
      } yield aa + ", " + bb + ", " + cc

    val actual =
      res.run(10).at(Seconds(0))

    val expected =
      (13, "foo, bar(foo), baz(foo)[bar(foo)]")

    assertEquals(actual, expected)

  }

  test("merge") {
    val a = SignalState((count: Int) => Signal.fixed(count + 1, "foo"))
    val b = SignalState((count: Int) => Signal.fixed(count + 1, "bar"))

    val actual =
      a.merge(b)(_ + ", " + _).run(0).at(Seconds.zero)

    val expected =
      (2, "foo, bar")

    assertEquals(actual, expected)
  }

  test("combine") {
    val a = SignalState((count: Int) => Signal.fixed(count + 1, "foo"))
    val b = SignalState((count: Int) => Signal.fixed(count + 1, "bar"))

    val actual =
      a.combine(b).run(0).at(Seconds.zero)

    val expected =
      (2, ("foo", "bar"))

    assertEquals(actual, expected)
  }

  test("signal functions") {
    val sf1: SignalFunction[Int, String] =
      SignalFunction((i: Int) => List.fill(i)("i").mkString)

    val sf2: SignalFunction[String, String] =
      SignalFunction((str: String) => "[" + str + "]")

    val sig: SignalState[Int, Int] =
      SignalState((count: Int) => Signal.fixed(count + 1, count + 5))

    val actual =
      (sig |> sf1 >>> sf2).run(1).at(Seconds.zero)

    val expected =
      (1, "[iiiiii]")

    assertEquals(actual, expected)

  }

}
