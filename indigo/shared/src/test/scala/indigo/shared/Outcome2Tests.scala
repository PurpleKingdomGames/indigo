package indigo.shared

import indigo.shared.events.GlobalEvent

import Outcome2._

class Outcome2Tests extends munit.FunSuite {

  test("Adding events.adding events after the fact") {
    assertEquals(Outcome2(10).unsafeGlobalEvents, Nil)
    assertEquals(Outcome2(10).addGlobalEvents(TestEvent("a")).unsafeGlobalEvents, List(TestEvent("a")))
  }

  test("Adding events.creating events based on new state") {
    val actual = Outcome2(10)
      .addGlobalEvents(TestEvent("a"))
      .createGlobalEvents(i => List(TestEvent(s"count: $i")))
      .unsafeGlobalEvents

    val expected = List(TestEvent("a"), TestEvent("count: 10"))

    assertEquals(actual, expected)
  }

  test("Extractor should allow pattern match") {
    val a = Outcome2(1).addGlobalEvents(TestEvent("a"))

    a match {
      case Outcome2(n, TestEvent(s) :: Nil) =>
        assertEquals(n, 1)
        assertEquals(s, "a")

      case _ =>
        fail("shouldn't have got here.")
    }
  }

  test("Transforming outcomes.sequencing") {
    val l: List[Outcome2[Int]] =
      List(
        Outcome2(1).addGlobalEvents(TestEvent("a")),
        Outcome2(2).addGlobalEvents(TestEvent("b")),
        Outcome2(3).addGlobalEvents(TestEvent("c"))
      )

    val actual: Outcome2[List[Int]] =
      l.sequence

    val expected: Outcome2[List[Int]] =
      Outcome2(List(1, 2, 3))
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Mapping over Outcomes.map state") {
    assertEquals(Outcome2(10).map(_ + 10).unsafeGet, Outcome2(20).unsafeGet)

    assertEquals(Outcome2(10).addGlobalEvents(TestEvent("a")).map(_ + 10).unsafeGet, Outcome2(20).addGlobalEvents(TestEvent("a")).unsafeGet)
    assertEquals(Outcome2(10).addGlobalEvents(TestEvent("a")).map(_ + 10).unsafeGlobalEvents, Outcome2(20).addGlobalEvents(TestEvent("a")).unsafeGlobalEvents)
  }

