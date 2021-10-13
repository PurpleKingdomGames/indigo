package indigo.shared.temporal

import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds

class SignalTests extends munit.FunSuite {

  test("Signals should be able to get a fixed value from a signal") {
    assertEquals(Signal.fixed("a").at(Seconds(100)), "a")
  }

  test("Signals should be able to get a value over time from a signal") {
    val sig = Signal(t => t.toInt * 10)

    (0 to 10).foreach { t =>
      assertEquals(sig.at(Seconds(t.toDouble)), t * 10)
    }
  }

  test("Signals should be able to merge signals") {
    val a = Signal.fixed(1)
    val b = Signal.fixed(2)

    //Alternative to applicative syntax
    assertEquals(a.merge(b)(_ + _).at(Seconds.zero), 3)
  }

  test("Signals should be able to flatMap Signals") {

    val a = Signal.fixed(10)

    assertEquals(a.flatMap(i => Signal.fixed(i * 2)).at(Seconds.zero), 20)
  }

  test("Signals should be able to flatMap Signals in a for comp") {

    val res =
      for {
        a <- Signal.fixed(10)
        b <- Signal.fixed(20)
        c <- Signal.fixed(30)
      } yield a + b + c

    assertEquals(res.at(Seconds.zero), 60)

  }

  test("Signals Pulse signal") {

    val pulse = Signal.Pulse(Seconds(10))

    assertEquals(pulse.at(Seconds(0)), true)
    assertEquals(pulse.at(Seconds(1)), true)
    assertEquals(pulse.at(Seconds(10)), false)
    assertEquals(pulse.at(Seconds(11)), false)
    assertEquals(pulse.at(Seconds(20)), true)
    assertEquals(pulse.at(Seconds(23)), true)
    assertEquals(pulse.at(Seconds(1234)), false)
    assertEquals(pulse.at(Seconds(1243)), true)

  }

  /*
So eventually I'd like a test where you take 3 signals, merge them and prove their bahviour changes like:
Signal(Math.sin + distance)
Signal(math.cos + distance)
Signal(always emits the cut off point)
Signal(TemporalPredicate.until(t >= cut off))

...which is a signal function of Signal[t, a -> Boolean]

Where a thing moves in a circle for 2 seconds and then stops.
   */

  test("Moving in a circle") {

    val distance: Signal[Double] =
      Signal.fixed(10)

    val timeToRadians: Signal[Double] =
      Signal(t => (Math.PI * 2) * (1d / 1000d) * (t.toDouble % 1000d))

    val input: Signal[(Double, Double)] =
      timeToRadians |*| distance

    val xSignal: SignalFunction[(Double, Double), Int] =
      SignalFunction((t: (Double, Double)) => (Math.sin(t._1) * t._2).toInt)

    val ySignal: SignalFunction[(Double, Double), Int] =
      SignalFunction((t: (Double, Double)) => (Math.cos(t._1) * t._2).toInt)

    val positionSF: SignalFunction[(Double, Double), (Int, Int)] =
      xSignal &&& ySignal

    val positionSignal: Signal[(Int, Int)] =
      input |> positionSF

    assertEquals(positionSignal.at(Seconds.zero), (0, 10))
    assertEquals(positionSignal.at(Seconds(250)), (10, 0))
    assertEquals(positionSignal.at(Seconds(500)), (0, -10))
    assertEquals(positionSignal.at(Seconds(750)), (-10, 0))
    assertEquals(positionSignal.at(Seconds(1000)), (0, 10))

  }

