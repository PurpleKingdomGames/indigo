package indigoextras.animation

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds
import indigo.syntax.*

class TimeWindowTests extends munit.FunSuite {

  test("length") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assert(tw.length == 3.seconds)
  }
  
  test("totalTime") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assert(tw.totalTime == 2.seconds)
  }

  test("within") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assert(!tw.within(0.seconds))
    assert(tw.within(1.seconds))
    assert(tw.within(2.seconds))
    assert(tw.within(3.seconds))
    assert(!tw.within(4.seconds))
    assert(!tw.within(5.seconds))
  }
  
  test("withStart") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assert(tw.withStart(0.seconds).start == 0.seconds)
  }
  
  test("withEnd") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assert(tw.withEnd(10.seconds).end == 10.seconds)
  }
  
  test("withModifier") {
    val f1  = (i: Int) => SignalFunction((_: Seconds) => i)
    val f2  = (i: Int) => SignalFunction((_: Seconds) => 10)
    val tw1 = TimeWindow(1.seconds, 3.seconds, f1)
    val tw2 = TimeWindow(1.seconds, 3.seconds, f2)

    assertEquals(Timeline(tw1).at(1.second)(1), Some(1))
    assertEquals(Timeline(tw2).at(1.second)(1), Some(10))
  }
  
  test("contractBy") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assertEquals(tw.contractBy(1.seconds).end, 2.seconds)
  }
  
  test("expandBy") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assertEquals(tw.expandBy(3.seconds).end, 6.seconds)
  }
  
  test("multiply") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assertEquals(tw.multiply(2.0).start, 2.0.seconds)
    assertEquals(tw.multiply(2.0).end, 6.0.seconds)
  }
  
  test("shiftBy") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assertEquals(tw.shiftBy(10.seconds).start, 11.seconds)
    assertEquals(tw.shiftBy(10.seconds).end, 13.seconds)
  }
  
  test("trim") {
    val f  = (i: Int) => SignalFunction((_: Seconds) => i)
    val tw = TimeWindow(1.seconds, 3.seconds, f)

    assert(tw.start == 1.second)
    assert(tw.trim.start == 0.seconds)
    assert(tw.trim.end == 2.seconds)
  }

}
