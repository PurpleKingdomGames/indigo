package indigo.macroshaders

opaque type Pipeline[A, B] = Program[A] => Program[B]
object Pipeline:

  import Program.*

  inline def apply[Env, A, B](f: A => B): Pipeline[A, B] =
    lift(f)

  extension [Env, A, B](ff: Pipeline[A, B])
    def runWith: Program[A] => Program[B] = ff

    def >>>[C](other: Pipeline[B, C]): Pipeline[A, C] =
      andThen(other)

    def andThen[C](other: Pipeline[B, C]): Pipeline[A, C] =
      ff andThen other

    def &&&[C](other: Pipeline[A, C]): Pipeline[A, (B, C)] =
      and(other)

    def and[C](other: Pipeline[A, C]): Pipeline[A, (B, C)] =
      Pipeline.parallel(ff, other)

  /** Equvilent to `pure` but for SignalFunctions
    */
  def arr[Env, A, B](f: A => B): Pipeline[A, B] =
    lift[Env, A, B](f)

  def lift[Env, A, B](f: A => B): Pipeline[A, B] =
    (fa: Program[A]) => fa.map(f)

  def flatLift[Env, A, B](f: A => Program[B]): Pipeline[A, B] =
    (fa: Program[A]) => fa.flatMap(f)

  def parallel[Env, A, B, C](
      fa: Pipeline[A, B],
      fb: Pipeline[A, C]
  ): Pipeline[A, (B, C)] =
    (s: Program[A]) => fa.runWith(s) |*| fb.runWith(s)
