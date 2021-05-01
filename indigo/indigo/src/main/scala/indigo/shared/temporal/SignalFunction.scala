package indigo.shared.temporal

/** A Signal Function maps Signal[A] -> Signal[B]
  */
opaque type SignalFunction[A, B] = Signal[A] => Signal[B]

object SignalFunction {

  def apply[A, B](f: A => B): SignalFunction[A, B] =
    lift(f)

  extension [A, B](sf: SignalFunction[A, B])
    def run: Signal[A] => Signal[B] = sf

    def >>>[C](other: SignalFunction[B, C]): SignalFunction[A, C] =
      andThen(other)

    def andThen[C](other: SignalFunction[B, C]): SignalFunction[A, C] =
      sf andThen other

    def &&&[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
      and(other)

    def and[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
      SignalFunction.parallel(sf, other)

  /** Equvilent to `pure` but for SignalFunctions
    */
  def arr[A, B](f: A => B): SignalFunction[A, B] =
    lift[A, B](f)

  def lift[A, B](f: A => B): SignalFunction[A, B] =
    (sa: Signal[A]) => sa.map(f)

  def flatLift[A, B](f: A => Signal[B]): SignalFunction[A, B] =
    (sa: Signal[A]) => sa.flatMap(f)

  def parallel[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    (s: Signal[A]) => sa.run(s) |*| sb.run(s)

}
