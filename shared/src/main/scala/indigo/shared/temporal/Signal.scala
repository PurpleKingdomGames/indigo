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

/**
  * A Signal Function maps Signal[A] -> Signal[B]
  */
final class SignalFunction[A, B](val run: Signal[A] => Signal[B]) extends AnyVal {

  def >>>[C](other: SignalFunction[B, C]): SignalFunction[A, C] =
    SignalFunction.andThen(this, other)

  def andThen[C](other: SignalFunction[B, C]): SignalFunction[A, C] =
    SignalFunction.andThen(this, other)

  def &&&[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    SignalFunction.parallel(this, other)

  def and[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    SignalFunction.parallel(this, other)
}
object SignalFunction {

  import indigo.shared.abstractions.syntax._

  def apply[A, B](f: A => B): SignalFunction[A, B] =
    lift(f)

  def arr[A, B](f: A => B): SignalFunction[A, B] =
    lift[A, B](f)

  def lift[A, B](f: A => B): SignalFunction[A, B] =
    new SignalFunction((sa: Signal[A]) => sa.map(f))

  def andThen[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[B, C]): SignalFunction[A, C] =
    new SignalFunction(sa.run andThen sb.run)

  def parallel[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    new SignalFunction[A, (B, C)]((s: Signal[A]) => (sa.run(s), sb.run(s)).map2((b, c) => (b, c)))

}
