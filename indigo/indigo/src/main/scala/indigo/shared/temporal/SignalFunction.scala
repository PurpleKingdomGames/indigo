package indigo.shared.temporal

/** A Signal Function is a combinator that maps `Signal[A] -> Signal[B]`. One way to thing of signal functions is to
  * think of each instance as one section of a transformation pipeline. When you attach the pipeline to a `Signal` you
  * can ask the pipeline for a transformed value over time. For example, you could have a signal that produces an
  * endlessly looping angle, and a signal function that turns that angle into perhaps and orbit or a color.
  */
opaque type SignalFunction[A, B] = Signal[A] => Signal[B]

object SignalFunction {

  inline def apply[A, B](f: A => B): SignalFunction[A, B] =
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
