package indigo.shared.abstractions

trait Functor[F[_]] extends Pure[F] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
object Functor {
  def apply[F[_]](implicit f: Functor[F]): Functor[F] = f
}
