package indigo.shared.animation.timeline

import indigo.shared.collections.Batch
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds
import indigo.syntax.*

class TimeSlotTests extends munit.FunSuite {

  import TimeSlot.*

  val f = (i: Int) => SignalFunction((_: Seconds) => i)

  test("toWindows") {
    val slot: TimeSlot[Int] = pause(2.seconds) andThen
      animate(5.seconds)(f) andThen
      animate(3.seconds, f)

    val actual =
      slot.toWindows

    val expected =
      Batch(
        TimeWindow(Seconds(2), Seconds(7), f),
        TimeWindow(Seconds(7), Seconds(10), f)
      )

    assert(actual == expected)
  }

}
