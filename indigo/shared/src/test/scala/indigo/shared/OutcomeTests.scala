package indigo.shared

import utest._

object OutcomeTests extends TestSuite {

  import indigo.shared.events.GlobalEvent
  import indigo.shared.EqualTo._
  import Outcome._

  final case class TestEvent(message: String) extends GlobalEvent

  val tests: Tests =
    Tests {
      "Adding events" - {

        "adding events after the fact" - {
          Outcome(10).globalEvents ==> Nil
          Outcome(10).addGlobalEvents(TestEvent("a")).globalEvents ==> List(TestEvent("a"))
        }

        "creating events based on new state" - {
          val actual = Outcome(10)
            .addGlobalEvents(TestEvent("a"))
            .createGlobalEvents(i => List(TestEvent(s"count: $i")))
            .globalEvents

          val expected = List(TestEvent("a"), TestEvent("count: 10"))

          actual ==> expected
        }

      }

      "Extractor should allow pattern match" - {
        val a = Outcome(1).addGlobalEvents(TestEvent("a"))

        a match {
          case Outcome(n, TestEvent(s) :: Nil) =>
            n ==> 1
            s ==> "a"
        }
      }

      "Transforming outcomes" - {
        import indigo.shared.EqualTo._
        import Outcome._

        "sequencing" - {
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

          actual === expected ==> true
        }

      }

      "Equality" - {

        "true" - {
          Outcome(1) === Outcome(1) ==> true
        }

        "false" - {
          Outcome(1) === Outcome(2) ==> false
        }
      }

      "Show" - {

        val actual = Outcome(1).toString()

        val expected = "Outcome(1, List())"

        actual ==> expected
      }

      "Mapping over Outcomes" - {

        "map state" - {
          Outcome(10).mapState(_ + 10) === Outcome(20) ==> true
          Outcome(10).addGlobalEvents(TestEvent("a")).mapState(_ + 10) === Outcome(20).addGlobalEvents(TestEvent("a")) ==> true
        }

        "map global events" - {
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

          actual === expected ==> true
        }

        "map all" - {
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

          actual === expected ==> true
        }

      }

      "flat map & join" - {

        "join preserves event order" - {
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

          actual === expected ==> true
        }

        "flatMapState" - {
          Outcome(10).flatMapState(i => Outcome(i * 10)) === Outcome(100) ==> true
        }

      }

      "Combine" - {

        "Outcomes can be combined" - {

          val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
          val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

          val actual =
            oa |+| ob

          val expected =
            Outcome(("count", 1)).addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

          actual === expected ==> true
        }

      }

      "Applicative" - {

        "ap" - {

          val actual: Outcome[Int] =
            Outcome(10).apState(Outcome((i: Int) => i + 10))

          val expected: Outcome[Int] =
            Outcome(20)

          actual === expected ==> true
        }

        "ap with event" - {

          val actual: Outcome[Int] =
            Outcome(10).addGlobalEvents(TestEvent("x")).apState(Outcome((i: Int) => i + 10))

          val expected: Outcome[Int] =
            Outcome(20).addGlobalEvents(TestEvent("x"))

          actual === expected ==> true
        }

        "map2 / merge" - {
          val oa = Outcome("count").addGlobalEvents(TestEvent("x"))
          val ob = Outcome(1).addGlobalEvents(TestEvent("y"), TestEvent("z"))

          val actual: Outcome[String] =
            Outcome.merge(oa, ob)((a: String, b: Int) => a + ": " + b)

          val expected: Outcome[String] =
            Outcome("count: 1").addGlobalEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

          actual === expected ==> true

        }

      }

    }

}
