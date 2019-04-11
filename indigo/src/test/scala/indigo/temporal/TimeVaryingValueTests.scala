package indigo.temporal

import utest._

import indigo.time.Millis
import indigo.shared.EqualTo._

object TimeVaryingValueTests extends TestSuite {

  val millis0: Millis   = Millis(0)
  val millis100: Millis = Millis(100)

  val tests: Tests =
    Tests {
      "increasing" - {

        "should increase one value over time." - {
          TimeVaryingValue(0, millis0).increase(10, millis100).value ==> 1
        }

        "should do a number of iterations over time" - {
          val runningTimes: List[Millis] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r))

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.increase(10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(10, 0, Millis(0))

          actual === expected ==> true
        }

      }

      "increasing capped" - {

        "should increase one value over time." - {
          TimeVaryingValue(0, millis0).increaseTo(100, 10, Millis((33.3 * 4).toLong)).value ==> 1
          TimeVaryingValue(0, millis0).increaseTo(100, 10, Millis(50000)).value ==> 100
        }

        "should do a number of iterations over time up to a limit" - {
          val runningTimes: List[Millis] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r))

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.increaseTo(5, 10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(5, 0, Millis(0))

          actual === expected ==> true
        }

      }

      "increasing wrapped" - {

        "should increase one value over time." - {
          "Case A" - {
            TimeVaryingValue(0, millis0)
              .increaseWrapAt(3, 10, Millis(100))
              .value ==> 1
          }

          "Case B" - {
            TimeVaryingValue(0, millis0)
              .increaseWrapAt(3, 10, Millis(100))
              .increaseWrapAt(3, 10, Millis(200))
              .value ==> 2
          }

          "Case C" - {
            TimeVaryingValue(0, millis0)
              .increaseWrapAt(3, 10, Millis(100))
              .increaseWrapAt(3, 10, Millis(200))
              .increaseWrapAt(3, 10, Millis(300))
              .value ==> 3
          }

          "Case D" - {
            TimeVaryingValue(0, millis0)
              .increaseWrapAt(3, 10, Millis(100))
              .increaseWrapAt(3, 10, Millis(200))
              .increaseWrapAt(3, 10, Millis(300))
              .increaseWrapAt(3, 10, Millis(400))
              .value ==> 0
          }

          "Case E" - {
            TimeVaryingValue(0, millis0)
              .increaseWrapAt(3, 10, Millis(100))
              .increaseWrapAt(3, 10, Millis(200))
              .increaseWrapAt(3, 10, Millis(300))
              .increaseWrapAt(3, 10, Millis(400))
              .increaseWrapAt(3, 10, Millis(500))
              .value ==> 1
          }
        }
      }

      "decreasing" - {

        "should decrease one value over time." - {
          TimeVaryingValue(0, millis0).decrease(10, millis100).value ==> -1
        }

        "should do a number of iterations over time" - {
          val runningTimes: List[Millis] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r))

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.decrease(10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(-10, 0, Millis(0))

          actual === expected ==> true
        }

      }

      "decreasing capped" - {

        "should do a number of iterations over time down to a limit" - {
          val runningTimes: List[Millis] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r))

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0, millis0))((tv, rt) => tv.decreaseTo(-5, 10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(-5, 0, Millis(0))

          actual === expected ==> true
        }

      }

      "decreasing wrapped" - {

        "should decrease one value over time." - {
          "Case A" - {
            TimeVaryingValue(0, millis0)
              .decreaseWrapAt(3, 10, Millis(100))
              .value ==> -1
          }

          "Case B" - {
            TimeVaryingValue(0, millis0)
              .decreaseWrapAt(3, 10, Millis(100))
              .decreaseWrapAt(3, 10, Millis(200))
              .value ==> -2
          }

          "Case C" - {
            TimeVaryingValue(0, millis0)
              .decreaseWrapAt(3, 10, Millis(100))
              .decreaseWrapAt(3, 10, Millis(200))
              .decreaseWrapAt(3, 10, Millis(300))
              .value ==> -3
          }

          "Case D" - {
            TimeVaryingValue(0, millis0)
              .decreaseWrapAt(3, 10, Millis(100))
              .decreaseWrapAt(3, 10, Millis(200))
              .decreaseWrapAt(3, 10, Millis(300))
              .decreaseWrapAt(3, 10, Millis(400))
              .value ==> 0
          }

          "Case E" - {
            TimeVaryingValue(0, millis0)
              .decreaseWrapAt(3, 10, Millis(100))
              .decreaseWrapAt(3, 10, Millis(200))
              .decreaseWrapAt(3, 10, Millis(300))
              .decreaseWrapAt(3, 10, Millis(400))
              .decreaseWrapAt(3, 10, Millis(500))
              .value ==> -1
          }
        }

      }
    }

}
