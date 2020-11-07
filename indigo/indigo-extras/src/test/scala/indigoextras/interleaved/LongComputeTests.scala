package indigoextras.interleaved

import utest._
import indigo.shared.time.Seconds
import indigo.shared.time.GameTime

object LongComputeTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Long Compute" - {

        final case object TestStep extends MonitoredStep[Unit, Int] {
          val size = 10

          def perform(reference: Unit, current: Int): Int =
            current + 1
        }

        val gameTime =
          GameTime.zero
            .setTargetFPS(10)           // Frame duration of 100 millis
            .copy(delta = Seconds(0.1)) // No update to running time required, we only care about the delta.

        "initially, no steps will fit, so ramp up to 10, then do the first one" - {
          val steps: List[TestStep.type] =
            List(TestStep, TestStep, TestStep)

          val longCompute =
            LongCompute(0, steps).withUnitRateOfChange(2)

          val actual =
            longCompute
              .update(gameTime)
              .update(gameTime)
              .update(gameTime)
              .update(gameTime)
              .update(gameTime)
              .update(gameTime)

          actual.steps ==> List(TestStep, TestStep)
          actual.result ==> 1
          actual.unitsToAttempt ==> 12
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
              tolerance = 0.1
            )

          val actual =
            longCompute.update(gameTime)

          actual.steps ==> Nil
          actual.result ==> 3
          actual.unitsToAttempt ==> 41
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
              tolerance = 0.1
            )

          val actual1 =
            longCompute.update(gameTime)

          actual1.steps ==> List(TestStep, TestStep)
          actual1.result ==> 2
          actual1.unitsToAttempt ==> 21

          val actual2 =
            actual1.update(gameTime)

          actual2.steps ==> Nil
          actual2.result ==> 4
          actual2.unitsToAttempt ==> 22

        }

      }

    }

}
