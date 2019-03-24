package indigo.abstractions

trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
object Monad {
  def apply[F[_]](implicit m: Monad[F]): Monad[F] = m
}
