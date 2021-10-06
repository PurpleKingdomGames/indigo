package indigo.shared.temporal

import indigo.shared.time.Seconds

/** SignalReader is a wrapper around signal that provides an execution environment. As such it generally delegates to
  * Signal.
  */
opaque type SignalReader[R, A] = R => Signal[A]

object SignalReader {

  def fromSignal[R, A](signal: Signal[A]): SignalReader[R, A] =
    apply((_: R) => signal)

  def fixed[R, A](a: A): SignalReader[R, A] =
    apply((_: R) => Signal.fixed(a))

  inline def apply[R, A](run: R => Signal[A]): SignalReader[R, A] = run

  extension [R, A](sr: SignalReader[R, A])
    def run: R => Signal[A] = sr

    def toSignal(readFrom: R): Signal[A] =
      sr(readFrom)

    def merge[B, C](other: SignalReader[R, B])(f: (A, B) => C): SignalReader[R, C] =
      SignalReader((r: R) => sr(r).merge(other.toSignal(r))(f))

    def |>[B](sf: SignalFunction[A, B]): SignalReader[R, B] =
      pipe(sf)
    def pipe[B](sf: SignalFunction[A, B]): SignalReader[R, B] =
      SignalReader((r: R) => sf.run(sr(r)))

    def |*|[B](other: SignalReader[R, B]): SignalReader[R, (A, B)] =
      combine(other)
    def combine[B](other: SignalReader[R, B]): SignalReader[R, (A, B)] =
      SignalReader((r: R) => sr(r).combine(other.toSignal(r)))

    def clampTime(from: Seconds, to: Seconds): SignalReader[R, A] =
      SignalReader((r: R) => sr(r).clampTime(from, to))
    def wrapTime(at: Seconds): SignalReader[R, A] =
      SignalReader((r: R) => sr(r).wrapTime(at))
    def affectTime(multiplyBy: Double): SignalReader[R, A] =
      SignalReader((r: R) => sr(r).affectTime(multiplyBy))

    def map[B](f: A => B): SignalReader[R, B] =
      SignalReader((r: R) => sr(r).map(f))

    def ap[B](f: SignalReader[R, A => B]): SignalReader[R, B] =
      SignalReader((r: R) => sr(r).ap(f.toSignal(r)))

    def flatMap[B](f: A => SignalReader[R, B]): SignalReader[R, B] =
      SignalReader((r: R) => sr(r).flatMap(a => f(a).toSignal(r)))

}
