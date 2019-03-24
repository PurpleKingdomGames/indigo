package indigo.abstractions

trait Applicative[F[_]] extends Apply[F] {
  def apply2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    ap2(fa, fb)(pure(f))
}
object Applicative {
  def apply[F[_]](implicit ap: Applicative[F]): Applicative[F] = ap
}