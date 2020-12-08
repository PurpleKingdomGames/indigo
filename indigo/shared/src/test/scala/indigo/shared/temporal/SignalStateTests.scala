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

  test("flatMap") {
    // val a = SignalState((count: Int) => (count + 1, Signal.fixed("foo")))
    // val b = SignalState((count: Int) => (count + 1, Signal.fixed("foo")))
    // val a = SignalState((count: Int) => (count + 1, Signal.fixed("foo")))

    // val res =
    //   for {
    //     (count1, fooSig) <- a
    //   } yield aa

    // assertEquals(res.run(0), 10)

    val res =
      SignalState((count: Int) => Signal.fixed((count + 1, "foo"))).flatMap { (str: String) =>
        SignalState((c: Int) => Signal.fixed((c + 1, str + str)))
      }

    val (s, v) = res.run(0).at(Seconds(0))

    assertEquals(s, 2)
    assertEquals(v, "foofoo")

  }

}
/*
final case class State[S, A](run: S => (S, A)) {
  
  def get(s: S): A =
    run(s)._2
  
  def map[B](f: A => B): State[S, B] =
    State { (s: S) =>
      val (ss, value) = run(s)
      (ss, f(value))
    }
  
  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State { (s: S) =>
      val (s0, v0) = run(s)
      val (s1, v1) = f(v0).run(s0)
      (s1, v1)
    }
  
}

val foo = State((count: Int) => (count + 1, "foo"))


val f: String => State[Int, Boolean] =
  (str: String) => State((i: Int) => (i + 1, str == "foo"))

val g: Boolean => State[Int, Int] =
  (p: Boolean) => State((i: Int) => (i + 1, if(p) 10 else 100))

foo.run(0)
foo.flatMap(f).run(0)
foo.flatMap(f).flatMap(g).run(0)
*/
