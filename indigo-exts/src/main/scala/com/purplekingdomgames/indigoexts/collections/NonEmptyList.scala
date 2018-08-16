package com.purplekingdomgames.indigoexts.collections

case class NonEmptyList[A](head: A, tail: List[A]) {
  def toList: List[A] =
    head :: tail

  def foldLeft[Z](acc: Z)(f: (Z, A) => Z): Z =
    tail.foldLeft(f(acc, head))(f)

  def reduceLeft(f: (A, A) => A): A =
    foldLeft(head)(f)

  def :+(next: A): NonEmptyList[A] =
    this.copy(tail = tail :+ next)

  def ::(first: A): NonEmptyList[A] =
    this.copy(first, head :: tail)

  def zipWithIndex: NonEmptyList[(A, Int)] =
    this.copy(
      head = (head, 0),
      tail.zipWithIndex.map(p => (p._1, p._2 + 1))
    )

  def map[B](f: A => B): NonEmptyList[B] =
    NonEmptyList(f(head), tail.map(f))

}