  test("Moving and then stopping after a certain time") {

    final case class Conditions(xPos: Int, velocity: Int, creationTime: Seconds, stopAfter: Seconds)

    val conditions = Conditions(100, 10, Seconds.zero, Seconds(20000))

    val timeAndConditions: Signal[(Seconds, Conditions)] =
      Signal.Time |*| Signal.fixed(conditions)

    val timeShift: SignalFunction[(Seconds, Conditions), (Seconds, Conditions)] =
      SignalFunction(t => (t._1 - t._2.creationTime, t._2))

    val timeStop: SignalFunction[(Seconds, Conditions), (Seconds, Conditions)] =
      SignalFunction(t => if (t._1 >= t._2.stopAfter) (t._2.stopAfter, t._2) else t)

    val timeToSeconds: SignalFunction[(Seconds, Conditions), (Double, Conditions)] =
      SignalFunction(t => (t._1.toDouble * 0.001d, t._2))

    val positionX: SignalFunction[(Double, Conditions), Int] =
      SignalFunction(t => t._2.xPos + (t._1 * t._2.velocity).toInt)

    val signalPipeline: SignalFunction[(Seconds, Conditions), Int] =
      timeShift >>> timeStop >>> timeToSeconds >>> positionX

    val signal: Signal[Int] =
      timeAndConditions |> signalPipeline

    // Sanity check, basic signal should adance position over time
    (0 to 10).toList.foreach { i =>
      assertEquals(signal.at(Seconds(i.toDouble * 1000d)), conditions.xPos + (conditions.velocity * i).toInt)
    }

    assertEquals(signal.at(Seconds(30000)), signal.at(Seconds(20000)))

  }

  def round(d: Double): Double =
    Math.floor(d * 100d) / 100d

