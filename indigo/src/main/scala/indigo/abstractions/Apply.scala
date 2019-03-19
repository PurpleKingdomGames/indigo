package indigo.abstractions

trait Apply[F[_]] extends Functor[F] {
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]
}
object Apply {
  def apply[F[_]](implicit a: Apply[F]): Apply[F] = a
}
