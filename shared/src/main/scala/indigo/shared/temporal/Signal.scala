package indigo.shared.temporal

import indigo.shared.time.{Millis, Seconds}
import indigo.shared.EqualTo._
import indigo.shared.abstractions.Monad
import indigo.shared.abstractions.Functor
import indigo.shared.abstractions.Apply

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
        Signal.create(t => f(fa.at(t)))

      def ap[A, B](fa: Signal[A])(f: Signal[A => B]): Signal[B] =
        Signal.create { (t: Millis) =>
          map(f) { ff =>
            ff(fa.at(t))
          }.at(t)
        }

      def flatMap[A, B](fa: Signal[A])(f: A => Signal[B]): Signal[B] =
        Signal.create(t => f(fa.at(t)).at(t))
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
        monadSignal.apply2(this, other)(f)

      def pipe[B](sf: SignalFunction[A, B]): Signal[B] =
        sf.run(this)

      def |*|[B](other: Signal[B]): Signal[(A, B)] =
        Signal.product(this, other)
    }

  def merge[A, B, C](sa: Signal[A], sb: Signal[B])(f: (A, B) => C): Signal[C] =
    monadSignal.apply2(sa, sb)(f)

  def product[A, B](sa: Signal[A], sb: Signal[B]): Signal[(A, B)] =
    merge(sa, sb)((_, _))

  def triple[A, B, C](sa: Signal[A], sb: Signal[B], sc: Signal[C]): Signal[(A, B, C)] =
    merge(merge(sa, sb)((a, b) => (a, b)), sc)((ab, c) => (ab._1, ab._2, c))

}
