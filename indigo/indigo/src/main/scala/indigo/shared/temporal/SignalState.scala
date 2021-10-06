package indigo.shared.temporal

import indigo.shared.time.Seconds

/** `SignalState` is a type of `Signal` that carries transformable state values throughout it's life.
  */
opaque type SignalState[S, A] = S => Signal[(S, A)]

object SignalState {

  def fromSignal[S, A](signal: Signal[A]): SignalState[S, A] =
    apply((s: S) => signal.map(a => (s, a)))

  def fixed[S, A](a: A): SignalState[S, A] =
    apply((s: S) => Signal.fixed((s, a)))

  inline def apply[S, A](f: S => Signal[(S, A)]): SignalState[S, A] = f

  extension [S, A](ss: SignalState[S, A])
    def run: S => Signal[(S, A)] = ss

    def toSignal(state: S): Signal[A] =
      ss(state).map(_._2)

    def modify(f: S => S): SignalState[S, Unit] =
      SignalState { (s: S) =>
        ss(s).map(p => (f(p._1), ()))
      }

    def get: SignalState[S, S] =
      SignalState { (s: S) =>
        ss(s).map(p => (p._1, p._1))
      }

    def set(newState: S): SignalState[S, Unit] =
      SignalState { (s: S) =>
        ss(s).map(_ => (newState, ()))
      }

    def merge[B, C](other: SignalState[S, B])(f: (A, B) => C): SignalState[S, C] =
      for {
        a <- ss
        b <- other
      } yield f(a, b)

    def |>[B](sf: SignalFunction[A, B]): SignalState[S, B] =
      pipe(sf)
    def pipe[B](sf: SignalFunction[A, B]): SignalState[S, B] =
      SignalState { (s: S) =>
        val sig = ss(s)
        sf.run(sig.map(_._2)).flatMap(b => sig.map(_ => (s, b)))
      }

    def |*|[B](other: SignalState[S, B]): SignalState[S, (A, B)] =
      combine(other)
    def combine[B](other: SignalState[S, B]): SignalState[S, (A, B)] =
      for {
        a <- ss
        b <- other
      } yield (a, b)

    def clampTime(from: Seconds, to: Seconds): SignalState[S, A] =
      SignalState((s: S) => ss(s).clampTime(from, to))
    def wrapTime(at: Seconds): SignalState[S, A] =
      SignalState((s: S) => ss(s).wrapTime(at))
    def affectTime(multiplyBy: Double): SignalState[S, A] =
      SignalState((s: S) => ss(s).affectTime(multiplyBy))

    def map[B](f: A => B): SignalState[S, B] =
      SignalState { (s: S) =>
        ss(s).map { case (s, a) => (s, f(a)) }
      }

    def ap[B](f: SignalState[S, A => B]): SignalState[S, B] =
      SignalState { (s: S) =>
        ss(s).flatMap { case (nextState, a) =>
          f(nextState).map { case (finalState, g) =>
            (finalState, g(a))
          }
        }
      }

    def flatMap[B](f: A => SignalState[S, B]): SignalState[S, B] =
      SignalState { (s: S) =>
        ss(s).flatMap { case (ss, aa) => f(aa)(ss) }
      }

}
