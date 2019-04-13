package indigoexts.abstractions

import indigo.shared.EqualTo
import indigo.shared.AsString

trait Id[A] {
  val value: A

  def map[B](f: A => B): Id[B] =
    Id.map[A, B](Id.pure(value))(f)

  def flatMap[B](f: A => Id[B]): Id[B] =
    Id.flatMap[A, B](Id.pure(value))(f)
}

object Id {

  implicit def eq[A](implicit eqA: EqualTo[A]): EqualTo[Id[A]] =
    EqualTo.create((a, b) => eqA.equal(a.value, b.value))

  implicit def show[A](implicit showA: AsString[A]): AsString[Id[A]] =
    AsString.create(a => s"Id(${showA.show(a.value)})")

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
