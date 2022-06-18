package indigo.shared.utils

/** Represents a simple-as-they-come lens, primarily for use with scenes in order to extract and replace the parts of,
  * say, a model that the scene wants to operate on.
  */
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

  /** Lens constructor
    *
    * @param getter
    *   function that notionally reads a value B from a given A
    * @param setter
    *   function that notionally sets a value B into a given A
    * @return
    *   Lens[A, B] - a lens that in some way works with a B nested in an A
    */
  def apply[A, B](getter: A => B, setter: (A, B) => A): Lens[A, B] =
    new Lens[A, B] {
      def get(from: A): B           = getter(from)
      def set(into: A, value: B): A = setter(into, value)
    }

  /** A NoOp where the outer type is equal to the inner type and never changes. Alias for `Lens.keepOriginal[A]`.
    */
  def identity[A]: Lens[A, A] =
    keepOriginal

  /** A NoOp where the outer type is equal to the inner type and never changes.
    */
  def keepOriginal[A]: Lens[A, A] =
    Lens(Predef.identity, (a, _) => a)

  /** Simple replacement. The outer type is equal to the inner type and we always keep the latest one.
    */
  def keepLatest[A]: Lens[A, A] =
    Lens(Predef.identity, (_, a) => a)

  /** Fixed will always get the default argument and never set it back.
    */
  def fixed[A, B](default: B): Lens[A, B] =
    Lens(_ => default, (a, _) => a)

  /** Get but don't set. Implies the value `B` nested in `A` never changes. `B` is read only / immutable in this scope.
    */
  def readOnly[A, B](get: A => B): Lens[A, B] =
    Lens(get, (a, _) => a)

  /** Unit is like Lens.fixed(()) without the arguments, where there is no inner type (or rather, the inner type is hard
    * coded to Unit)
    */
  def unit[A]: Lens[A, Unit] =
    Lens(_ => (), (a, _) => a)

}
