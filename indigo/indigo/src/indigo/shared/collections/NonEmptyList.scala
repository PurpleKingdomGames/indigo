package indigo.shared.collections

import scala.annotation.tailrec

/** An ordered list-type object that requires there to always be at least one element present, ruling out the
  * possibility of unsafely accessing the `head` element.
  * @tparam A
  *   The type of element to be stored in the list.
  */
final case class NonEmptyList[A](head: A, tail: List[A]) derives CanEqual:

  /** Alias for `head`
    * @return
    *   `A`
    */
  def first: A = head

  /** Returns the last element in the list
    * @return
    *   `A`
    */
  def last: A =
    tail.reverse.headOption match {
      case Some(s) => s
      case _       => head
    }

  /** A count of the elements in the list
    * @return
    *   Int
    */
  def length: Int =
    NonEmptyList.length(this)

  /** Reverse the order of the list
    * @return
    *   `NonEmptyList[A]`
    */
  def reverse: NonEmptyList[A] =
    NonEmptyList.reverse(this)

  /** Converts the NonEmptyList back to a regular List.
    * @return
    */
  def toList: List[A] =
    head :: tail

  /** Converts the NonEmptyList back to a regular Batch.
    * @return
    */
  def toBatch: Batch[A] =
    head :: Batch.fromList(tail)

  /** `foldLeft` differs from reduce it two important ways:
    *   1. It has an initial value onto which all other values are applied
    *   1. It does not require the result type to be the same as the list type.
    * @example
    *   `NonEmptyList(1, 2, 3)("")((a, b) => a + b) results in "123"`
    * @param acc
    *   The initial accumulator value to accumulate against
    * @param f
    *   A function for combining the accumulator and the next value
    * @tparam Z
    *   The accumulator type
    * @return
    *   the final accumulated value
    */
  def foldLeft[Z](acc: Z)(f: (Z, A) => Z): Z =
    NonEmptyList.foldLeft(this)(acc)(f)

  /** @example
    *   `NonEmptyList(1, 2, 3)((a, b) => a + b) results in 6`
    * @param f
    *   a function for combining to `A`'s into a single `A`
    * @return
    *   The final `A` value
    */
  def reduce(f: (A, A) => A): A =
    NonEmptyList.reduce(this)(f)

  /** Append an element
    * @param next
    *   The next element of the same type
    * @return
    *   `NonEmptyList[A]`
    */
  def :+(next: A): NonEmptyList[A] =
    NonEmptyList.append(this)(next)

  /** Prepend an element
    * @param first
    *   The new head element of the same type
    * @return
    *   `NonEmptyList[A]`
    */
  def ::(first: A): NonEmptyList[A] =
    NonEmptyList.cons(this)(first)

  /** Concatenate two `NonEmptyList`'s together
    * @param other
    *   A second NonEmptyList of the same type
    * @return
    *   A new NonEmptyList containing the elements of both lists
    */
  def ++(other: NonEmptyList[A]): NonEmptyList[A] =
    NonEmptyList.combine(this)(other)

  /** Concatenate a `NonEmptyList` with a `List`
    * @param other
    *   A List of the same type
    * @return
    *   A new NonEmptyList containing the elements of both lists
    */
  def ++(other: List[A]): NonEmptyList[A] =
    NonEmptyList.combineWithList(this)(other)

  /** Apply a function `f` to each element of the list to produce a new list.
    * @example
    *   `NonEmptyList(1, 2, 3).map(_ * 10)` results in `NonEmptyList(10, 20, 30)`
    * @param f
    *   function to apply to each element
    * @tparam B
    *   Resultant type of the new NonEmptyList
    * @return
    *   A NonEmptyList of a potentially different type
    */
  def map[B](f: A => B): NonEmptyList[B] =
    NonEmptyList.map(this)(f)

  /** Apply a function `f` to each element of the list to produce a new list. Differs from map because f produces
    * another NonEmptyList, which is then flattened. Useful in monadic comprehensions.
    * @example
    *   `NonEmptyList(1, 2, 3).flatMap(i => NonEmptyList(i * 10))` results in `NonEmptyList(10, 20, 30)`
    * @param f
    *   function to apply to each element
    * @tparam B
    *   Resultant type of the new NonEmptyList
    * @return
    *   A NonEmptyList of a potentially different type
    */
  def flatMap[B](f: A => NonEmptyList[B]): NonEmptyList[B] =
    NonEmptyList.flatMap(this)(f)

  /** @example
    *   `NonEmptyList("a", "b", "c").zipWithIndex` results in `NonEmptyList(("a", 0), ("b", 1), ("c",2))`
    * @return
    */
  def zipWithIndex: NonEmptyList[(A, Int)] =
    NonEmptyList.zipWithIndex(this)

  /** Takes two NonEmptyLists and creates a new NonEmptyList of the elements of both inputs tupled together.
    * @example
    *   `NonEmptyList("a", "b", "c").zip(NonEmptyList(1, 2, 3))` results in `NonEmptyList(("a", 1), ("b", 2), ("c", 3))`
    * @param other
    *   The second NonEmptyList to zip with.
    * @tparam B
    *   The type of the second NonEmptyList
    * @return
    *   `NonEmptyList[(A, B)]`
    */
  def zip[B](other: NonEmptyList[B]): NonEmptyList[(A, B)] =
    NonEmptyList.zip(this, other)

  /** Checks that a predicate holds for all elements
    * @param p
    *   Predicate function
    * @return
    *   Boolean
    */
  def forall(p: A => Boolean): Boolean =
    NonEmptyList.forall(this)(p)

  /** Search the NonEmptyList using a predicate and return the first element that matches
    * @param p
    *   Predicate, returns the first elements for which this predicate holds true
    * @return
    *   Optional A, if no match can be found None is returned.
    */
  def find(p: A => Boolean): Option[A] =
    NonEmptyList.find(this)(p)

  /** List find, but only returns a Boolean indicating if an element matching the predicate was found.
    * @param p
    *   Predicate function
    * @return
    *   Boolean
    */
  def exists(p: A => Boolean): Boolean =
    NonEmptyList.exists(this)(p)

  override def toString: String =
    s"NonEmptyList[$head][${tail.mkString(", ")}]"

  /** Delegates to `mkString(separator: String): String`
    * @return
    *   `String`
    */
  def mkString: String =
    mkString("")

  /** Converts the list into a String
    * @param separator
    *   A string to add between the elements
    * @return
    *   `String`
    */

  def mkString(separator: String): String =
    head.toString + separator + tail.mkString(separator)

