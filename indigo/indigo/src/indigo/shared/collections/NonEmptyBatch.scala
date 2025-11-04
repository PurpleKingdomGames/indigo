package indigo.shared.collections

/** An ordered batch-type object that requires there to always be at least one element present, ruling out the
  * possibility of unsafely accessing the `head` element.
  * @tparam A
  *   The type of element to be stored in the batch.
  */
final case class NonEmptyBatch[A](head: A, tail: Batch[A]) derives CanEqual:

  /** Alias for `head`
    * @return
    *   `A`
    */
  def first: A = head

  /** Returns the last element in the batch
    * @return
    *   `A`
    */
  def last: A =
    tail.reverse.headOption match {
      case Some(s) => s
      case _       => head
    }

  /** A count of the elements in the batch
    * @return
    *   Int
    */
  def size: Int =
    NonEmptyBatch.size(this)

  /** A count of the elements in the batch
    * @return
    *   Int
    */
  def length: Int =
    size

  /** Reverse the order of the batch
    * @return
    *   `NonEmptyBatch[A]`
    */
  def reverse: NonEmptyBatch[A] =
    NonEmptyBatch.reverse(this)

  /** Converts the NonEmptyBatch back to a regular Batch.
    * @return
    */
  def toBatch: Batch[A] =
    head :: tail

  /** Converts the NonEmptyBatch back to a List.
    * @return
    */
  def toList: List[A] =
    head :: tail.toList

  /** `foldLeft` differs from reduce it two important ways:
    *   1. It has an initial value onto which all other values are applied
    *   1. It does not require the result type to be the same as the batch type.
    * @example
    *   `NonEmptyBatch(1, 2, 3)("")((a, b) => a + b) results in "123"`
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
    NonEmptyBatch.foldLeft(this)(acc)(f)

  /** @example
    *   `NonEmptyBatch(1, 2, 3)((a, b) => a + b) results in 6`
    * @param f
    *   a function for combining to `A`'s into a single `A`
    * @return
    *   The final `A` value
    */
  def reduce(f: (A, A) => A): A =
    NonEmptyBatch.reduce(this)(f)

  /** Append an element
    * @param next
    *   The next element of the same type
    * @return
    *   `NonEmptyBatch[A]`
    */
  def :+(next: A): NonEmptyBatch[A] =
    NonEmptyBatch.append(this)(next)

  /** Prepend an element
    * @param first
    *   The new head element of the same type
    * @return
    *   `NonEmptyBatch[A]`
    */
  def ::(first: A): NonEmptyBatch[A] =
    NonEmptyBatch.cons(this)(first)

  /** Concatenate two `NonEmptyBatch`'s together
    * @param other
    *   A second NonEmptyBatch of the same type
    * @return
    *   A new NonEmptyBatch containing the elements of both lists
    */
  def ++(other: NonEmptyBatch[A]): NonEmptyBatch[A] =
    NonEmptyBatch.combine(this)(other)

  /** Concatenate a `NonEmptyBatch` with a `Batch`
    * @param other
    *   A Batch of the same type
    * @return
    *   A new NonEmptyBatch containing the elements of both lists
    */
  def ++(other: Batch[A]): NonEmptyBatch[A] =
    NonEmptyBatch.combineWithBatch(this)(other)

  /** Apply a function `f` to each element of the batch to produce a new batch.
    * @example
    *   `NonEmptyBatch(1, 2, 3).map(_ * 10)` results in `NonEmptyBatch(10, 20, 30)`
    * @param f
    *   function to apply to each element
    * @tparam B
    *   Resultant type of the new NonEmptyBatch
    * @return
    *   A NonEmptyBatch of a potentially different type
    */
  def map[B](f: A => B): NonEmptyBatch[B] =
    NonEmptyBatch.map(this)(f)

  /** Apply a function `f` to each element of the batch to produce a new batch. Differs from map because f produces
    * another NonEmptyBatch, which is then flattened. Useful in monadic comprehensions.
    * @example
    *   `NonEmptyBatch(1, 2, 3).flatMap(i => NonEmptyBatch(i * 10))` results in `NonEmptyBatch(10, 20, 30)`
    * @param f
    *   function to apply to each element
    * @tparam B
    *   Resultant type of the new NonEmptyBatch
    * @return
    *   A NonEmptyBatch of a potentially different type
    */
  def flatMap[B](f: A => NonEmptyBatch[B]): NonEmptyBatch[B] =
    NonEmptyBatch.flatMap(this)(f)

  /** @example
    *   `NonEmptyBatch("a", "b", "c").zipWithIndex` results in `NonEmptyBatch(("a", 0), ("b", 1), ("c",2))`
    * @return
    */
  def zipWithIndex: NonEmptyBatch[(A, Int)] =
    NonEmptyBatch.zipWithIndex(this)

  /** Takes two NonEmptyBatchs and creates a new NonEmptyBatch of the elements of both inputs tupled together.
    * @example
    *   `NonEmptyBatch("a", "b", "c").zip(NonEmptyBatch(1, 2, 3))` results in `NonEmptyBatch(("a", 1), ("b", 2), ("c",
    *   3))`
    * @param other
    *   The second NonEmptyBatch to zip with.
    * @tparam B
    *   The type of the second NonEmptyBatch
    * @return
    *   `NonEmptyBatch[(A, B)]`
    */
  def zip[B](other: NonEmptyBatch[B]): NonEmptyBatch[(A, B)] =
    NonEmptyBatch.zip(this, other)

  /** Checks that a predicate holds for all elements
    * @param p
    *   Predicate function
    * @return
    *   Boolean
    */
  def forall(p: A => Boolean): Boolean =
    NonEmptyBatch.forall(this)(p)

  /** Search the NonEmptyBatch using a predicate and return the first element that matches
    * @param p
    *   Predicate, returns the first elements for which this predicate holds true
    * @return
    *   Optional A, if no match can be found None is returned.
    */
  def find(p: A => Boolean): Option[A] =
    NonEmptyBatch.find(this)(p)

  /** Batch find, but only returns a Boolean indicating if an element matching the predicate was found.
    * @param p
    *   Predicate function
    * @return
    *   Boolean
    */
  def exists(p: A => Boolean): Boolean =
    NonEmptyBatch.exists(this)(p)

  override def toString: String =
    s"NonEmptyBatch[$head][${tail.toJSArray.mkString(", ")}]"

  /** Delegates to `mkString(separator: String): String`
    * @return
    *   `String`
    */
  def mkString: String =
    mkString("")

  /** Converts the batch into a String
    * @param separator
    *   A string to add between the elements
    * @return
    *   `String`
    */
  def mkString(separator: String): String =
    head.toString + separator + tail.toJSArray.mkString(separator)

  /** Converts the batch into a String
    * @param prefix
    *   A string to add before the elements
    * @param separator
    *   A string to add between the elements
    * @param suffix
    *   A string to add after the elements
    * @return
    *   `String`
    */
  def mkString(prefix: String, separator: String, suffix: String): String =
    prefix + head.toString + separator + tail.toJSArray.mkString(separator) + suffix

  override def equals(that: Any): Boolean =
    given CanEqual[A, A] = CanEqual.derived

    if that.isInstanceOf[NonEmptyBatch[?]] then
      try
        val b = that.asInstanceOf[NonEmptyBatch[A]]
        b.head == head && b.tail == tail
      catch _ => false
    else false

