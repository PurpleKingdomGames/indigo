package indigo.shared.temporal

import utest._
import indigo.shared.time.GameTime
import indigo.shared.time.Seconds
import indigo.shared.datatypes.Point
import indigo.shared.time.Millis

object SignalTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Signals" - {
        "should be able to get a fixed value from a signal" - {
          Signal.fixed("a").at(Seconds(100)) ==> "a"
        }

        "should be able to get a value over time from a signal" - {
          val sig = Signal(t => t.toInt * 10)

          (0 to 10).foreach { t =>
            sig.at(Seconds(t)) ==> t * 10
          }
        }

        "should be able to merge signals" - {
          val a = Signal.fixed(1)
          val b = Signal.fixed(2)

          //Alternative to applicative syntax
          a.merge(b)(_ + _).at(Seconds.zero) ==> 3
        }

        "should be able to flatMap Signals" - {

          val a = Signal.fixed(10)

          a.flatMap(i => Signal.fixed(i * 2)).at(Seconds.zero) ==> 20
        }

        "should be able to flatMap Signals in a for comp" - {

          val res =
            for {
              a <- Signal.fixed(10)
              b <- Signal.fixed(20)
              c <- Signal.fixed(30)
            } yield a + b + c

          res.at(Seconds.zero) ==> 60

        }

        "Pulse signal" - {

          val pulse = Signal.Pulse(Seconds(10))

          pulse.at(Seconds(0)) ==> true
          pulse.at(Seconds(1)) ==> true
          pulse.at(Seconds(10)) ==> false
          pulse.at(Seconds(11)) ==> false
          pulse.at(Seconds(20)) ==> true
          pulse.at(Seconds(23)) ==> true
          pulse.at(Seconds(1234)) ==> false
          pulse.at(Seconds(1243)) ==> true

        }
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

      "Moving in a circle" - {

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

        positionSignal.at(Seconds.zero) ==> (0, 10)
        positionSignal.at(Seconds(250)) ==> (10, 0)
        positionSignal.at(Seconds(500)) ==> (0, -10)
        positionSignal.at(Seconds(750)) ==> (-10, 0)
        positionSignal.at(Seconds(1000)) ==> (0, 10)

      }

      "Moving and then stopping after a certain time" - {

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
          signal.at(Seconds(i * 1000)) ==> conditions.xPos + (conditions.velocity * i).toInt
        }

        signal.at(Seconds(30000)) ==> signal.at(Seconds(20000))

      }

      "Utils" - {

        def round(d: Double): Double =
          Math.floor(d * 100d) / 100d

        "Lerp (linear interpolation)" - {
          // X
          Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(0.0)) ==> Point(60, 10)
          Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(0.5)) ==> Point(35, 10)
          Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(1)).at(Seconds(1.0)) ==> Point(10, 10)
          // Y
          Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(1)).at(Seconds(0.0)) ==> Point(10, 10)
          Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(1)).at(Seconds(0.5)) ==> Point(10, 35)
          Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(1)).at(Seconds(1.0)) ==> Point(10, 60)
          // X,Y
          Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(1)).at(Seconds(0.0)) ==> Point(10, 10)
          Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(1)).at(Seconds(0.5)) ==> Point(35, 35)
          Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(1)).at(Seconds(1.0)) ==> Point(60, 60)

          // Over more than a second...
          Signal.Lerp(Point(60, 10), Point(10, 10), Seconds(10)).at(Seconds(10.0)) ==> Point(10, 10)
          Signal.Lerp(Point(10, 10), Point(10, 60), Seconds(10)).at(Seconds(10.0)) ==> Point(10, 60)

          Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(10)).at(Seconds(0.0)) ==> Point(10, 10)
          Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(10)).at(Seconds(5.0)) ==> Point(35, 35)
          Signal.Lerp(Point(10, 10), Point(60, 60), Seconds(10)).at(Seconds(10.0)) ==> Point(60, 60)
        }

        "SmoothPulse smoothly interpolates from 0 to 1" - {
          round(Signal.SmoothPulse.at(Seconds(0))) ==> 0
          round(Signal.SmoothPulse.at(Seconds(0.25))) ==> 0.5
          round(Signal.SmoothPulse.at(Seconds(0.5))) ==> 1
          round(Signal.SmoothPulse.at(Seconds(0.75))) ==> 0.5
          round(Signal.SmoothPulse.at(Seconds(1))) ==> 0
          round(Signal.SmoothPulse.at(Seconds(1.5))) ==> 1
          round(Signal.SmoothPulse.at(Seconds(2))) ==> 0
        }

        "Pulse produces a signal of true and false values" - {

          val pulse = Signal.Pulse(Seconds(10))

          pulse.at(Seconds(0)) ==> true
          pulse.at(Seconds(5)) ==> true
          pulse.at(Seconds(10)) ==> false
          pulse.at(Seconds(15)) ==> false
          pulse.at(Seconds(20)) ==> true

        }

        "clamping time limits the time so that any time passed can be valid" - {

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
              days(t.value.toInt)
            }

          val clamped =
            daysOfTheWeek.clampTime(Seconds(0), Seconds(6))

          clamped.at(Seconds(-100)) ==> days(0)
          clamped.at(Seconds(-1)) ==> days(0)
          clamped.at(Seconds(0)) ==> days(0)
          clamped.at(Seconds(1)) ==> days(1)
          clamped.at(Seconds(2)) ==> days(2)
          clamped.at(Seconds(3)) ==> days(3)
          clamped.at(Seconds(4)) ==> days(4)
          clamped.at(Seconds(5)) ==> days(5)
          clamped.at(Seconds(6)) ==> days(6)
          clamped.at(Seconds(7)) ==> days(6)
          clamped.at(Seconds(50)) ==> days(6)
          clamped.at(Seconds(1000)) ==> days(6)
        }

        "wrapping time keeps time looping round a fixed point" - {

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
              days(t.value.toInt)
            }

          val wrapped =
            daysOfTheWeek
              .clampTime(Seconds(0), Seconds(1000)) // So now we can't be less than 0, the 1000 would still break
              .wrapTime(Seconds(7))                 // ...but we wrap at day 7

          wrapped.at(Seconds(0)) ==> days(0)
          wrapped.at(Seconds(1)) ==> days(1)
          wrapped.at(Seconds(2)) ==> days(2)
          wrapped.at(Seconds(3)) ==> days(3)
          wrapped.at(Seconds(4)) ==> days(4)
          wrapped.at(Seconds(5)) ==> days(5)
          wrapped.at(Seconds(6)) ==> days(6)
          wrapped.at(Seconds(7)) ==> days(0)

        }

        "affect time" - {

          val double = Signal.Time.affectTime(2.0d)
          val half   = Signal.Time.affectTime(0.5d)

          val times: List[Seconds] =
            (1 to 10).map(_ * 10).map(_.toDouble).toList.map(Seconds.apply)

          times.foreach { t =>
            double.at(t) ==> t * Seconds(2L)
            half.at(t) ==> t / Seconds(2L)
          }

        }

        "Easing" - {

          def simplify(d: Double): Int =
            (d * 100).toInt

          val times: List[Seconds] =
            (0 to 10).toList.map(i => Seconds(i.toDouble))

          "Easing in out" - {
            val signal =
              Signal.EaseInOut(Seconds(10))

            val actual =
              times.map(t => simplify(signal.at(t)))

            val expected =
              List(0, 2, 9, 20, 34, 50, 65, 79, 90, 97, 100)

            actual ==> expected
          }

          "Easing in" - {
            val signal =
              Signal.EaseIn(Seconds(10))

            val actual =
              times.map(t => simplify(signal.at(t)))

            val expected =
              List(0, 1, 4, 10, 19, 29, 41, 54, 69, 84, 100)

            actual ==> expected
          }

          "Easing out" - {
            val signal =
              Signal.EaseOut(Seconds(10))

            val actual =
              times.map(t => simplify(signal.at(t)))

            val expected =
              List(0, 15, 30, 45, 58, 70, 80, 89, 95, 98, 100)

            actual ==> expected
          }
        }

      }

    }

}
