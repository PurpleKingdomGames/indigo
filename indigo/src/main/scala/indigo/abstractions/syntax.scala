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

  implicit class ApplicativeSyntax[F[_], A, B](ft: (F[A], F[B]))(implicit ev: Applicative[F]) {
    def map2[C](f: (A, B) => C): F[C] =
      ev.apply2(ft._1, ft._2)(f)
  }

  implicit class MonadSyntax[F[_], A](fa: F[A])(implicit ev: Monad[F]) {
    def flatMap[B](f: A => F[B]): F[B] =
      ev.flatMap(fa)(f)
  }

}
