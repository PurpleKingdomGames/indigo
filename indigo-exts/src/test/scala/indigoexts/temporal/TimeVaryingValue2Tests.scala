package indigoexts.temporal

import utest._
import indigo.GameTime.Millis

object TimeVaryingValue2 extends TestSuite {

  val tests: Tests =
    Tests {

      /*
So eventually I'd like a test where you take 3 signals, merge them and prove their bahviour changes like:
Signal(Math.sin + distance)
Signal(math.cos + distance)
Signal(always emits the cut off point)
Signal(TemporalPredicate.until(t >= cut off))

...which is a signal function of Signal[t, a -> Boolean]

Where a thing moves in a circle for 2 seconds and then stops.
       */
      "experiment" - {

        val distance = 10

        val sin    = Signal.create(t => Math.sin(t.toDouble) * distance)
        val cos    = Signal.create(t => Math.cos(t.toDouble) * distance)
        val cutOff = Signal.fixed(Millis(2000))

      }

    }

}
