package indigoexts.abstractions

trait Id[A] {
  val value: A

  def map[B](f: A => B): Id[B] =
    Id.map[A, B](Id.pure(value))(f)

  def flatMap[B](f: A => Id[B]): Id[B] =
    Id.flatMap[A, B](Id.pure(value))(f)

  def ===(other: Id[A]): Boolean =
    value == other.value
}

object Id {

  def pure[A](a: A): Id[A] =
    new Id[A] {
      val value: A = a
    }

  def apply[A](a: A): Id[A] = pure(a)

  def map[A, B](a: Id[A])(f: A => B): Id[B] =
    pure(f(a.value))

  def flatten[A](a: Id[Id[A]]): Id[A] =
    a.value

  def flatMap[A, B](a: Id[A])(f: A => Id[B]): Id[B] =
    flatten(a.map(f))
}
