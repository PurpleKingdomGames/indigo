package indigo.shared

import indigo.shared.events.GlobalEvent

import Outcome._

class OutcomeTests extends munit.FunSuite {

  test("Adding events.adding events after the fact") {
    assertEquals(Outcome(10).globalEvents, Nil)
    assertEquals(Outcome(10).addGlobalEvents(TestEvent("a")).globalEvents, List(TestEvent("a")))
  }

  test("Adding events.creating events based on new state") {
    val actual = Outcome(10)
      .addGlobalEvents(TestEvent("a"))
      .createGlobalEvents(i => List(TestEvent(s"count: $i")))
      .globalEvents

    val expected = List(TestEvent("a"), TestEvent("count: 10"))

    assertEquals(actual, expected)
  }

  test("Extractor should allow pattern match") {
    val a = Outcome(1).addGlobalEvents(TestEvent("a"))

    a match {
      case Outcome(n, TestEvent(s) :: Nil) =>
        assertEquals(n, 1)
        assertEquals(s, "a")

      case _ =>
        fail("shouldn't have got here.")
    }
  }

  test("Transforming outcomes.sequencing") {
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

    assertEquals(actual, expected)
  }

  test("Equality true") {
    assert(Outcome(1) == Outcome(1))
  }

  test("Equality false") {
    assert((Outcome(1) == Outcome(2)) == false)
  }

  test("Show") {

    val actual = Outcome(1).toString()

    val expected = "Outcome(1,List())"

    assertEquals(actual, expected)
  }

  test("Mapping over Outcomes.map state") {
    assertEquals(Outcome(10).map(_ + 10), Outcome(20))
    assertEquals(Outcome(10).addGlobalEvents(TestEvent("a")).map(_ + 10), Outcome(20).addGlobalEvents(TestEvent("a")))
  }

  test("Replace global event list") {
    val actual =
      Outcome(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .replaceGlobalEvents(_.filter {
          case TestEvent(msg) =>
            msg == "b"
        })

    val expected =
      Outcome(10)
        .addGlobalEvents(TestEvent("b"))

    assertEquals(actual, expected)
  }

  test("clear global event list") {
    val actual =
      Outcome(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .clearGlobalEvents

    val expected =
      Outcome(10, Nil)

    assertEquals(actual, expected)
  }

  test("Mapping over Outcomes.map global events") {
    val actual =
      Outcome(10)
        .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
        .mapGlobalEvents {
          case TestEvent(msg) =>
            TestEvent(msg + msg)
        }

    val expected =
      Outcome(10)
        .addGlobalEvents(TestEvent("aa"), TestEvent("bb"), TestEvent("cc"))

    assertEquals(actual, expected)
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
          }
        )

    val expected =
      Outcome(30)
        .addGlobalEvents(TestEvent("b"))

    assertEquals(actual, expected)
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

    assertEquals(actual, expected)
  }

  test("flat map & join.flatMap") {
    assertEquals(Outcome(10).flatMap(i => Outcome(i * 10)), Outcome(100))
    assertEquals(Outcome.join(Outcome(10).map(i => Outcome(i * 10))), Outcome(100))
  }

  test("Applicative.ap") {

    val actual: Outcome[Int] =
      Outcome(10).ap(Outcome((i: Int) => i + 10))

    val expected: Outcome[Int] =
      Outcome(20)

    assertEquals(actual, expected)
  }

  test("Applicative.ap with event") {

    val actual: Outcome[Int] =
      Outcome(10).addGlobalEvents(TestEvent("x")).ap(Outcome((i: Int) => i + 10))

    val expected: Outcome[Int] =
      Outcome(20).addGlobalEvents(TestEvent("x"))

    assertEquals(actual, expected)
  }

  test("Combine - 2 Outcomes can be combined") {

    val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

    val actual1 = oa.combine(ob)
    val actual2 = Outcome.combine(oa, ob)
    val actual3 = (oa, ob).combine

    val expected =
      Outcome(("count", 1)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

    assertEquals(actual1, expected)
    assertEquals(actual2, expected)
    assertEquals(actual3, expected)
  }

  test("Combine - 3 Outcomes can be combined") {

    val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
    val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))
    val oc = Outcome(true).addGlobalEvents(TestEvent("w"))

    val actual1 = Outcome.combine3(oa, ob, oc)
    val actual2 = (oa, ob, oc).combine

    val expected =
      Outcome(("count", 1, true)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"), TestEvent("w"))

    assertEquals(actual1, expected)
    assertEquals(actual2, expected)
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

    assertEquals(actual1, expected)
    assertEquals(actual2, expected)
    assertEquals(actual3, expected)
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

    assertEquals(actual1, expected)
    assertEquals(actual2, expected)
  }

}

final case class TestEvent(message: String) extends GlobalEvent