  test("Utils.Lerp (linear interpolation)") {
    // X
    assertEquals(Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(-0.5)), Point(60, 10))
    assertEquals(Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(0.0)), Point(60, 10))
    assertEquals(Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(0.5)), Point(35, 10))
    assertEquals(Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(1.0)), Point(10, 10))
    assertEquals(Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(2.0)), Point(10, 10))
    // Y
    assertEquals(Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(1)).at(Seconds(0.0)), Point(10, 10))
    assertEquals(Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(1)).at(Seconds(0.5)), Point(10, 35))
    assertEquals(Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(1)).at(Seconds(1.0)), Point(10, 60))
    // X,Y
    assertEquals(Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(1)).at(Seconds(0.0)), Point(10, 10))
    assertEquals(Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(1)).at(Seconds(0.5)), Point(35, 35))
    assertEquals(Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(1)).at(Seconds(1.0)), Point(60, 60))

    // Over more than a second...
    assertEquals(Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(10)).at(Seconds(10.0)), Point(10, 10))
    assertEquals(Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(10)).at(Seconds(10.0)), Point(10, 60))

    assertEquals(Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(10)).at(Seconds(0.0)), Point(10, 10))
    assertEquals(Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(10)).at(Seconds(5.0)), Point(35, 35))
    assertEquals(Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(10)).at(Seconds(10.0)), Point(60, 60))
  }

  test("Utils.Linear") {
    assertEquals(Signal.Linear(Seconds(1)).at(Seconds(-0.5)), 0.0)
    assertEquals(Signal.Linear(Seconds(1)).at(Seconds(0.0)), 0.0)
    assertEquals(Signal.Linear(Seconds(1)).at(Seconds(0.5)), 0.5)
    assertEquals(Signal.Linear(Seconds(1)).at(Seconds(1.0)), 1.0)
    assertEquals(Signal.Linear(Seconds(1)).at(Seconds(2.0)), 1.0)

    assertEquals(Signal.Linear(Seconds(10)).at(Seconds(0.0)), 0.0)
    assertEquals(Signal.Linear(Seconds(10)).at(Seconds(5.0)), 0.5)
    assertEquals(Signal.Linear(Seconds(10)).at(Seconds(10.0)), 1.0)
  }

  test("Utils.SmoothPulse smoothly interpolates from 0 to 1") {
    assertEquals(round(Signal.SmoothPulse.at(Seconds(0))), 0.0)
    assertEquals(round(Signal.SmoothPulse.at(Seconds(0.25))), 0.5)
    assertEquals(round(Signal.SmoothPulse.at(Seconds(0.5))), 1.0)
    assertEquals(round(Signal.SmoothPulse.at(Seconds(0.75))), 0.5)
    assertEquals(round(Signal.SmoothPulse.at(Seconds(1))), 0.0)
    assertEquals(round(Signal.SmoothPulse.at(Seconds(1.5))), 1.0)
    assertEquals(round(Signal.SmoothPulse.at(Seconds(2))), 0.0)
  }

  test("Utils.Pulse produces a signal of true and false values") {

    val pulse = Signal.Pulse(Seconds(10))

    assertEquals(pulse.at(Seconds(0)), true)
    assertEquals(pulse.at(Seconds(5)), true)
    assertEquals(pulse.at(Seconds(10)), false)
    assertEquals(pulse.at(Seconds(15)), false)
    assertEquals(pulse.at(Seconds(20)), true)

  }

  test("Utils.clamping time limits the time so that any time passed can be valid") {

    val days =
      List(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
      )

    val daysOfTheWeek =
      Signal { t =>
        // Clearly this will blow up for < 0 or > 6
        days(t.toInt)
      }

    val clamped =
      daysOfTheWeek.clampTime(Seconds(0), Seconds(6))

    assertEquals(clamped.at(Seconds(-100)), days(0))
    assertEquals(clamped.at(Seconds(-1)), days(0))
    assertEquals(clamped.at(Seconds(0)), days(0))
    assertEquals(clamped.at(Seconds(1)), days(1))
    assertEquals(clamped.at(Seconds(2)), days(2))
    assertEquals(clamped.at(Seconds(3)), days(3))
    assertEquals(clamped.at(Seconds(4)), days(4))
    assertEquals(clamped.at(Seconds(5)), days(5))
    assertEquals(clamped.at(Seconds(6)), days(6))
    assertEquals(clamped.at(Seconds(7)), days(6))
    assertEquals(clamped.at(Seconds(50)), days(6))
    assertEquals(clamped.at(Seconds(1000)), days(6))
  }

  test("Utils.wrapping time keeps time looping round a fixed point") {

    val days =
      List(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
      )

    val daysOfTheWeek =
      Signal { t =>
        // Clearly this will blow up for < 0 or > 6
        days(t.toInt)
      }

    val wrapped =
      daysOfTheWeek
        .clampTime(Seconds(0), Seconds(1000)) // So now we can't be less than 0, the 1000 would still break
        .wrapTime(Seconds(7))                 // ...but we wrap at day 7

    assertEquals(wrapped.at(Seconds(0)), days(0))
    assertEquals(wrapped.at(Seconds(1)), days(1))
    assertEquals(wrapped.at(Seconds(2)), days(2))
    assertEquals(wrapped.at(Seconds(3)), days(3))
    assertEquals(wrapped.at(Seconds(4)), days(4))
    assertEquals(wrapped.at(Seconds(5)), days(5))
    assertEquals(wrapped.at(Seconds(6)), days(6))
    assertEquals(wrapped.at(Seconds(7)), days(0))

  }

  test("Utils.affect time") {

    val double = Signal.Time.affectTime(2.0d)
    val half   = Signal.Time.affectTime(0.5d)

    val times: List[Seconds] =
      (1 to 10).map(_ * 10).map(_.toDouble).toList.map(s => Seconds(s))

    times.foreach { t =>
      assertEquals(double.at(t), t * Seconds(2L))
      assertEquals(half.at(t), t / Seconds(2L))
    }

  }

  def simplify(d: Double): Int =
    (d * 100).toInt

  val times: List[Seconds] =
    (0 to 10).toList.map(i => Seconds(i.toDouble))

  test("Utils.Easing.Easing in out") {
    val signal =
      Signal.EaseInOut(Seconds(10))

    val actual =
      times.map(t => simplify(signal.at(t)))

    val expected =
      List(0, 2, 9, 20, 34, 50, 65, 79, 90, 97, 100)

    assertEquals(actual, expected)
  }

  test("Utils.Easing.Easing in") {
    val signal =
      Signal.EaseIn(Seconds(10))

    val actual =
      times.map(t => simplify(signal.at(t)))

    val expected =
      List(0, 1, 4, 10, 19, 29, 41, 54, 69, 84, 100)

    assertEquals(actual, expected)
  }

  test("Utils.Easing.Easing out") {
    val signal =
      Signal.EaseOut(Seconds(10))

    val actual =
      times.map(t => simplify(signal.at(t)))

    val expected =
      List(0, 15, 30, 45, 58, 70, 80, 89, 95, 98, 100)

    assertEquals(actual, expected)
  }

}
