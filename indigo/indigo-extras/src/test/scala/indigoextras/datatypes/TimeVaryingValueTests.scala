package indigoextras.datatypes

import utest._

import indigo.shared.time.{Seconds, Millis}
import indigo.shared.EqualTo._

object TimeVaryingValueTests extends TestSuite {

  val tests: Tests =
    Tests {
      "increasing" - {

        "should increase one value over time." - {
          Increasing(0, 10).update(Seconds(0.1)).value ==> 1
        }

        "should do a number of iterations over time" - {
          val increments: List[Seconds] =
            (1 to 10).toList.map(r => Millis(100).toSeconds)

          val actual: TimeVaryingValue =
            increments.foldLeft(Increasing(0, 10))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            Increasing(10, 10)

          actual ==> expected
        }

      }

      "increasing capped" - {

        "should increase one value over time." - {
          IncreaseTo(0, 10, 100).update(Millis((33.3 * 4).toLong).toSeconds).toInt ==> 1
          IncreaseTo(0, 10, 100).update(Millis(50000).toSeconds).value ==> 100
        }

        "should do a number of iterations over time up to a limit" - {
          val increments: List[Seconds] =
            (1 to 11).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: IncreaseTo =
            increments.foldLeft(IncreaseTo(0, 10, 5))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            IncreaseTo(5, 10, 5)

          actual ==> expected
        }

      }

      "increasing wrapped" - {

        "should increase one value over time." - {
          "Case A" - {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .value ==> 1
          }

          "Case B" - {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              .value ==> 2
          }

          "Case C" - {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              .update(Millis(100).toSeconds) // 3
              .value ==> 3
          }

          "Case D" - {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              .update(Millis(100).toSeconds) // 3
              .update(Millis(100).toSeconds) // 0
              .value ==> 0
          }

          "Case E" - {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              .update(Millis(100).toSeconds) // 3
              .update(Millis(100).toSeconds) // 0
              .update(Millis(100).toSeconds) // 1
              .value ==> 1
          }
        }
      }

      "decreasing" - {

        "should decrease one value over time." - {
          Decreasing(0, 10).update(Seconds(0.1)).value ==> -1
        }

        "should do a number of iterations over time" - {
          val increments: List[Seconds] =
            (1 to 10).toList.map(_ => Millis(100).toSeconds)

          val actual: TimeVaryingValue =
            increments.foldLeft(Decreasing(0, 10))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            Decreasing(-10, 10)

          actual ==> expected
        }

      }

      "decreasing capped" - {

        "should do a number of iterations over time down to a limit" - {
          val increments: List[Seconds] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: TimeVaryingValue =
            increments.foldLeft(DecreaseTo(0, 10, -5))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            new DecreaseTo(-5, 10, -5)

          actual ==> expected
        }

      }

      "decreasing wrapped" - {

        "should decrease one value over time." - {
          "Case A" - {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .value ==> -1
          }

          "Case B" - {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .value ==> -2
          }

          "Case C" - {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .value ==> -3
          }

          "Case D" - {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .value ==> 0
          }

          "Case E" - {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .value ==> -1
          }
        }

      }
    }

}
