package indigo.shared.temporal

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.time.Seconds

/** A Signal is function `t: Seconds -> A`. Signals are stateless time based value, which is to say that for a given
  * time, they will produce a value based on that time. They are useful for representing things like animations,
  * particularly when used in combination with `SignalFunction`s.
  */
opaque type Signal[A] = Seconds => A

object Signal {

  inline def apply[A](run: Seconds => A): Signal[A] = run

  extension [A](s: Signal[A])
    def run: Seconds => A = s

    def at(t: Seconds): A =
      run(t)

    def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C] =
      Signal.mergeSignals(s, other)(f)

    def |>[B](sf: SignalFunction[A, B]): Signal[B] =
      pipe(sf)
    def pipe[B](sf: SignalFunction[A, B]): Signal[B] =
      sf.run(s)

    def |*|[B](other: Signal[B]): Signal[(A, B)] =
      combine(other)
    def combine[B](other: Signal[B]): Signal[(A, B)] =
      Signal.product(s, other)

    def clampTime(from: Seconds, to: Seconds): Signal[A] =
      Signal.clampSignalTime(s, from, to)

    def wrapTime(at: Seconds): Signal[A] =
      Signal.wrapSignalTime(s, at)

    def affectTime(multiplyBy: Double): Signal[A] =
      Signal.affectSignalTime(s, multiplyBy)

    def map[B](f: A => B): Signal[B] =
      (t: Seconds) => f(at(t))

    def ap[B](f: Signal[A => B]): Signal[B] =
      (t: Seconds) =>
        f.map { ff =>
          ff(at(t))
        }.at(t)

    def flatMap[B](f: A => Signal[B]): Signal[B] =
      (t: Seconds) => f(at(t)).at(t)

  implicit class SignalTuple2ToSignal[A, B](t: (Signal[A], Signal[B])) {
    def toSignal: Signal[(A, B)] =
      Signal.product(t._1, t._2)
  }

  implicit class SignalTuple3ToSignal[A, B, C](t: (Signal[A], Signal[B], Signal[C])) {
    def toSignal: Signal[(A, B, C)] =
      Signal.triple(t._1, t._2, t._3)
  }

  implicit class SignalTuple4ToSignal[A, B, C, D](t: (Signal[A], Signal[B], Signal[C], Signal[D])) {
    def toSignal: Signal[(A, B, C, D)] =
      for {
        a <- t._1
        b <- t._2
        c <- t._3
        d <- t._4
      } yield (a, b, c, d)
  }

  implicit class ValueToSignal[A](a: A) {
    def toSignal: Signal[A] =
      Signal.fixed(a)
  }

  val Time: Signal[Seconds] =
    Signal(identity)

  def Pulse(interval: Seconds): Signal[Boolean] =
    Signal(t => (t.toMillis / interval.toMillis).toLong % 2 == 0)

  def Step(at: Seconds): Signal[Boolean] =
    Signal(t => if t >= at then true else false)

  def SinWave: Signal[Double] =
    Signal(t => Math.sin(Radians.fromSeconds(t).toDouble))

  def CosWave: Signal[Double] =
    Signal(t => Math.cos(Radians.fromSeconds(t).toDouble))

  def Orbit(center: Point, distance: Double, offset: Radians): Signal[Vector2] =
    Signal { t =>
      Vector2(
        (Math.sin((Radians.fromSeconds(t) + offset).toDouble) * distance) + center.x,
        (Math.cos((Radians.fromSeconds(t) + offset).toDouble) * distance) + center.y
      )
    }

  def Orbit(center: Point, distance: Double): Signal[Vector2] =
    Orbit(center, distance, Radians(0))

  def SmoothPulse: Signal[Double] =
    Signal.CosWave.map { a =>
      1.0 - (a + 1.0) / 2.0
    }

  def Lerp(from: Point, to: Point, over: Seconds): Signal[Point] =
    if (from == to) Signal.fixed(from)
    else {
      def linear(t: Double, p0: Vector2, p1: Vector2): Vector2 =
        Vector2(
          (1 - t) * p0.x + t * p1.x,
          (1 - t) * p0.y + t * p1.y
        )

      Signal { t =>
        val time   = Math.max(0.0d, Math.min(1.0d, t.toDouble / over.toDouble))
        val interp = linear(time, from.toVector, to.toVector).toPoint

        Point(
          x = if (from.x == to.x) from.x else interp.x,
          y = if (from.y == to.y) from.y else interp.y
        )
      }
    }

  def Linear(over: Seconds): Signal[Double] =
    Signal { t =>
      val time = Math.max(0.0d, Math.min(1.0d, t.toDouble / over.toDouble))
      (time - t.toDouble) * 0.0 + time * 1.0d
    }

  def EaseInOut(duration: Seconds): Signal[Double] =
    Signal.Time |> SignalFunction.easeInOut(duration)

  def EaseIn(duration: Seconds): Signal[Double] =
    Signal.Time |> SignalFunction.easeIn(duration)

  def EaseOut(duration: Seconds): Signal[Double] =
    Signal.Time |> SignalFunction.easeOut(duration)

  def clampSignalTime[A](signal: Signal[A], from: Seconds, to: Seconds): Signal[A] =
    Signal { t =>
      if (from < to)
        if (t < from) from
        else if (t > to) to
        else t
      else if (t < to) to
      else if (t > from) from
      else t
    }.map { t =>
      signal.at(t)
    }

  def wrapSignalTime[A](signal: Signal[A], at: Seconds): Signal[A] =
    Signal { t =>
      signal.at(t % at)
    }

  def affectSignalTime[A](sa: Signal[A], multiplyBy: Double): Signal[A] =
    Signal { t =>
      sa.at(Seconds(t.toDouble * multiplyBy))
    }

  def fixed[A](a: A): Signal[A] =
    apply(_ => a)

  def mergeSignals[A, B, C](sa: Signal[A], sb: Signal[B])(f: (A, B) => C): Signal[C] =
    sa.flatMap(a => sb.map(b => (a, b))).map(p => f(p._1, p._2))

  def product[A, B](sa: Signal[A], sb: Signal[B]): Signal[(A, B)] =
    mergeSignals(sa, sb)((_, _))

  def triple[A, B, C](sa: Signal[A], sb: Signal[B], sc: Signal[C]): Signal[(A, B, C)] =
    mergeSignals(mergeSignals(sa, sb)((a, b) => (a, b)), sc)((ab, c) => (ab._1, ab._2, c))

}
