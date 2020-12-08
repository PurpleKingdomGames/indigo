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

  def merge[B, C](other: SignalState[S, B])(f: (A, B) => C): SignalState[S, C] =
    for {
      a <- this
      b <- other
    } yield f(a, b)

  def |>[B](sf: SignalFunction[A, B]): SignalState[S, B] =
    pipe(sf)
  def pipe[B](sf: SignalFunction[A, B]): SignalState[S, B] =
    SignalState { (s: S) =>
      val sig = run(s)
      sf.run(sig.map(_._2)).flatMap(b => sig.map(_ => (s, b)))
    }

  def |*|[B](other: SignalState[S, B]): SignalState[S, (A, B)] =
    combine(other)
  def combine[B](other: SignalState[S, B]): SignalState[S, (A, B)] =
    for {
      a <- this
      b <- other
    } yield (a, b)

  def clampTime(from: Seconds, to: Seconds): SignalState[S, A] =
    SignalState((s: S) => run(s).clampTime(from, to))
  def wrapTime(at: Seconds): SignalState[S, A] =
    SignalState((s: S) => run(s).wrapTime(at))
  def affectTime(multiplyBy: Double): SignalState[S, A] =
    SignalState((s: S) => run(s).affectTime(multiplyBy))

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
