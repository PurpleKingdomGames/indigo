package indigo.shared.temporal

import indigo.shared.time.Seconds

final class SignalState[S, A](val f: S => (S, Signal[A])) extends AnyVal {

  def toSignal(state: S): Signal[A] =
    f(state)._2

  // def merge[B, C](other: SignalState[B])(f: (A, B) => C): SignalState[C]

  // def |>[B](sf: SignalFunction[A, B]): SignalState[B]
  // def pipe[B](sf: SignalFunction[A, B]): SignalState[B]

  // def |*|[B](other: SignalState[B]): SignalState[(A, B)]
  // def combine[B](other: SignalState[B]): SignalState[(A, B)]

  // def clampTime(from: Seconds, to: Seconds): SignalState[A]
  // def wrapTime(at: Seconds): SignalState[A]
  // def affectTime(multiplyBy: Double): SignalState[A]

  def map[B](f: A => B): SignalState[S, B] =
    SignalState((s: S) => (s, toSignal(s).map(f)))
  //SignalReader((r: R) => toSignal(r).map(f))
  // def ap[B](f: SignalState[A => B]): SignalState[B]
  // def flatMap[B](f: A => SignalState[B]): SignalState[B]

}

object SignalState {

  def fromSignal[S, A](signal: Signal[A]): SignalState[S, A] =
    apply((s: S) => (s, signal))

  def fixed[S, A](a: A): SignalState[S, A] =
    apply((s: S) => (s, Signal.fixed(a)))

  def apply[S, A](f: S => (S, Signal[A])): SignalState[S, A] =
    new SignalState[S, A](f)

}
