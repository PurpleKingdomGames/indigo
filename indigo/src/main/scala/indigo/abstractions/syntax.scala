package indigo.abstractions

package object syntax {

  implicit class FunctorSyntax[F[_], A](fa: F[A])(implicit ev: Functor[F]) {
    def map[B](f: A => B): F[B] =
      ev.map(fa)(f)
  }
  implicit class ApplySyntax[F[_], A](fa: F[A])(implicit ev: Apply[F]) {
    def ap[B](f: F[A => B]): F[B] =
      ev.ap(fa)(f)
  }
  implicit class MonadSyntax[F[_], A](fa: F[A])(implicit ev: Monad[F]) {
    def flatMap[B](f: A => F[B]): F[B] =
      ev.flatMap(fa)(f)
  }

}
