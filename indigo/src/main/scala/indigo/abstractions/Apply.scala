package indigo.abstractions

trait Apply[F[_]] extends Functor[F] {
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]
  def ap2[A, B, C](fa: F[A], fb: F[B])(f: F[(A, B) => C]): F[C] =
    ap(fb)(ap(fa)(map(f)(_.curried)))
}
object Apply {
  def apply[F[_]](implicit a: Apply[F]): Apply[F] = a
}