  test("Replace global event list") {
    val actual =
      Outcome2(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .replaceGlobalEvents(_.filter {
          case TestEvent(msg) =>
            msg == "b"
        })

    val expected =
      Outcome2(10)
        .addGlobalEvents(TestEvent("b"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("clear global event list") {
    val actual =
      Outcome2(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .clearGlobalEvents

    val expected =
      Outcome2(10, Nil)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Mapping over Outcomes.map global events") {
    val actual =
      Outcome2(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .mapGlobalEvents {
          case TestEvent(msg) =>
            TestEvent(msg + msg)
        }

    val expected =
      Outcome2(10)
        .addGlobalEvents(TestEvent("aa"), TestEvent("bb"), TestEvent("cc"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Mapping over Outcomes.map all") {
    val actual =
      Outcome2(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .mapAll(
          _ + 20,
          _.filter {
            case TestEvent(msg) =>
              msg == "b"
          }
        )

    val expected =
      Outcome2(30)
        .addGlobalEvents(TestEvent("b"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("flat map & join.join preserves event order") {
    val oa =
      Outcome2(
        Outcome2(
          Outcome2(10).addGlobalEvents(TestEvent("z"))
        ).addGlobalEvents(TestEvent("x"), TestEvent("y"))
      ).addGlobalEvents(TestEvent("w"))

    val expected =
      Outcome2(10)
        .addGlobalEvents(TestEvent("w"), TestEvent("x"), TestEvent("y"), TestEvent("z"))

    val actual = Outcome2.join(Outcome2.join(oa))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("flat map & join.flatMap") {
    assertEquals(Outcome2(10).flatMap(i => Outcome2(i * 10)).unsafeGet, Outcome2(100).unsafeGet)
    assertEquals(Outcome2(10).flatMap(i => Outcome2(i * 10)).unsafeGlobalEvents, Outcome2(100).unsafeGlobalEvents)

    assertEquals(Outcome2.join(Outcome2(10).map(i => Outcome2(i * 10))).unsafeGet, Outcome2(100).unsafeGet)
    assertEquals(Outcome2.join(Outcome2(10).map(i => Outcome2(i * 10))).unsafeGlobalEvents, Outcome2(100).unsafeGlobalEvents)
  }

  test("Applicative.ap") {

    val actual: Outcome2[Int] =
      Outcome2(10).ap(Outcome2((i: Int) => i + 10))

    val expected: Outcome2[Int] =
      Outcome2(20)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Applicative.ap with event") {

    val actual: Outcome2[Int] =
      Outcome2(10).addGlobalEvents(TestEvent("x")).ap(Outcome2((i: Int) => i + 10))

    val expected: Outcome2[Int] =
      Outcome2(20).addGlobalEvents(TestEvent("x"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Combine - 2 Outcomes can be combined") {

    val oa = Outcome2("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome2(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

    val actual1 = oa.combine(ob)
    val actual2 = Outcome2.combine(oa, ob)
    val actual3 = (oa, ob).combine

    val expected =
      Outcome2(("count", 1)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Combine - 3 Outcomes can be combined") {

    val oa = Outcome2("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome2(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))
    val oc = Outcome2(true).addGlobalEvents(TestEvent("w"))

    val actual1 = Outcome2.combine3(oa, ob, oc)
    val actual2 = (oa, ob, oc).combine

    val expected =
      Outcome2(("count", 1, true)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"), TestEvent("w"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Applicative.map2 / merge") {
    val oa = Outcome2("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome2(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

    val actual1: Outcome2[String] =
      Outcome2.merge(oa, ob)((a: String, b: Int) => a + ": " + b)
    val actual2: Outcome2[String] =
      oa.merge(ob)((a: String, b: Int) => a + ": " + b)
    val actual3: Outcome2[String] =
      (oa, ob).merge((a: String, b: Int) => a + ": " + b)

    val expected: Outcome2[String] =
      Outcome2("count: 1").addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Applicative.map3 / merge") {
    val oa = Outcome2("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome2(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))
    val oc = Outcome2(true).addGlobalEvents(TestEvent("w"))

    val actual1: Outcome2[String] =
      Outcome2.merge3(oa, ob, oc)((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)
    val actual2: Outcome2[String] =
      (oa, ob, oc).merge((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)

    val expected: Outcome2[String] =
      Outcome2("count: 1: true").addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"), TestEvent("w"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  // Error handline

  test("Exceptions thrown during creation are handled") {
    val e = new Exception("Boom!")

    val actual =
      Outcome2(throw e)

    val expected =
      Outcome2.Error(e)

    assertEquals(actual, expected)
  }

  test("mapping an error") {
    val e = new Exception("Boom!")
    val actual =
      Outcome2(10).map[Int](_ => throw e).map(i => i * i)

    val expected =
      Outcome2.Error(e)

    assertEquals(actual, expected)
  }

  test("flatMapping an error") {
    def foo(): Int =
      throw new Exception("amount: 10")

    val actual =
      for {
        a <- Outcome2(10)
        b <- Outcome2(foo())
        c <- Outcome2(30)
      } yield a + b + c

    val expected =
      Outcome2.Error(new Exception("amount: 10"))

    assertEquals(actual.isError, expected.isError)

    (actual, expected) match {
      case (Outcome2.Error(e1), Outcome2.Error(e2)) =>
        assertEquals(e1.getMessage, e2.getMessage)

      case _ =>
        fail("test failed, should have got here.")
    }
  }

  test("raising an error") {
    val e = new Exception("Boom!")

    def foo(o: Outcome2[Int]): Outcome2[Int] =
      o.flatMap { i =>
        if (i % 2 == 0) Outcome2(i * 10)
        else Outcome2.raiseError(e)
      }

    val expected =
      Outcome2.Error(e)

    assertEquals(foo(Outcome2(4)), Outcome2(40))
    assertEquals(foo(Outcome2(5)), Outcome2(throw e))
  }

  test("recovering from an error") {
    val e = new Exception("Boom!")
    val actual =
      Outcome2(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .handleError {
          case e =>
            Outcome2(e.getMessage.length)
        }

    val expected =
      Outcome2(5)

    assertEquals(actual, expected)
  }

  test("recovering from an error with orElse") {
    val e = new Exception("Boom!")
    val actual =
      Outcome2(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .orElse(Outcome2(e.getMessage.length))

    val expected =
      Outcome2(5)

    assertEquals(actual, expected)
  }

  test("logging a crash") {
    val e = new Exception("Boom!")

    var logs: String = ""

    try Outcome2(10)
      .map[Int](_ => throw e)
      .map(i => i * i)
      .logCrash(e => logs = e.getMessage)
    catch {
      _ => ()
    }

    val expected =
      "Boom!"

    assertEquals(logs, expected)
  }

}

final case class TestEvent(message: String) extends GlobalEvent
