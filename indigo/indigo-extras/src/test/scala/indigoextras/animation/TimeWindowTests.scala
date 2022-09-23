package indigoextras.animation

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds
import indigo.syntax.*

class TimeWindowTests extends munit.FunSuite {

  test("within") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val ts = TimeWindow(1.seconds, 3.seconds, f)

    assert(!ts.within(0.seconds))
    assert(ts.within(1.seconds))
    assert(ts.within(2.seconds))
    assert(ts.within(3.seconds))
    assert(!ts.within(4.seconds))
    assert(!ts.within(5.seconds))
  }

}