object NonEmptyList:

  def apply[A](head: A, tail: A*): NonEmptyList[A] =
    pure(head, tail.toList)

  def pure[A](headItem: A, tailItems: List[A]): NonEmptyList[A] =
    NonEmptyList[A](headItem, tailItems)

  def point[A](a: A): NonEmptyList[A] =
    pure(a, List.empty[A])

  def fromList[A](l: List[A]): Option[NonEmptyList[A]] =
    l match {
      case x :: xs =>
        Some(pure(x, xs))

      case _ =>
        None
    }

  def fromBatch[A](b: Batch[A]): Option[NonEmptyList[A]] =
    fromList(b.toList)

  def fromNonEmptyBatch[A](neb: NonEmptyBatch[A]): NonEmptyList[A] =
    NonEmptyList(neb.head, neb.tail.toList)

  def length[A](fa: NonEmptyList[A]): Int =
    fa.tail.length + 1

  def reverse[A](fa: NonEmptyList[A]): NonEmptyList[A] =
    fa.tail.reverse match {
      case x :: xs =>
        pure(x, xs ++ List(fa.head))

      case _ =>
        fa
    }

  def map[A, B](fa: NonEmptyList[A])(f: A => B): NonEmptyList[B] =
    pure(f(fa.head), fa.tail.map(f))

  def combine[A](fa: NonEmptyList[A])(fb: NonEmptyList[A]): NonEmptyList[A] =
    pure(fa.head, fa.tail ++ fb.toList)

  def combineWithList[A](fa: NonEmptyList[A])(fb: List[A]): NonEmptyList[A] =
    pure(fa.head, fa.tail ++ fb.toList)

  def flatten[A](fa: NonEmptyList[NonEmptyList[A]]): NonEmptyList[A] =
    fa.tail.foldLeft(fa.head)(_ ++ _)

  def flatMap[A, B](fa: NonEmptyList[A])(f: A => NonEmptyList[B]): NonEmptyList[B] =
    flatten(map(fa)(f))

  def foldLeft[A, Z](fa: NonEmptyList[A])(acc: Z)(f: (Z, A) => Z): Z =
    fa.tail.foldLeft(f(acc, fa.head))(f)

  def reduce[A](fa: NonEmptyList[A])(f: (A, A) => A): A =
    fa.tail match {
      case x :: xs =>
        foldLeft(NonEmptyList.pure(x, xs))(fa.head)(f)

      case _ =>
        fa.head
    }

  def append[A](fa: NonEmptyList[A])(next: A): NonEmptyList[A] =
    pure(fa.head, fa.tail ++ List(next))

  def cons[A](fa: NonEmptyList[A])(first: A): NonEmptyList[A] =
    pure(first, fa.head :: fa.tail)

  def zipWithIndex[A](fa: NonEmptyList[A]): NonEmptyList[(A, Int)] =
    pure((fa.head, 0), fa.tail.zipWithIndex.map(p => (p._1, p._2 + 1)))

  def zip[A, B](fa: NonEmptyList[A], fb: NonEmptyList[B]): NonEmptyList[(A, B)] =
    pure((fa.head, fb.head), fa.tail.zip(fb.tail))

  def forall[A](fa: NonEmptyList[A])(p: A => Boolean): Boolean =
    fa.toList.forall(p)

  def find[A](fa: NonEmptyList[A])(p: A => Boolean): Option[A] =
    fa.toList.find(p)

  def exists[A](fa: NonEmptyList[A])(p: A => Boolean): Boolean =
    fa.toList.exists(p)

  def sequenceListOption[A](l: List[Option[A]]): Option[List[A]] =
    @tailrec
    def rec(remaining: List[Option[A]], acc: List[A]): Option[List[A]] =
      remaining match
        case Nil =>
          Some(acc.reverse)

        case None :: as =>
          rec(as, acc)

        case Some(a) :: as =>
          rec(as, a :: acc)

    rec(l, Nil)

  def sequenceOption[A](l: NonEmptyList[Option[A]]): Option[NonEmptyList[A]] =
    sequenceListOption(l.toList).flatMap(NonEmptyList.fromList)
