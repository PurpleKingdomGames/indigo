package indigoexts.lenses

trait Lens[A, B] {
  def get(from: A): B
  def set(into: A, value: B): A

  def modify(a: A, f: B => B): A =
    set(a, f(get(a)))

  def >=>[C](next: Lens[B, C]): Lens[A, C] =
    andThen(next)

  def andThen[C](next: Lens[B, C]): Lens[A, C] =
    Lens(
      a => next.get(get(a)),
      (a: A, c: C) => set(a, next.set(get(a), c))
    )
}

object Lens {

  def apply[A, B](getter: A => B, setter: (A, B) => A): Lens[A, B] =
    new Lens[A, B] {
      def get(from: A): B           = getter(from)
      def set(into: A, value: B): A = setter(into, value)
    }

  def identity[A]: Lens[A, A] =
    keepOriginal

  def keepOriginal[A]: Lens[A, A] =
    Lens(Predef.identity, (a, _) => a)

  def keepLatest[A]: Lens[A, A] =
    Lens(Predef.identity, (_, a) => a)

  def fixed[A, B](default: B): Lens[A, B] =
    Lens(_ => default, (a, _) => a)

  // Left for interest but see tests.
//  def modifyF[S, T, A, B](s: S)(f: A => B, g: (S, B) => T)(implicit lens: Lens[S, A]): T =
//    g(s, f(lens.get(s)))

}
