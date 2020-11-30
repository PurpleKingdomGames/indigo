package indigoextras.datatypes

import indigo.shared.time.{Seconds, Millis}
import indigo.shared.EqualTo._

class TimeVaryingValueTests extends munit.FunSuite {


      test("increasing") {

        test("should increase one value over time.") {
          assertEquals(Increasing(0, 10).update(Seconds(0.1)).value, 1)
        }

        test("should do a number of iterations over time") {
          val increments: List[Seconds] =
            (1 to 10).toList.map(r => Millis(100).toSeconds)

          val actual: TimeVaryingValue =
            increments.foldLeft(Increasing(0, 10))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            Increasing(10, 10)

          assertEquals(actual, expected)
        }

      }

      test("increasing capped") {

        test("should increase one value over time.") {
          assertEquals(IncreaseTo(0, 10, 100).update(Millis((33.3 * 4).toLong).toSeconds).toInt, 1)
          assertEquals(IncreaseTo(0, 10, 100).update(Millis(50000).toSeconds).value, 100)
        }

        test("should do a number of iterations over time up to a limit") {
          val increments: List[Seconds] =
            (1 to 11).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: IncreaseTo =
            increments.foldLeft(IncreaseTo(0, 10, 5))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            IncreaseTo(5, 10, 5)

          assertEquals(actual, expected)
        }

      }

      test("increasing wrapped") {

        test("should increase one value over time.") {
          test("Case A") {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              assertEquals(.value, 1)
          }

          test("Case B") {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              assertEquals(.value, 2)
          }

          test("Case C") {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              .update(Millis(100).toSeconds) // 3
              assertEquals(.value, 3)
          }

          test("Case D") {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              .update(Millis(100).toSeconds) // 3
              .update(Millis(100).toSeconds) // 0
              assertEquals(.value, 0)
          }

          test("Case E") {
            IncreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds) // 1
              .update(Millis(100).toSeconds) // 2
              .update(Millis(100).toSeconds) // 3
              .update(Millis(100).toSeconds) // 0
              .update(Millis(100).toSeconds) // 1
              assertEquals(.value, 1)
          }
        }
      }

      test("decreasing") {

        test("should decrease one value over time.") {
          assertEquals(Decreasing(0, 10).update(Seconds(0.1)).value, -1)
        }

        test("should do a number of iterations over time") {
          val increments: List[Seconds] =
            (1 to 10).toList.map(_ => Millis(100).toSeconds)

          val actual: TimeVaryingValue =
            increments.foldLeft(Decreasing(0, 10))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            Decreasing(-10, 10)

          assertEquals(actual, expected)
        }

      }

      test("decreasing capped") {

        test("should do a number of iterations over time down to a limit") {
          val increments: List[Seconds] =
            (1 to 10).toList.map(_ * 100).map(r => Millis(r).toSeconds)

          val actual: TimeVaryingValue =
            increments.foldLeft(DecreaseTo(0, 10, -5))((tv, rt) => tv.update(rt))

          val expected: TimeVaryingValue =
            new DecreaseTo(-5, 10, -5)

          assertEquals(actual, expected)
        }

      }

      test("decreasing wrapped") {

        test("should decrease one value over time.") {
          test("Case A") {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              assertEquals(.value, -1)
          }

          test("Case B") {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              assertEquals(.value, -2)
          }

          test("Case C") {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              assertEquals(.value, -3)
          }

          test("Case D") {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              assertEquals(.value, 0)
          }

          test("Case E") {
            DecreaseWrapAt(10, 3)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              .update(Millis(100).toSeconds)
              assertEquals(.value, -1)
          }
        }

      }
    }

}
