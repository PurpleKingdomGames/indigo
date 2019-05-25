package indigo.shared.temporal

import indigo.shared.time.{Millis, Seconds}
import indigo.shared.abstractions.Applicative
import indigo.shared.EqualTo._

/**
  * A Signal, or Time Varying Value is function t: Millis -> A
  */
sealed trait Signal[A] {

  def at(t: Millis): A

  def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C]

  def |>[B](sf: SignalFunction[A, B]): Signal[B] = pipe(sf)

  def pipe[B](sf: SignalFunction[A, B]): Signal[B]

  def |*|[B](other: Signal[B]): Signal[(A, B)]

  def clampTime(from: Millis, to: Millis): Signal[A] =
    Signal.clampTime(this, from, to)

  def wrapTime(at: Millis): Signal[A] =
    Signal.wrapTime(this, at)

  def map[B](f: A => B): Signal[B] =
    implicitly[Applicative[Signal]].map(this)(f)

  def ap[B](f: Signal[A => B]): Signal[B] =
    implicitly[Applicative[Signal]].ap(this)(f)
}
object Signal {

  implicit val applicativeSignal: Applicative[Signal] =
    new Applicative[Signal] {
      def pure[A](a: A): Signal[A] =
        Signal.fixed(a)

      def map[A, B](fa: Signal[A])(f: A => B): Signal[B] =
        Signal.create(t => f(fa.at(t)))

      def ap[A, B](fa: Signal[A])(f: Signal[A => B]): Signal[B] =
        Signal.create { (t: Millis) =>
          map(f) { ff =>
            ff(fa.at(t))
          }.at(t)
        }

    }

  val Time: Signal[Millis] =
    Signal.create(identity)

  val TimeInSeconds: Signal[Seconds] =
    Signal.create(_.toSeconds)

  def Pulse(interval: Millis): Signal[Boolean] =
    Signal.create(t => (t / interval).value % 2 === 0)

  def clampTime[A](signal: Signal[A], from: Millis, to: Millis): Signal[A] =
    Signal
      .create { t =>
        if (from < to) {
          if (t < from) from
          else if (t > to) to
          else t
        } else {
          if (t < to) to
          else if (t > from) from
          else t
        }
      }
      .map { t =>
        signal.at(t)
      }

  def wrapTime[A](signal: Signal[A], at: Millis): Signal[A] =
    Signal.create { t =>
      signal.at(t % at)
    }

  def fixed[A](a: A): Signal[A] =
    create(_ => a)

  def apply[A](f: Millis => A): Signal[A] =
    create(f)

  def create[A](f: Millis => A): Signal[A] =
    new Signal[A] {
      def at(t: Millis): A =
        f(t)

      def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C] =
        applicativeSignal.apply2(this, other)(f)

      def pipe[B](sf: SignalFunction[A, B]): Signal[B] =
        sf.run(this)

      def |*|[B](other: Signal[B]): Signal[(A, B)] =
        Signal.product(this, other)
    }

  def merge[A, B, C](sa: Signal[A], sb: Signal[B])(f: (A, B) => C): Signal[C] =
    applicativeSignal.apply2(sa, sb)(f)

  def product[A, B](sa: Signal[A], sb: Signal[B]): Signal[(A, B)] =
    merge(sa, sb)((_, _))

  def triple[A, B, C](sa: Signal[A], sb: Signal[B], sc: Signal[C]): Signal[(A, B, C)] =
    merge(merge(sa, sb)((a, b) => (a, b)), sc)((ab, c) => (ab._1, ab._2, c))

}
