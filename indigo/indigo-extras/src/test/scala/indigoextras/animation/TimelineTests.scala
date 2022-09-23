package indigoextras.animation

import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.GlobalEvent
import indigo.shared.temporal.Signal
import indigo.shared.temporal.SignalFunction
import indigo.shared.time.Seconds
import indigo.syntax.*

class TimelineTests extends munit.FunSuite {

  test("check the core types work (no sugar)") {

    val startingValue: Int = 10

    val f1 = (a: Int) => SignalFunction((t: Seconds) => a * t.toInt)
    val f2 = (a: Int) => SignalFunction((t: Seconds) => a * (t.toInt * 2))
    val f3 = (a: Int) => SignalFunction((_: Seconds) => a + 10)

    assert((Signal.Time |> f1(startingValue)).at(0.seconds) == 0)
    assert((Signal.Time |> f1(startingValue)).at(1.seconds) == 10)
    assert((Signal.Time |> f1(startingValue)).at(2.seconds) == 20)
    assert((Signal.Time |> f2(startingValue)).at(0.seconds) == 0)
    assert((Signal.Time |> f2(startingValue)).at(1.seconds) == 20)
    assert((Signal.Time |> f2(startingValue)).at(2.seconds) == 40)
    assert((Signal.Time |> f3(startingValue)).at(0.seconds) == 20)
    assert((Signal.Time |> f3(startingValue)).at(1.seconds) == 20)
    assert((Signal.Time |> f3(startingValue)).at(2.seconds) == 20)

    val windows = Batch(
      TimeWindow(1.seconds, 3.seconds, f1),
      TimeWindow(4.seconds, 6.seconds, f2),
      TimeWindow(5.seconds, 6.seconds, f3)
    )

    val tl = Timeline(windows)

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

  test("How to emit an event.") {

    case object MyTestEvent extends GlobalEvent

    val f: Outcome[Int] => SignalFunction[Seconds, Outcome[Int]] = i =>
      SignalFunction { (t: Seconds) =>
        if t < 1.5.seconds then i
        else i.addGlobalEvents(MyTestEvent)
      }

    val tw = TimeWindow[Outcome[Int]](1.seconds, 3.seconds, f)

    assertEquals(Timeline(tw).at(1.second)(Outcome(1)), Some(Outcome(1)))
    assertEquals(Timeline(tw).at(3.seconds)(Outcome(2)), Some(Outcome(2, Batch(MyTestEvent))))
  }

}
