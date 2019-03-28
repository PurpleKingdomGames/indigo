package indigoexts.temporal

import indigo.GameTime.Millis
import indigo.abstractions.Applicative

/**
  * A Signal, or Time Varying Value is function t: Millis -> A
  */
sealed trait Signal[A] {
  def at(t: Millis): A
  def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C]
  def |>[B](sf: SignalFunction[A, B]): Signal[B] = pipe(sf)
  def pipe[B](sf: SignalFunction[A, B]): Signal[B]
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

  def fixed[A](a: A): Signal[A] =
    create(_ => a)

  def create[A](f: Millis => A): Signal[A] =
    new Signal[A] {
      def at(t: Millis): A =
        f(t)

      def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C] =
        applicativeSignal.apply2(this, other)(f)

      def pipe[B](sf: SignalFunction[A, B]): Signal[B] =
        sf.run(this)
    }

  def merge[A, B, C](sa: Signal[A], sb: Signal[B])(f: (A, B) => C): Signal[C] =
    applicativeSignal.apply2(sa, sb)(f)

  def product[A, B](sa: Signal[A], sb: Signal[B]): Signal[(A, B)] =
    merge(sa, sb)((_, _))

}

/**
  * A Signal Function maps Signal[A] -> Signal[B]
  */
final class SignalFunction[A, B](val run: Signal[A] => Signal[B]) {

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

  import indigo.abstractions.syntax._

  def apply[A, B](f: Signal[A] => Signal[B]): SignalFunction[A, B] =
    new SignalFunction(f)

  def arr[A, B](f: A => B): SignalFunction[A, B] =
    lift[A, B](f)

  def lift[A, B](f: A => B): SignalFunction[A, B] =
    new SignalFunction((sa: Signal[A]) => sa.map(f))

  def andThen[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[B, C]): SignalFunction[A, C] =
    SignalFunction(sa.run andThen sb.run)

  def parallel[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    SignalFunction[A, (B, C)] { (s: Signal[A]) =>
      (sa.run(s), sb.run(s)).map2((b, c) => (b, c))
    }

}
