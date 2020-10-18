package indigoextras.datatypes

import utest._

import indigo.shared.time.{Seconds, Millis}
import indigo.shared.EqualTo._

object TimeVaryingValueTests extends TestSuite {

  val tests: Tests =
    Tests {
      "increasing" - {

        "should increase one value over time." - {
          TimeVaryingValue(0).increase(10, Seconds(0.1)).value ==> 1
        }

        "should do a number of iterations over time" - {
          val runningTimes: List[Seconds] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0))((tv, rt) => tv.increase(10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(10, 0)

          actual === expected ==> true
        }

      }

      "increasing capped" - {

        "should increase one value over time." - {
          TimeVaryingValue(0).increaseTo(100, 10, Millis((33.3 * 4).toLong).toSeconds).value ==> 1
          TimeVaryingValue(0).increaseTo(100, 10, Millis(50000).toSeconds).value ==> 100
        }

        "should do a number of iterations over time up to a limit" - {
          val runningTimes: List[Seconds] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0))((tv, rt) => tv.increaseTo(5, 10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(5, 0)

          actual === expected ==> true
        }

      }

      "increasing wrapped" - {

        "should increase one value over time." - {
          "Case A" - {
            TimeVaryingValue(0)
              .increaseWrapAt(3, 10, Millis(100).toSeconds)
              .value ==> 1
          }

          "Case B" - {
            TimeVaryingValue(0)
              .increaseWrapAt(3, 10, Millis(100).toSeconds)
              .increaseWrapAt(3, 10, Millis(200).toSeconds)
              .value ==> 2
          }

          "Case C" - {
            TimeVaryingValue(0)
              .increaseWrapAt(3, 10, Millis(100).toSeconds)
              .increaseWrapAt(3, 10, Millis(200).toSeconds)
              .increaseWrapAt(3, 10, Millis(300).toSeconds)
              .value ==> 3
          }

          "Case D" - {
            TimeVaryingValue(0)
              .increaseWrapAt(3, 10, Millis(100).toSeconds)
              .increaseWrapAt(3, 10, Millis(200).toSeconds)
              .increaseWrapAt(3, 10, Millis(300).toSeconds)
              .increaseWrapAt(3, 10, Millis(400).toSeconds)
              .value ==> 0
          }

          "Case E" - {
            TimeVaryingValue(0)
              .increaseWrapAt(3, 10, Millis(100).toSeconds)
              .increaseWrapAt(3, 10, Millis(200).toSeconds)
              .increaseWrapAt(3, 10, Millis(300).toSeconds)
              .increaseWrapAt(3, 10, Millis(400).toSeconds)
              .increaseWrapAt(3, 10, Millis(500).toSeconds)
              .value ==> 1
          }
        }
      }

      "decreasing" - {

        "should decrease one value over time." - {
          TimeVaryingValue(0).decrease(10, Seconds(0.1)).value ==> -1
        }

        "should do a number of iterations over time" - {
          val runningTimes: List[Seconds] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0))((tv, rt) => tv.decrease(10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(-10, 0)

          actual === expected ==> true
        }

      }

      "decreasing capped" - {

        "should do a number of iterations over time down to a limit" - {
          val runningTimes: List[Seconds] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: TimeVaryingValue[Int] =
            runningTimes.foldLeft(TimeVaryingValue(0))((tv, rt) => tv.decreaseTo(-5, 10, rt))

          val expected: TimeVaryingValue[Int] =
            new TimeVaryingValue(-5, 0)

          actual === expected ==> true
        }

      }

      "decreasing wrapped" - {

        "should decrease one value over time." - {
          "Case A" - {
            TimeVaryingValue(0)
              .decreaseWrapAt(3, 10, Millis(100).toSeconds)
              .value ==> -1
          }

          "Case B" - {
            TimeVaryingValue(0)
              .decreaseWrapAt(3, 10, Millis(100).toSeconds)
              .decreaseWrapAt(3, 10, Millis(200).toSeconds)
              .value ==> -2
          }

          "Case C" - {
            TimeVaryingValue(0)
              .decreaseWrapAt(3, 10, Millis(100).toSeconds)
              .decreaseWrapAt(3, 10, Millis(200).toSeconds)
              .decreaseWrapAt(3, 10, Millis(300).toSeconds)
              .value ==> -3
          }

          "Case D" - {
            TimeVaryingValue(0)
              .decreaseWrapAt(3, 10, Millis(100).toSeconds)
              .decreaseWrapAt(3, 10, Millis(200).toSeconds)
              .decreaseWrapAt(3, 10, Millis(300).toSeconds)
              .decreaseWrapAt(3, 10, Millis(400).toSeconds)
              .value ==> 0
          }

          "Case E" - {
            TimeVaryingValue(0)
              .decreaseWrapAt(3, 10, Millis(100).toSeconds)
              .decreaseWrapAt(3, 10, Millis(200).toSeconds)
              .decreaseWrapAt(3, 10, Millis(300).toSeconds)
              .decreaseWrapAt(3, 10, Millis(400).toSeconds)
              .decreaseWrapAt(3, 10, Millis(500).toSeconds)
              .value ==> -1
          }
        }

      }
    }

}
