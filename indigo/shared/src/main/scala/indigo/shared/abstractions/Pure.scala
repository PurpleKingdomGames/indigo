package indigo.shared.abstractions

trait Pure[F[_]] {
  def pure[A](a: A): F[A]
}
object Pure {
  def apply[F[_]](implicit p: Pure[F]): Pure[F] = p
}
