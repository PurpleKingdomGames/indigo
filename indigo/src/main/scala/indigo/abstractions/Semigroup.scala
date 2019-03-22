package indigo.abstractions

trait Semigroup[A] {
  def combine(a: A, b: A): A
}
object Semigroup {
  def apply[A](implicit sg: Semigroup[A]): Semigroup[A] = sg
}