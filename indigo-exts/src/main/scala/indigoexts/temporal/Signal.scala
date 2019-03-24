package indigoexts.temporal

import indigo.GameTime.Millis
import indigo.abstractions.Applicative

/**
  * A Signal, or Time Varying Value is function t: Millis -> A
  */
trait Signal[A] {
  def at(t: Millis): A
  def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C]
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
      def at(t: Millis): A = f(t)
      def merge[B, C](other: Signal[B])(f: (A, B) => C): Signal[C] =
        applicativeSignal.apply2(this, other)(f)
    }
}

/**
  * A Signal Function maps Signal[A] -> Signal[B]
  */
class SignalFunction[A, B](val f: Signal[A] => Signal[B]) {

  def andThen[C](other: SignalFunction[B, C]): SignalFunction[A, C] =
    SignalFunction.andThen(this, other)

  def and[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    SignalFunction.parallel(this, other)
}
object SignalFunction {

  import indigo.abstractions.syntax._

  def apply[A, B](f: Signal[A] => Signal[B]): SignalFunction[A, B] =
    new SignalFunction(f)

  def lift[A, B, C](f: A => B): SignalFunction[A, B] =
    new SignalFunction((sa: Signal[A]) => sa.map(f))

  def andThen[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[B, C]): SignalFunction[A, C] =
    SignalFunction(sa.f andThen sb.f)

  def parallel[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    SignalFunction[A, (B, C)]{
      (s: Signal[A]) =>
        (sa.f(s), sb.f(s)).map2((b, c) => (b, c))
    }

}

/**
  * A Temporal Proposition is a function t: Millis -> Boolean
  */
trait TemporalProposition extends Signal[Boolean] {
  def at(t: Millis): Boolean
}

/**
  * A Temporal Predicate is the combination of a Signal and a TProp
  * where we use the values coming into the unapplied signal (to capture the time!)
  */
trait TemporalPredicate[A] {

  // def until
// SingalFunction[A, Boolean] => TPred (constructor)

}
