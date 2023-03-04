package indigo.shared

import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent

import Outcome.*

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class OutcomeTests extends munit.FunSuite {

  test("Adding events.adding events after the fact") {
    assertEquals(Outcome(10).unsafeGlobalEvents, Batch.empty)
    assertEquals(Outcome(10).addGlobalEvents(TestEvent("a")).unsafeGlobalEvents, Batch(TestEvent("a")))
  }

  test("Adding events.creating events based on new state") {
    val actual = Outcome(10)
      .addGlobalEvents(TestEvent("a"))
      .createGlobalEvents(i => Batch(TestEvent(s"count: $i")))
      .unsafeGlobalEvents

    val expected = Batch(TestEvent("a"), TestEvent("count: 10"))

    assertEquals(actual, expected)
  }

  test("Extractor should allow pattern match") {
    val a = Outcome(1).addGlobalEvents(TestEvent("a"))

    a match {
      case Outcome(n, Batch(TestEvent(s))) =>
        assertEquals(n, 1)
        assertEquals(s, "a")

      case _ =>
        fail("shouldn't have got here.")
    }
  }

  test("Transforming outcomes.sequencing (list)") {
    val l: List[Outcome[Int]] =
      List(
        Outcome(1).addGlobalEvents(TestEvent("a")),
        Outcome(2).addGlobalEvents(TestEvent("b")),
        Outcome(3).addGlobalEvents(TestEvent("c"))
      )

    val actual: Outcome[List[Int]] =
      l.sequence

    val expected: Outcome[List[Int]] =
      Outcome(List(1, 2, 3))
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Transforming outcomes.sequencing (batch)") {
    val l: Batch[Outcome[Int]] =
      Batch(
        Outcome(1).addGlobalEvents(TestEvent("a")),
        Outcome(2).addGlobalEvents(TestEvent("b")),
        Outcome(3).addGlobalEvents(TestEvent("c"))
      )

    val actual: Outcome[Batch[Int]] =
      l.sequence

    val expected: Outcome[Batch[Int]] =
      Outcome(Batch(1, 2, 3))
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Mapping over Outcomes.map state") {
    assertEquals(Outcome(10).map(_ + 10).unsafeGet, Outcome(20).unsafeGet)

    assertEquals(
      Outcome(10).addGlobalEvents(TestEvent("a")).map(_ + 10).unsafeGet,
      Outcome(20).addGlobalEvents(TestEvent("a")).unsafeGet
    )
    assertEquals(
      Outcome(10).addGlobalEvents(TestEvent("a")).map(_ + 10).unsafeGlobalEvents,
      Outcome(20).addGlobalEvents(TestEvent("a")).unsafeGlobalEvents
    )
  }

  test("Replace global event list") {
    val actual =
      Outcome(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .replaceGlobalEvents(_.filter {
          case TestEvent(msg) =>
            msg == "b"

          case _ =>
            fail("Boom.")
        })

    val expected =
      Outcome(10)
        .addGlobalEvents(TestEvent("b"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("clear global event list") {
    val actual =
      Outcome(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .clearGlobalEvents

    val expected =
      Outcome(10, Batch.empty)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Mapping over Outcomes.map global events") {
    val actual =
      Outcome(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .mapGlobalEvents {
          case TestEvent(msg) =>
            TestEvent(msg + msg)

          case _ =>
            fail("Boom.")
        }

    val expected =
      Outcome(10)
        .addGlobalEvents(TestEvent("aa"), TestEvent("bb"), TestEvent("cc"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Mapping over Outcomes.map all") {
    val actual =
      Outcome(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .mapAll(
          _ + 20,
          _.filter {
            case TestEvent(msg) =>
              msg == "b"

            case _ =>
              fail("Boom.")
          }
        )

    val expected =
      Outcome(30)
        .addGlobalEvents(TestEvent("b"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("flat map & join.join preserves event order") {
    val oa =
      Outcome(
        Outcome(
          Outcome(10).addGlobalEvents(TestEvent("z"))
        ).addGlobalEvents(TestEvent("x"), TestEvent("y"))
      ).addGlobalEvents(TestEvent("w"))

    val expected =
      Outcome(10)
        .addGlobalEvents(TestEvent("w"), TestEvent("x"), TestEvent("y"), TestEvent("z"))

    val actual = Outcome.join(Outcome.join(oa))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("flat map & join.flatMap") {
    assertEquals(Outcome(10).flatMap(i => Outcome(i * 10)).unsafeGet, Outcome(100).unsafeGet)
    assertEquals(Outcome(10).flatMap(i => Outcome(i * 10)).unsafeGlobalEvents, Outcome(100).unsafeGlobalEvents)

    assertEquals(Outcome.join(Outcome(10).map(i => Outcome(i * 10))).unsafeGet, Outcome(100).unsafeGet)
    assertEquals(
      Outcome.join(Outcome(10).map(i => Outcome(i * 10))).unsafeGlobalEvents,
      Outcome(100).unsafeGlobalEvents
    )
  }

  test("Applicative.ap") {

    val actual: Outcome[Int] =
      Outcome(10).ap(Outcome((i: Int) => i + 10))

    val expected: Outcome[Int] =
      Outcome(20)

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Applicative.ap with event") {

    val actual: Outcome[Int] =
      Outcome(10).addGlobalEvents(TestEvent("x")).ap(Outcome((i: Int) => i + 10))

    val expected: Outcome[Int] =
      Outcome(20).addGlobalEvents(TestEvent("x"))

    assertEquals(actual.unsafeGet, expected.unsafeGet)
    assertEquals(actual.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Combine - 2 Outcomes can be combined") {

    val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

    val actual1 = oa.combine(ob)
    val actual2 = Outcome.combine(oa, ob)
    val actual3 = (oa, ob).combine

    val expected =
      Outcome(("count", 1)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Combine - 3 Outcomes can be combined") {

    val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))
    val oc = Outcome(true).addGlobalEvents(TestEvent("w"))

    val actual1 = Outcome.combine3(oa, ob, oc)
    val actual2 = (oa, ob, oc).combine

    val expected =
      Outcome(("count", 1, true)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"), TestEvent("w"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Applicative.map2 / merge") {
    val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

    val actual1: Outcome[String] =
      Outcome.merge(oa, ob)((a: String, b: Int) => a + ": " + b)
    val actual2: Outcome[String] =
      oa.merge(ob)((a: String, b: Int) => a + ": " + b)
    val actual3: Outcome[String] =
      (oa, ob).merge((a: String, b: Int) => a + ": " + b)

    val expected: Outcome[String] =
      Outcome("count: 1").addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual3.unsafeGet, expected.unsafeGet)
    assertEquals(actual3.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  test("Applicative.map3 / merge") {
    val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))
    val oc = Outcome(true).addGlobalEvents(TestEvent("w"))

    val actual1: Outcome[String] =
      Outcome.merge3(oa, ob, oc)((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)
    val actual2: Outcome[String] =
      (oa, ob, oc).merge((a: String, b: Int, c: Boolean) => a + ": " + b + ": " + c)

    val expected: Outcome[String] =
      Outcome("count: 1: true").addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"), TestEvent("w"))

    assertEquals(actual1.unsafeGet, expected.unsafeGet)
    assertEquals(actual1.unsafeGlobalEvents, expected.unsafeGlobalEvents)
    assertEquals(actual2.unsafeGet, expected.unsafeGet)
    assertEquals(actual2.unsafeGlobalEvents, expected.unsafeGlobalEvents)
  }

  // Error handline

  def errorsMatch[A](actual: Outcome[A], expected: Outcome[A]): Boolean =
    (actual, expected) match {
      case (Outcome.Error(e1, _), Outcome.Error(e2, _)) =>
        e1.getMessage == e2.getMessage

      case _ =>
        false
    }

  test("Exceptions thrown during creation are handled") {
    val e = new Exception("Boom!")

    val actual =
      Outcome(throw e)

    val expected =
      Outcome.Error(e)

    assert(errorsMatch(actual, expected))
  }

  test("mapping an error") {
    val e = new Exception("Boom!")
    val actual =
      Outcome(10).map[Int](_ => throw e).map(i => i * i)

    val expected =
      Outcome.Error(e)

    assert(errorsMatch(actual, expected))
  }

  test("flatMapping an error") {
    def foo(): Int =
      throw new Exception("amount: 10")

    val actual =
      for {
        a <- Outcome(10)
        b <- Outcome(foo())
        c <- Outcome(30)
      } yield a + b + c

    val expected =
      Outcome.Error(new Exception("amount: 10"))

    assertEquals(actual.isError, expected.isError)
    assert(errorsMatch(actual, expected))
  }

  test("raising an error") {
    val e = new Exception("Boom!")

    def foo(o: Outcome[Int]): Outcome[Int] =
      o.flatMap { i =>
        if (i % 2 == 0) Outcome(i * 10)
        else Outcome.raiseError(e)
      }

    assertEquals(foo(Outcome(4)), Outcome(40))
    assert(errorsMatch(foo(Outcome(5)), Outcome(throw e)))
  }

  test("recovering from an error") {
    val e = new Exception("Boom!")
    val actual =
      Outcome(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .handleError { case e =>
          Outcome(e.getMessage.length)
        }

    val expected =
      Outcome(5)

    assertEquals(actual, expected)
  }

  test("recovering from an error with orElse") {
    val e = new Exception("Boom!")
    val actual =
      Outcome(10)
        .map[Int](_ => throw e)
        .map(i => i * i)
        .orElse(Outcome(e.getMessage.length))

    val expected =
      Outcome(5)

    assertEquals(actual, expected)
  }

  test("logging a crash") {
    val e = new Exception("Boom!")

    val actual =
      try
        Outcome(10)
          .map[Int](_ => throw e)
          .map(i => i * i)
          .logCrash { case e => e.getMessage }
      catch {
        case _: Throwable =>
          ()
      }

    val expected =
      "Boom!"

    actual match {
      case Error(e, r) =>
        assertEquals(r(e), expected)

      case _ =>
        fail("Failed...")
    }

  }

  test("Convert Option[A] to Outcome[A]") {
    import indigo.syntax.*

    val e = new Exception("Boom!")

    val actual =
      Option(123).toOutcome(e)

    val expected =
      Outcome(123)

    assertEquals(actual, expected)
  }

  test("Convert Option[A] to Outcome[A] (error case)") {
    import indigo.syntax.*

    val e = new Exception("Boom!")

    val actual =
      Option.empty[Int].toOutcome(e)

    val expected =
      Outcome.Error(e)

    assert(errorsMatch(actual, expected))
  }

}

final case class TestEvent(message: String) extends GlobalEvent
