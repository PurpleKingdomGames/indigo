package indigoextras.temporal

import indigo.shared.time.Seconds

import utest._
import indigo.shared.time.Millis

object ValueOverTimeTests extends TestSuite {

  val tests: Tests =
    Tests {
      "The changeAmount function (Int)" - {

        "should be able to calculate the amount of change at 10fps" - {

          val vot: ValueOverTime[Int] =
            ValueOverTime.intValueOverTime

          vot.changeAmount(Millis(100).toSeconds, 10, Seconds(0)) ==> 1
          vot.changeAmount(Millis(200).toSeconds, 10, Seconds(0)) ==> 2
          vot.changeAmount(Millis(300).toSeconds, 10, Seconds(0)) ==> 3
          vot.changeAmount(Millis(400).toSeconds, 10, Seconds(0)) ==> 4
          vot.changeAmount(Millis(500).toSeconds, 10, Seconds(0)) ==> 5

        }

        "should be able to calculate the amount of change" - {

          val vot: ValueOverTime[Int] =
            ValueOverTime.intValueOverTime

          vot.changeAmount(Millis(33).toSeconds, 10, Seconds(0)) ==> 0
          vot.changeAmount(Millis(33 * 4).toSeconds, 10, Seconds(0)) ==> 1
          vot.changeAmount(Millis(17 * 30).toSeconds, 10, Seconds(0)) ==> 5
          vot.changeAmount(Millis(33 * 31).toSeconds, 10, Seconds(0)) ==> 10

        }

        "should produce a changing value from a consistently small delta" - {

          val vot: ValueOverTime[Int] =
            ValueOverTime.intValueOverTime

          // Almost perfect 30fps for 30 frames @ 10 units per second should equal 10 units completed
          val actual: List[Int] = (1 to 30).toList
            .map(_.toDouble * 33.3)
            .map { runningTime =>
              vot.changeAmount(Millis(runningTime.toLong).toSeconds, 10, Seconds(0))
            }
            .distinct

          val expected: List[Int] = (0 to 9).toList

          actual ==> expected

        }

      }

      "The changeAmount function (Float)" - {

        "should be able to calculate the amount of change at 10fps" - {

          val vot: ValueOverTime[Float] =
            ValueOverTime.floatValueOverTime

          vot.changeAmount(Millis(100).toSeconds, 10f, Seconds(0)) ==> 1f
          vot.changeAmount(Millis(200).toSeconds, 10f, Seconds(0)) ==> 2f
          vot.changeAmount(Millis(300).toSeconds, 10f, Seconds(0)) ==> 3f
          vot.changeAmount(Millis(400).toSeconds, 10f, Seconds(0)) ==> 4f
          vot.changeAmount(Millis(500).toSeconds, 10f, Seconds(0)) ==> 5f

        }

      }

      "The changeAmount function (Double)" - {

        "should be able to calculate the amount of change at 10fps" - {

          val vot: ValueOverTime[Double] =
            ValueOverTime.doubleValueOverTime

          vot.changeAmount(Seconds(0.1), 10d, Seconds(0)) ==> 1d
          vot.changeAmount(Seconds(0.2), 10d, Seconds(0)) ==> 2d
          vot.changeAmount(Seconds(0.3), 10d, Seconds(0)) ==> 3d
          vot.changeAmount(Seconds(0.4), 10d, Seconds(0)) ==> 4d
          vot.changeAmount(Seconds(0.5), 10d, Seconds(0)) ==> 5d

        }

      }
    }

}
