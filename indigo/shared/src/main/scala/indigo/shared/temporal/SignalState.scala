package indigo.shared.temporal

import indigo.shared.time.Seconds

final class SignalState[S, A](val run: S => Signal[(S, A)]) extends AnyVal {

  def toSignal(state: S): Signal[A] =
    run(state).map(_._2)

  def modify(f: S => S): SignalState[S, Unit] =
    SignalState { (s: S) =>
      run(s).map(p => (f(p._1), ()))
    }

  def get: SignalState[S, S] =
    SignalState { (s: S) =>
      run(s).map(p => (p._1, p._1))
    }

  def set(newState: S): SignalState[S, Unit] =
    SignalState { (s: S) =>
      run(s).map(_ => (newState, ()))
    }

  // def merge[B, C](other: SignalState[B])(f: (A, B) => C): SignalState[C]

  // def |>[B](sf: SignalFunction[A, B]): SignalState[B]
  // def pipe[B](sf: SignalFunction[A, B]): SignalState[B]

  // def |*|[B](other: SignalState[B]): SignalState[(A, B)]
  // def combine[B](other: SignalState[B]): SignalState[(A, B)]

  // def clampTime(from: Seconds, to: Seconds): SignalState[A]
  // def wrapTime(at: Seconds): SignalState[A]
  // def affectTime(multiplyBy: Double): SignalState[A]

  def map[B](f: A => B): SignalState[S, B] =
    SignalState { (s: S) =>
      run(s).map { case (s, a) => (s, f(a)) }
    }

  def ap[B](f: SignalState[S, A => B]): SignalState[S, B] =
    SignalState { (s: S) =>
      run(s).flatMap {
        case (nextState, a) =>
          f.run(nextState).map {
            case (finalState, g) =>
              (finalState, g(a))
          }
      }
    }

  def flatMap[B](f: A => SignalState[S, B]): SignalState[S, B] =
    SignalState { (s: S) =>
      run(s).flatMap { case (ss, aa) => f(aa).run(ss) }
    }

}

object SignalState {

  def fromSignal[S, A](signal: Signal[A]): SignalState[S, A] =
    apply((s: S) => signal.map(a => (s, a)))

  def fixed[S, A](a: A): SignalState[S, A] =
    apply((s: S) => Signal.fixed((s, a)))

  def apply[S, A](f: S => Signal[(S, A)]): SignalState[S, A] =
    new SignalState[S, A](f)

}
