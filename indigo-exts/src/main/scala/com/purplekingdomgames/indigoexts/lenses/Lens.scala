package com.purplekingdomgames.indigoexts.lenses

trait Lens[A, B] {
  def get(from: A): B
  def set(into: A, value: B): A
  def andThen[C](next: Lens[B, C]): Lens[A, C]
}

object Lens {

  def apply[A, B](getter: A => B, setter: (A, B) => A): Lens[A, B] =
    new Lens[A, B] {
      def get(from: A): B           = getter(from)
      def set(into: A, value: B): A = setter(into, value)
      def andThen[C](next: Lens[B, C]): Lens[A, C] =
        Lens(
          getter andThen ((b: B) => next.get(b)),
          (a: A, c: C) => setter(a, next.set(getter(a), c))
        )

    }

  def identity[A, B](b: B): Lens[A, B] =
    Lens(_ => b, (a, _) => a)

}
