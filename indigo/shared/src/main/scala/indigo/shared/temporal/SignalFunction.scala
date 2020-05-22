package indigo.shared.temporal

/**
  * A Signal Function maps Signal[A] -> Signal[B]
  */
final class SignalFunction[A, B](val run: Signal[A] => Signal[B]) extends AnyVal {

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

  def apply[A, B](f: A => B): SignalFunction[A, B] =
    lift(f)

  /**
    * Equvilent to `pure` but for SignalFunctions
    */
  def arr[A, B](f: A => B): SignalFunction[A, B] =
    lift[A, B](f)

  def lift[A, B](f: A => B): SignalFunction[A, B] =
    new SignalFunction((sa: Signal[A]) => sa.map(f))

  def flatLift[A, B](f: A => Signal[B]): SignalFunction[A, B] =
    new SignalFunction((sa: Signal[A]) => sa.flatMap(f))

  def andThen[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[B, C]): SignalFunction[A, C] =
    new SignalFunction(sa.run andThen sb.run)

  def parallel[A, B, C](sa: SignalFunction[A, B], sb: SignalFunction[A, C]): SignalFunction[A, (B, C)] =
    new SignalFunction[A, (B, C)]((s: Signal[A]) => sa.run(s) |*| sb.run(s))

}
