package indigoexts.collections

import indigo.runtime.Show

import indigo.Eq._
import indigo.shared.Eq

trait NonEmptyList[A] {

  val head: A
  val tail: List[A]

  def first: A = head

  def last: A =
    tail.reverse.headOption match {
      case Some(s) => s
      case None    => head
    }

  def ===(other: NonEmptyList[A])(implicit eq: Eq[A]): Boolean =
    NonEmptyList.equality(this, other)

  def length: Int =
    NonEmptyList.length(this)

  def reverse: NonEmptyList[A] =
    NonEmptyList.reverse(this)

  def toList: List[A] =
    head :: tail

  def foldLeft[Z](acc: Z)(f: (Z, A) => Z): Z =
    NonEmptyList.foldLeft(this)(acc)(f)

  def reduce(f: (A, A) => A): A =
    NonEmptyList.reduce(this)(f)

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

  override def toString: String =
    s"Nel[${head}][${tail.mkString(", ")}]"

  def mkString: String =
    mkString("")

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def mkString(separator: String): String =
    head.toString + separator + tail.mkString(separator)

}

object NonEmptyList {

  implicit def showNonEmptyList[A](implicit showA: Show[A]): Show[NonEmptyList[A]] =
    Show.create { l =>
      val s = l.map(a => showA.show(a))
      s"Nel[${s.head}][${s.tail.mkString(", ")}]"
    }

  def apply[A](head: A, tail: A*): NonEmptyList[A] =
    apply(head, tail.toList)

  def apply[A](headItem: A, tailItems: List[A]): NonEmptyList[A] =
    new NonEmptyList[A] {
      val head: A       = headItem
      val tail: List[A] = tailItems
    }

  def unapply[A](nel: NonEmptyList[A]): Option[(A, List[A])] =
    Option((nel.head, nel.tail))

  def point[A](a: A): NonEmptyList[A] =
    apply(a, List.empty[A])

  def equality[A](a: NonEmptyList[A], b: NonEmptyList[A])(implicit eq: Eq[A]): Boolean =
    a.length === b.length && a.zip(b).forall(as => eq.equal(as._1, as._2))

  def length[A](fa: NonEmptyList[A]): Int =
    fa.tail.length + 1

  def reverse[A](fa: NonEmptyList[A]): NonEmptyList[A] =
    fa.tail.reverse match {
      case Nil =>
        fa

      case x :: xs =>
        apply(x, xs :+ fa.head)
    }

  def map[A, B](fa: NonEmptyList[A])(f: A => B): NonEmptyList[B] =
    apply(f(fa.head), fa.tail.map(f))

  def combine[A](fa: NonEmptyList[A])(fb: NonEmptyList[A]): NonEmptyList[A] =
    apply(fa.head, fa.tail ++ fb.toList)

  def flatten[A](fa: NonEmptyList[NonEmptyList[A]]): NonEmptyList[A] =
    fa.tail.foldLeft(fa.head)(_ ++ _)

  def flatMap[A, B](fa: NonEmptyList[A])(f: A => NonEmptyList[B]): NonEmptyList[B] =
    flatten(map(fa)(f))

  def foldLeft[A, Z](fa: NonEmptyList[A])(acc: Z)(f: (Z, A) => Z): Z =
    fa.tail.foldLeft(f(acc, fa.head))(f)

  def reduce[A](fa: NonEmptyList[A])(f: (A, A) => A): A =
    fa.tail match {
      case Nil =>
        fa.head

      case x :: xs =>
        foldLeft(NonEmptyList(x, xs))(fa.head)(f)
    }

  def append[A](fa: NonEmptyList[A])(next: A): NonEmptyList[A] =
    apply(fa.head, fa.tail :+ next)

  def cons[A](fa: NonEmptyList[A])(first: A): NonEmptyList[A] =
    apply(first, fa.head :: fa.tail)

  def zipWithIndex[A](fa: NonEmptyList[A]): NonEmptyList[(A, Int)] =
    apply((fa.head, 0), fa.tail.zipWithIndex.map(p => (p._1, p._2 + 1)))

  def zip[A, B](fa: NonEmptyList[A], fb: NonEmptyList[B]): NonEmptyList[(A, B)] =
    apply((fa.head, fb.head), fa.tail.zip(fb.tail))

  def forall[A](fa: NonEmptyList[A])(p: A => Boolean): Boolean =
    fa.toList.forall(p)

  def find[A](fa: NonEmptyList[A])(p: A => Boolean): Option[A] =
    fa.toList.find(p)

  def exists[A](fa: NonEmptyList[A])(p: A => Boolean): Boolean =
    fa.toList.exists(p)

}