object NonEmptyBatch:

  object ==: {
    def unapply[A](b: NonEmptyBatch[A]): Option[(A, Batch[A])] =
      Some(b.head -> b.tail)
  }

  def apply[A](head: A, tail: A*): NonEmptyBatch[A] =
    pure(head, Batch.fromSeq(tail))

  def pure[A](headItem: A, tailItems: Batch[A]): NonEmptyBatch[A] =
    NonEmptyBatch[A](headItem, tailItems)

  def point[A](a: A): NonEmptyBatch[A] =
    pure(a, Batch.empty[A])

  def fromBatch[A](b: Batch[A]): Option[NonEmptyBatch[A]] =
    if b.isEmpty then None
    else Some(pure(b.head, b.tail))

  def fromList[A](l: List[A]): Option[NonEmptyBatch[A]] =
    fromBatch(Batch.fromList(l))

  def fromNonEmptyList[A](nel: NonEmptyList[A]): NonEmptyBatch[A] =
    NonEmptyBatch(nel.head, Batch.fromList(nel.tail))

  def size[A](fa: NonEmptyBatch[A]): Int =
    fa.tail.size + 1

  def reverse[A](fa: NonEmptyBatch[A]): NonEmptyBatch[A] =
    NonEmptyBatch.fromBatch(fa.toBatch.reverse).get // safe, we know it's non-empty

  def map[A, B](fa: NonEmptyBatch[A])(f: A => B): NonEmptyBatch[B] =
    pure(f(fa.head), fa.tail.map(f))

  def combine[A](fa: NonEmptyBatch[A])(fb: NonEmptyBatch[A]): NonEmptyBatch[A] =
    pure(fa.head, fa.tail ++ fb.toBatch)

  def combineWithBatch[A](fa: NonEmptyBatch[A])(fb: Batch[A]): NonEmptyBatch[A] =
    pure(fa.head, fa.tail ++ fb)

  def flatten[A](fa: NonEmptyBatch[NonEmptyBatch[A]]): NonEmptyBatch[A] =
    fa.tail.foldLeft(fa.head)(_ ++ _)

  def flatMap[A, B](fa: NonEmptyBatch[A])(f: A => NonEmptyBatch[B]): NonEmptyBatch[B] =
    flatten(map(fa)(f))

  def foldLeft[A, Z](fa: NonEmptyBatch[A])(acc: Z)(f: (Z, A) => Z): Z =
    fa.tail.foldLeft(f(acc, fa.head))(f)

  def reduce[A](fa: NonEmptyBatch[A])(f: (A, A) => A): A =
    fa.toBatch.reduce(f)

  def append[A](fa: NonEmptyBatch[A])(next: A): NonEmptyBatch[A] =
    pure(fa.head, fa.tail ++ Batch(next))

  def cons[A](fa: NonEmptyBatch[A])(first: A): NonEmptyBatch[A] =
    pure(first, fa.head :: fa.tail)

  def zipWithIndex[A](fa: NonEmptyBatch[A]): NonEmptyBatch[(A, Int)] =
    pure((fa.head, 0), fa.tail.zipWithIndex.map(p => (p._1, p._2 + 1)))

  def zip[A, B](fa: NonEmptyBatch[A], fb: NonEmptyBatch[B]): NonEmptyBatch[(A, B)] =
    pure((fa.head, fb.head), fa.tail.zip(fb.tail))

  def forall[A](fa: NonEmptyBatch[A])(p: A => Boolean): Boolean =
    fa.toBatch.forall(p)

  def find[A](fa: NonEmptyBatch[A])(p: A => Boolean): Option[A] =
    fa.toBatch.find(p)

  def exists[A](fa: NonEmptyBatch[A])(p: A => Boolean): Boolean =
    fa.toBatch.exists(p)

  def sequenceOption[A](b: NonEmptyBatch[Option[A]]): Option[NonEmptyBatch[A]] =
    Batch.sequenceOption(b.toBatch).flatMap(NonEmptyBatch.fromBatch)
