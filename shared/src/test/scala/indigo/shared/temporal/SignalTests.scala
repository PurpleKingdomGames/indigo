package indigo.shared.temporal

import utest._
import indigo.shared.time.GameTime
import indigo.shared.time.Millis

object SignalTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Signals" - {
        "should be able to get a fixed value from a signal" - {
          Signal.fixed("a").at(Millis(100)) ==> "a"
        }

        "should be able to get a value over time from a signal" - {
          val sig = Signal.create(t => t.toInt * 10)

          (0 to 10).foreach { t =>
            sig.at(Millis(t)) ==> t * 10
          }
        }

        "should be able to merge signals" - {
          val a = Signal.fixed(1)
          val b = Signal.fixed(2)

          //Alternative to applicative syntax
          a.merge(b)(_ + _).at(Millis.zero) ==> 3
        }

        "Pulse signal" - {

          val pulse = Signal.Pulse(Millis(10))

          pulse.at(Millis(0)) ==> true
          pulse.at(Millis(1)) ==> true
          pulse.at(Millis(10)) ==> false
          pulse.at(Millis(11)) ==> false
          pulse.at(Millis(20)) ==> true
          pulse.at(Millis(23)) ==> true
          pulse.at(Millis(1234)) ==> false
          pulse.at(Millis(1243)) ==> true

        }
      }

      "SignalFunctions" - {
        "should be able to compose signal functions" - {
          val f = SignalFunction.lift((i: Int) => s"$i")
          val g = SignalFunction.lift((s: String) => s.length < 2)

          val h: SignalFunction[Int, Boolean] = f andThen g

          h.run(Signal.fixed(1)).at(Millis.zero) ==> true
          h.run(Signal.fixed(1000)).at(Millis.zero) ==> false
        }

        "should be able to run signal functions and parallel" - {
          val f = SignalFunction.lift((i: Int) => s"$i")
          val g = SignalFunction.lift((i: Int) => i < 10)

          val h: SignalFunction[Int, (String, Boolean)] = f and g

          h.run(Signal.fixed(1)).at(Millis.zero) ==> ("1", true)
          h.run(Signal.fixed(1000)).at(Millis.zero) ==> ("1000", false)
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

        positionSignal.at(Millis.zero) ==> (0, 10)
        positionSignal.at(Millis(250)) ==> (10, 0)
        positionSignal.at(Millis(500)) ==> (0, -10)
        positionSignal.at(Millis(750)) ==> (-10, 0)
        positionSignal.at(Millis(1000)) ==> (0, 10)

      }

      "Moving and then stopping after a certain time" - {

        final case class Conditions(xPos: Int, velocity: Int, creationTime: Millis, stopAfter: Millis)

        val conditions = Conditions(100, 10, Millis.zero, Millis(20000))

        val timeAndConditions: Signal[(Millis, Conditions)] =
          Signal.Time |*| Signal.fixed(conditions)

        val timeShift: SignalFunction[(Millis, Conditions), (Millis, Conditions)] =
          SignalFunction(t => (t._1 - t._2.creationTime, t._2))

        val timeStop: SignalFunction[(Millis, Conditions), (Millis, Conditions)] =
          SignalFunction(t => if (t._1 >= t._2.stopAfter) (t._2.stopAfter, t._2) else t)

        val timeToSeconds: SignalFunction[(Millis, Conditions), (Double, Conditions)] =
          SignalFunction(t => (t._1.toDouble * 0.001d, t._2))

        val positionX: SignalFunction[(Double, Conditions), Int] =
          SignalFunction(t => t._2.xPos + (t._1 * t._2.velocity).toInt)

        val signalPipeline: SignalFunction[(Millis, Conditions), Int] =
          timeShift >>> timeStop >>> timeToSeconds >>> positionX

        val signal: Signal[Int] =
          timeAndConditions |> signalPipeline

        // Sanity check, basic signal should adance position over time
        (0 to 10).toList.foreach { i =>
          signal.at(Millis(i * 1000)) ==> conditions.xPos + (conditions.velocity * i).toInt
        }

        signal.at(Millis(30000)) ==> signal.at(Millis(20000))

      }

      "Utils" - {

        "Pulse produces a signal of true and false values" - {

          val pulse = Signal.Pulse(Millis(10))

          pulse.at(Millis(0)) ==> true
          pulse.at(Millis(5)) ==> true
          pulse.at(Millis(10)) ==> false
          pulse.at(Millis(15)) ==> false
          pulse.at(Millis(20)) ==> true

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
            Signal.create { t =>
              // Clearly this will blow up for < 0 or > 6
              days(t.value.toInt)
            }

          val clamped =
            daysOfTheWeek.clampTime(Millis(0), Millis(6))

          clamped.at(Millis(-100)) ==> days(0)
          clamped.at(Millis(-1)) ==> days(0)
          clamped.at(Millis(0)) ==> days(0)
          clamped.at(Millis(1)) ==> days(1)
          clamped.at(Millis(2)) ==> days(2)
          clamped.at(Millis(3)) ==> days(3)
          clamped.at(Millis(4)) ==> days(4)
          clamped.at(Millis(5)) ==> days(5)
          clamped.at(Millis(6)) ==> days(6)
          clamped.at(Millis(7)) ==> days(6)
          clamped.at(Millis(50)) ==> days(6)
          clamped.at(Millis(1000)) ==> days(6)
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
            Signal.create { t =>
              // Clearly this will blow up for < 0 or > 6
              days(t.value.toInt)
            }

          val wrapped =
            daysOfTheWeek
              .clampTime(Millis(0), Millis(1000)) // So now we can't be less than 0, the 1000 would still break
              .wrapTime(Millis(7))                // ...but we wrap at day 7

          wrapped.at(Millis(0)) ==> days(0)
          wrapped.at(Millis(1)) ==> days(1)
          wrapped.at(Millis(2)) ==> days(2)
          wrapped.at(Millis(3)) ==> days(3)
          wrapped.at(Millis(4)) ==> days(4)
          wrapped.at(Millis(5)) ==> days(5)
          wrapped.at(Millis(6)) ==> days(6)
          wrapped.at(Millis(7)) ==> days(0)

        }

      }

    }

}
