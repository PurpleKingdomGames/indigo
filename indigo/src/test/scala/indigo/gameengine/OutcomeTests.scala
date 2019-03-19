package indigo.gameengine

import utest._

object OutcomeTests extends TestSuite {
  import indigo._
  import indigo.Eq._
  import Outcome._

  final case class TestEvent(message: String) extends GlobalEvent

  val tests: Tests =
    Tests {
      "Adding events" - {

        "adding events after the fact" - {
          Outcome(10).events ==> Nil
          Outcome(10).addEvents(TestEvent("a")).events ==> List(TestEvent("a"))
        }

        "creating events based on new state" - {
          val actual = Outcome(10)
            .addEvents(TestEvent("a"))
            .createEvents(i => List(TestEvent(s"count: $i")))
            .events

          val expected = List(TestEvent("a"), TestEvent("count: 10"))

          actual ==> expected
        }

      }

      "Extractor should allow pattern match" - {
        val a = Outcome(1).addEvents(TestEvent("a"))

        a match {
          case Outcome(n, TestEvent(s) :: Nil) =>
            n ==> 1
            s ==> "a"
        }
      }

      "Transforming outcomes" - {
        import indigo.Eq._
        import Outcome._

        "sequencing" - {
          val l: List[Outcome[Int]] =
            List(
              Outcome(1).addEvents(TestEvent("a")),
              Outcome(2).addEvents(TestEvent("b")),
              Outcome(3).addEvents(TestEvent("c"))
            )

          val actual: Outcome[List[Int]] =
            l.sequence

          val expected: Outcome[List[Int]] =
            Outcome(List(1, 2, 3))
              .addEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))

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

        val actual = Outcome(1).show

        val expected = "Outcome(1, [])"

        actual ==> expected
      }

      "Mapping over Outcomes" - {

        "map state" - {
          Outcome(10).mapState(_ + 10) === Outcome(20) ==> true
          Outcome(10).addEvents(TestEvent("a")).mapState(_ + 10) === Outcome(20).addEvents(TestEvent("a")) ==> true
        }

        "map events" - {
          val actual =
            Outcome(10)
              .addEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
              .mapEvents(_.filter {
                case TestEvent(msg) =>
                  msg == "b"
              })

          val expected =
            Outcome(10)
              .addEvents(TestEvent("b"))

          actual === expected ==> true
        }

        "map all" - {
          val actual =
            Outcome(10)
              .addEvents(TestEvent("a"), TestEvent("b"), TestEvent("c"))
              .mapAll(
                _ + 20,
                _.filter {
                  case TestEvent(msg) =>
                    msg == "b"
                }
              )

          val expected =
            Outcome(30)
              .addEvents(TestEvent("b"))

          actual === expected ==> true
        }

      }

      "Combine" - {

        "Outcomes can be combined" - {

          val oa = Outcome("count").addEvents(TestEvent("x"))
          val ob = Outcome(1).addEvents(TestEvent("y"), TestEvent("z"))

          val actual =
            oa |+| ob

          val expected =
            Outcome(("count", 1)).addEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

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
            Outcome(10).addEvents(TestEvent("x")).apState(Outcome((i: Int) => i + 10))

          val expected: Outcome[Int] =
            Outcome(20).addEvents(TestEvent("x"))

          actual === expected ==> true
        }

        "map2" - {

          val oa = Outcome("count").addEvents(TestEvent("x"))
          val ob = Outcome(1).addEvents(TestEvent("y"), TestEvent("z"))

          val actual: Outcome[String] =
            (oa, ob).map2(t => t._1 + ": " + t._2)

          val expected: Outcome[String] =
            Outcome("count: 1").addEvents(TestEvent("x"), TestEvent("y"), TestEvent("z"))

          actual === expected ==> true

        }

      }

    }

}
