package indigo.shared.abstractions

trait Monoid[A] extends Semigroup[A] {
  def identity: A
}