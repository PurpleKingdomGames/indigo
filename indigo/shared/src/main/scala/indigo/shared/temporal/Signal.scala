package indigo.shared.temporal

import indigo.shared.time.{Millis, Seconds}
import indigo.shared.EqualTo._
import indigo.shared.abstractions.Monad
import indigo.shared.abstractions.Functor
import indigo.shared.abstractions.Apply
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians

/**
  * A Signal is function t: Millis -> A
  */
final class Signal[A](val f: Millis => A) extends AnyVal {

  def at(t: Millis): A =
    f(t)

  def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C] =
    implicitly[Monad[Signal]].apply2(this, other)(f)

  def |>[B](sf: SignalFunction[A, B]): Signal[B] =
    pipe(sf)

  def pipe[B](sf: SignalFunction[A, B]): Signal[B] =
    sf.run(this)

  def |*|[B](other: Signal[B]): Signal[(A, B)] =
    Signal.product(this, other)

  def clampTime(from: Millis, to: Millis): Signal[A] =
    Signal.clampTime(this, from, to)

  def wrapTime(at: Millis): Signal[A] =
    Signal.wrapTime(this, at)

  def affectTime(multiplyBy: Double): Signal[A] =
    Signal.affectTime(this, multiplyBy)

  def easeIn(target: Millis, divisor: Int): Signal[A] =
    Signal.easeIn(this, target, divisor)

  def easeOut(target: Millis, divisor: Int): Signal[A] =
    Signal.easeOut(this, target, divisor)

  def map[B](f: A => B): Signal[B] =
    implicitly[Functor[Signal]].map(this)(f)

  def ap[B](f: Signal[A => B]): Signal[B] =
    implicitly[Apply[Signal]].ap(this)(f)

  def flatMap[B](f: A => Signal[B]): Signal[B] =
    implicitly[Monad[Signal]].flatMap(this)(f)
}
object Signal {

  implicit val monadSignal: Monad[Signal] =
    new Monad[Signal] {
      def pure[A](a: A): Signal[A] =
        Signal.fixed(a)

      def map[A, B](fa: Signal[A])(f: A => B): Signal[B] =
        Signal(t => f(fa.at(t)))

      def ap[A, B](fa: Signal[A])(f: Signal[A => B]): Signal[B] =
        Signal { (t: Millis) =>
          map(f) { ff =>
            ff(fa.at(t))
          }.at(t)
        }

      def flatMap[A, B](fa: Signal[A])(f: A => Signal[B]): Signal[B] =
        Signal(t => f(fa.at(t)).at(t))
    }

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

  val Time: Signal[Millis] =
    Signal(identity)

  val TimeInSeconds: Signal[Seconds] =
    Signal(_.toSeconds)

  def Pulse(interval: Millis): Signal[Boolean] =
    Signal(t => (t / interval).value % 2 === 0)

  def SinWave: Signal[Double] =
    Signal(t => Math.sin(Radians.fromSeconds(t.toSeconds).value))

  def CosWave: Signal[Double] =
    Signal(t => Math.cos(Radians.fromSeconds(t.toSeconds).value))

  def Orbit(center: Point, distance: Double): Signal[Vector2] =
    Signal { t =>
      Vector2((Math.sin(Radians.fromSeconds(t.toSeconds).value) * distance) + center.x, (Math.cos(Radians.fromSeconds(t.toSeconds).value) * distance) + center.y)
    }

  def SmoothPulse: Signal[Double] =
    Signal.CosWave.map { a =>
      1.0 - (a + 1.0) / 2.0
    }

  def Lerp(from: Point, to: Point, over: Seconds): Signal[Point] =
    if (from === to) Signal.fixed(from)
    else {
      def linear(t: Double, p0: Vector2, p1: Vector2): Vector2 =
        Vector2(
          (1 - t) * p0.x + t * p1.x,
          (1 - t) * p0.y + t * p1.y
        )

      Signal { t =>
        val time   = Math.max(0, Math.min(1, t.toSeconds.toDouble)) / over.toDouble
        val interp = linear(time, from.toVector, to.toVector).toPoint

        Point(
          x = if (from.x === to.x) from.x else interp.x,
          y = if (from.y === to.y) from.y else interp.y
        )
      }
    }

  def clampTime[A](signal: Signal[A], from: Millis, to: Millis): Signal[A] =
    Signal { t =>
      if (from < to) {
        if (t < from) from
        else if (t > to) to
        else t
      } else {
        if (t < to) to
        else if (t > from) from
        else t
      }
    }.map { t =>
      signal.at(t)
    }

  def wrapTime[A](signal: Signal[A], at: Millis): Signal[A] =
    Signal { t =>
      signal.at(t % at)
    }

  def affectTime[A](sa: Signal[A], multiplyBy: Double): Signal[A] =
    Signal { t =>
      sa.at(Millis((t.toDouble * multiplyBy).toLong))
    }

  def easeOut[A](sa: Signal[A], target: Millis, divisor: Int): Signal[A] =
    if (divisor === 0) sa
    else {
      Signal {
        case t @ Millis.zero =>
          sa.at(t)

        case t =>
          val targetChecked: Millis =
            Millis(Math.min(t.value, target.value))

          val next: Millis =
            t + ((target - targetChecked) / Millis(divisor.toLong))

          sa.at(next)
      }
    }

  def easeIn[A](sa: Signal[A], target: Millis, divisor: Int): Signal[A] =
    if (divisor === 0) sa
    else {
      Signal {
        case t @ Millis.zero =>
          sa.at(t)

        case t =>
          val targetChecked: Long =
            Math.min(t.value, target.value)

          val next: Long =
            t.value / Math.max(1, (target.value - targetChecked) / divisor.toLong)

          sa.at(Millis(next))
      }
    }

  def fixed[A](a: A): Signal[A] =
    apply(_ => a)

  def apply[A](f: Millis => A): Signal[A] =
    new Signal[A](f)

  def merge[A, B, C](sa: Signal[A], sb: Signal[B])(f: (A, B) => C): Signal[C] =
    monadSignal.apply2(sa, sb)(f)

  def product[A, B](sa: Signal[A], sb: Signal[B]): Signal[(A, B)] =
    merge(sa, sb)((_, _))

  def triple[A, B, C](sa: Signal[A], sb: Signal[B], sc: Signal[C]): Signal[(A, B, C)] =
    merge(merge(sa, sb)((a, b) => (a, b)), sc)((ab, c) => (ab._1, ab._2, c))

}
