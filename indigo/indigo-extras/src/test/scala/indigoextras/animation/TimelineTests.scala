package indigoextras.animation

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
import indigo.syntax.*

class TimelineTests extends munit.FunSuite {

  test("Timeslot within") {
    val f  = (i: Int) => Signal(_ => i)
    val ts = TimeSlot(1.seconds, 3.seconds, f)

    assert(!ts.within(0.seconds))
    assert(ts.within(1.seconds))
    assert(ts.within(2.seconds))
    assert(ts.within(3.seconds))
    assert(!ts.within(4.seconds))
    assert(!ts.within(5.seconds))
  }

  test("check the core types work (no sugar)") {

    val startingValue: Int = 10

    val f1 = (a: Int) => Signal(t => a * t.toInt)
    val f2 = (a: Int) => Signal(t => a * (t.toInt * 2))
    val f3 = (a: Int) => Signal(_ => a + 10)

    assert(f1(startingValue).at(0.seconds) == 0)
    assert(f1(startingValue).at(1.seconds) == 10)
    assert(f1(startingValue).at(2.seconds) == 20)

    assert(f2(startingValue).at(0.seconds) == 0)
    assert(f2(startingValue).at(1.seconds) == 20)
    assert(f2(startingValue).at(2.seconds) == 40)

    assert(f3(startingValue).at(0.seconds) == 20)
    assert(f3(startingValue).at(1.seconds) == 20)
    assert(f3(startingValue).at(2.seconds) == 20)

    val slots = Batch(
      TimeSlot(1.seconds, 3.seconds, f1),
      TimeSlot(4.seconds, 6.seconds, f2),
      TimeSlot(5.seconds, 6.seconds, f3)
    )

    val tl = Timeline(slots)

    val actual: List[Option[Int]] =
      List(
        tl.at(0.seconds)(startingValue),
        tl.at(1.seconds)(startingValue),
        tl.at(2.seconds)(startingValue),
        tl.at(3.seconds)(startingValue),
        tl.at(3.5.seconds)(startingValue),
        tl.at(4.seconds)(startingValue),
        tl.at(5.seconds)(startingValue),
        tl.at(6.seconds)(startingValue),
        tl.at(7.seconds)(startingValue)
      )

    val expected: List[Option[Int]] =
      List(
        None,     // tl.at(0.seconds)
        Some(0),  // tl.at(1.seconds)
        Some(10), // tl.at(2.seconds)
        Some(20), // tl.at(3.seconds)
        None,     // tl.at((3.5).seconds)
        Some(0),  // tl.at(4.seconds)
        Some(30), // tl.at(5.seconds)
        Some(50), // tl.at(6.seconds)
        None      // tl.at(7.seconds)
      )

    assertEquals(actual, expected)
  }

}
