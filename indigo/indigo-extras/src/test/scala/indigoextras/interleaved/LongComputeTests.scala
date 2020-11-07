package indigoextras.interleaved

import utest._
import indigo.shared.time.Seconds
import indigo.shared.time.GameTime
import indigo.shared.time.Millis

object LongComputeTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Long Compute" - {

        final case object TestStep extends MonitoredStep[Unit, Int] {
          val size = 10

          def perform(reference: Unit, current: Int): Int =
            current + 1
        }

        /*
         * Frame duration of 100 millis
         * No update to running time required, we only care about the delta.
         * The default tolerance is 0.2, meaning 0.2 of 100 millis / second i.e. the delta at most can be 20
         */
        val gameTime =
          GameTime.zero
            .setTargetFPS(10)
            .copy(delta = Millis(20).toSeconds)

        "initially, no steps will fit, so do the first one" - {
          val steps: List[TestStep.type] =
            List(TestStep, TestStep, TestStep)

          val longCompute =
            LongCompute(0, steps).withUnitRateOfChange(2)

          val actual =
            longCompute
              .update(gameTime)

          actual.steps ==> List(TestStep, TestStep)
          actual.result ==> 1
          actual.unitsToAttempt ==> 2
          actual.sizeCompleted ==> 10
          actual.isComplete ==> false
          actual.sizeRemaining ==> 20
          Math.floor(actual.portionCompleted * 100) / 100 ==> 0.33
        }

        "all the work units will fit, do all" - {
          val steps: List[TestStep.type] =
            List(TestStep, TestStep, TestStep)

          val longCompute =
            LongCompute(
              reference = (),
              result = 0,
              steps = steps,
              unitsToAttempt = 40,
              rateOfChange = 1,
              tolerance = 0.2,
              sizeCompleted = 0
            )

          val actual =
            longCompute.update(gameTime)

          actual.steps ==> Nil
          actual.result ==> 3
          actual.unitsToAttempt ==> 41
          actual.sizeCompleted ==> 30
          actual.isComplete ==> true
          actual.sizeRemaining ==> 0
          actual.portionCompleted ==> 1.0d
        }

        "half the work units fit, do over 2 frames" - {
          val steps: List[TestStep.type] =
            List(TestStep, TestStep, TestStep, TestStep)

          val longCompute =
            LongCompute(
              reference = (),
              result = 0,
              steps = steps,
              unitsToAttempt = 20,
              rateOfChange = 1,
              tolerance = 0.2,
              sizeCompleted = 0
            )

          val actual1 =
            longCompute.update(gameTime)

          actual1.steps ==> List(TestStep, TestStep)
          actual1.result ==> 2
          actual1.unitsToAttempt ==> 21
          actual1.sizeCompleted ==> 20
          actual1.isComplete ==> false
          actual1.sizeRemaining ==> 20
          Math.floor(actual1.portionCompleted * 100) / 100 ==> 0.5

          val actual2 =
            actual1.update(gameTime)

          actual2.steps ==> Nil
          actual2.result ==> 4
          actual2.unitsToAttempt ==> 22
          actual2.sizeCompleted ==> 40
          actual2.isComplete ==> true
          actual2.sizeRemaining ==> 0
          actual2.portionCompleted ==> 1.0

        }

        "isWithinTolerance" - {

          val gameTime =
            GameTime
              .is(Seconds(1))
              .setTargetFPS(10) // Frame duration of 100 millis

          // 10 FPS = 100 Millis / frame
          // So, tolerance 0.5d is 50 Millis
          LongCompute.isWithinTolerance(gameTime.copy(delta = Millis(0).toSeconds), 0.5) ==> true
          LongCompute.isWithinTolerance(gameTime.copy(delta = Millis(25).toSeconds), 0.5) ==> true
          LongCompute.isWithinTolerance(gameTime.copy(delta = Millis(50).toSeconds), 0.5) ==> true
          LongCompute.isWithinTolerance(gameTime.copy(delta = Millis(75).toSeconds), 0.5) ==> false
          LongCompute.isWithinTolerance(gameTime.copy(delta = Millis(100).toSeconds), 0.5) ==> false
          LongCompute.isWithinTolerance(gameTime.copy(delta = Millis(200).toSeconds), 0.5) ==> false

        }

      }

    }

}
