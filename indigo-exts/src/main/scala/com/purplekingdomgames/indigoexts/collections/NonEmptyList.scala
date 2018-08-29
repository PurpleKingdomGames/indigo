package com.purplekingdomgames.indigoexts.collections

case class NonEmptyList[A](head: A, tail: List[A]) {

  def ===(other: NonEmptyList[A]): Boolean =
    NonEmptyList.equality(this, other)

  def length: Int =
    NonEmptyList.length(this)

  def toList: List[A] =
    head :: tail

  def foldLeft[Z](acc: Z)(f: (Z, A) => Z): Z =
    NonEmptyList.foldLeft(this)(acc)(f)

  def reduceLeft(f: (A, A) => A): A =
    NonEmptyList.reduceLeft(this)(f)

  def :+(next: A): NonEmptyList[A] =
    NonEmptyList.append(this)(next)

  def ::(first: A): NonEmptyList[A] =
    NonEmptyList.cons(this)(first)

  def ++(other: NonEmptyList[A]): NonEmptyList[A] =
    NonEmptyList.combine(this)(other)

  def map[B](f: A => B): NonEmptyList[B] =
    NonEmptyList.map(this)(f)

  def flatMap[B](f: A => NonEmptyList[B]): NonEmptyList[B] =
    NonEmptyList.flatMap(this)(f)

  def zipWithIndex: NonEmptyList[(A, Int)] =
    NonEmptyList.zipWithIndex(this)

  def zip[B](other: NonEmptyList[B]): NonEmptyList[(A, B)] =
    NonEmptyList.zip(this, other)

  def forall(p: A => Boolean): Boolean =
    NonEmptyList.forall(this)(p)

  def find(p: A => Boolean): Option[A] =
    NonEmptyList.find(this)(p)

  def exists(p: A => Boolean): Boolean =
    NonEmptyList.exists(this)(p)

}

object NonEmptyList {

  def apply[A](a: A): NonEmptyList[A] =
    NonEmptyList(a, List.empty[A])

  def point[A](a: A): NonEmptyList[A] =
    apply(a)

  def equality[A](a: NonEmptyList[A], b: NonEmptyList[A]): Boolean =
    a.length == b.length && a.zip(b).forall(as => as._1 == as._2)

  def length[A](fa: NonEmptyList[A]): Int =
    fa.tail.length + 1

  def map[A, B](fa: NonEmptyList[A])(f: A => B): NonEmptyList[B] =
    NonEmptyList(f(fa.head), fa.tail.map(f))

  def combine[A](fa: NonEmptyList[A])(fb: NonEmptyList[A]): NonEmptyList[A] =
    NonEmptyList(fa.head, fa.tail ++ fb.toList)

  def flatten[A](fa: NonEmptyList[NonEmptyList[A]]): NonEmptyList[A] =
    fa.tail.foldLeft(fa.head)(_ ++ _)

  def flatMap[A, B](fa: NonEmptyList[A])(f: A => NonEmptyList[B]): NonEmptyList[B] =
    flatten(map(fa)(f))

  def foldLeft[A, Z](fa: NonEmptyList[A])(acc: Z)(f: (Z, A) => Z): Z =
    fa.tail.foldLeft(f(acc, fa.head))(f)

  def reduceLeft[A](fa: NonEmptyList[A])(f: (A, A) => A): A =
    foldLeft(fa)(fa.head)(f)

  def append[A](fa: NonEmptyList[A])(next: A): NonEmptyList[A] =
    NonEmptyList(fa.head, fa.tail :+ next)

  def cons[A](fa: NonEmptyList[A])(first: A): NonEmptyList[A] =
    NonEmptyList(first, fa.head :: fa.tail)

  def zipWithIndex[A](fa: NonEmptyList[A]): NonEmptyList[(A, Int)] =
    NonEmptyList((fa.head, 0), fa.tail.zipWithIndex.map(p => (p._1, p._2 + 1)))

  def zip[A, B](fa: NonEmptyList[A], fb: NonEmptyList[B]): NonEmptyList[(A, B)] =
    NonEmptyList((fa.head, fb.head), fa.tail.zip(fb.tail))

  def forall[A](fa: NonEmptyList[A])(p: A => Boolean): Boolean =
    fa.toList.forall(p)

  def find[A](fa: NonEmptyList[A])(p: A => Boolean): Option[A] =
    fa.toList.find(p)

  def exists[A](fa: NonEmptyList[A])(p: A => Boolean): Boolean =
    fa.toList.exists(p)

}
