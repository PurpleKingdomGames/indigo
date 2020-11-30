package indigo.shared

class OutcomeTests extends munit.FunSuite {

  import indigo.shared.events.GlobalEvent
  import indigo.shared.EqualTo._
  import Outcome._

  final case class TestEvent(message: String) extends GlobalEvent


      test("Adding events") {

        test("adding events after the fact") {
          assertEquals(Outcome(10).globalEvents, Nil)
          assertEquals(Outcome(10).addGlobalEvents(TestEvent("a")).globalEvents, List(TestEvent("a")))
        }

        test("creating events based on new state") {
          val actual = Outcome(10)
            .addGlobalEvents(TestEvent("a"))
            .createGlobalEvents(i => List(TestEvent(s"count: $i")))
            .globalEvents

          val expected = List(TestEvent("a"), TestEvent("count: 10"))

          assertEquals(actual, expected)
        }

      }

      test("Extractor should allow pattern match") {
        val a = Outcome(1).addGlobalEvents(TestEvent("a"))

        a match {
          case Outcome(n, TestEvent(s) :: Nil) =>
            assertEquals(n, 1)
            assertEquals(s, "a")
        }
      }

      test("Transforming outcomes") {
        import indigo.shared.EqualTo._
        import Outcome._

        test("sequencing") {
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

          assertEquals(actual === expected, true)
        }

      }

      test("Equality") {

        test("true") {
          assertEquals(Outcome(1) === Outcome(1), true)
        }

        test("false") {
          assertEquals(Outcome(1) === Outcome(2), false)
        }
      }

      test("Show") {

        val actual = Outcome(1).toString()

        val expected = "Outcome(1, List())"

        assertEquals(actual, expected)
      }

      test("Mapping over Outcomes") {

        test("map state") {
          assertEquals(Outcome(10).mapState(_ + 10) === Outcome(20), true)
          assertEquals(Outcome(10).addGlobalEvents(TestEvent("a")).mapState(_ + 10) === Outcome(20).addGlobalEvents(TestEvent("a")), true)
        }

        test("map global events") {
          val actual =
            Outcome(10)
              .addGlobalEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
              .mapGlobalEvents(_.filter {
                case TestEvent(msg) =>
                  msg == "b"
              })

          val expected =
            Outcome(10)
              .addGlobalEvents(TestEvent("b"))

          assertEquals(actual === expected, true)
        }

        test("map all") {
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

          assertEquals(actual === expected, true)
        }

      }

      test("flat map & join") {

        test("join preserves event order") {
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

          assertEquals(actual === expected, true)
        }

        test("flatMapState") {
          assertEquals(Outcome(10).flatMapState(i => Outcome(i * 10)) === Outcome(100), true)
        }

      }

      test("Combine") {

        test("Outcomes can be combined") {

          val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
          val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

          val actual =
            oa |+| ob

          val expected =
            Outcome(("count", 1)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

          assertEquals(actual === expected, true)
        }

      }

      test("Applicative") {

        test("ap") {

          val actual: Outcome[Int] =
            Outcome(10).apState(Outcome((i: Int) => i + 10))

          val expected: Outcome[Int] =
            Outcome(20)

          assertEquals(actual === expected, true)
        }

        test("ap with event") {

          val actual: Outcome[Int] =
            Outcome(10).addGlobalEvents(TestEvent("x")).apState(Outcome((i: Int) => i + 10))

          val expected: Outcome[Int] =
            Outcome(20).addGlobalEvents(TestEvent("x"))

          assertEquals(actual === expected, true)
        }

        test("map2 / merge") {
          val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
          val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

          val actual: Outcome[String] =
            Outcome.merge(oa, ob)((a: String, b: Int) => a + ": " + b)

          val expected: Outcome[String] =
            Outcome("count: 1").addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

          assertEquals(actual === expected, true)

        }

      }

    }

}
