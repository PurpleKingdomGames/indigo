package indigo.shared.collections

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.control.NonFatal

import scalajs.js
import scalajs.js.JSConverters.*

/** Batch is a really thin wrapper over `js.Array` to replace `List` on the Indigo APIs. Its purpose is to provide fast
  * scene construction and fast conversion back to js.Array for the engine to use. Most operations that require any sort
  * of traversal are performed by flattening the structure and delegated to `js.Array`. In practice, scene construction
  * is mostly about building the structure, so the penalty is acceptable, and still faster than using `List`.
  */
sealed trait Batch[+A]:
  private lazy val _jsArray: js.Array[A] = toJSArray

  def head: A
  def headOption: Option[A]
  def last: A
  def lastOption: Option[A]
  def isEmpty: Boolean
  def size: Int
  def toJSArray[B >: A]: js.Array[B]

  def length: Int                  = size
  def lengthCompare(len: Int): Int = _jsArray.lengthCompare(len)

  def ++[B >: A](other: Batch[B]): Batch[B] =
    if this.isEmpty then other
    else if other.isEmpty then this
    else Batch.Combine(this, other)

  def |+|[B >: A](other: Batch[B]): Batch[B] =
    this ++ other

  def ::[B >: A](value: B): Batch[B] =
    Batch(value) ++ this

  def +:[B >: A](value: B): Batch[B] =
    Batch(value) ++ this

  def :+[B >: A](value: B): Batch[B] =
    this ++ Batch(value)

  def apply(index: Int): A =
    _jsArray(index)

  def collect[B >: A, C](f: PartialFunction[B, C]): Batch[C] =
    Batch.Wrapped(_jsArray.collect(f))

  def collectFirst[B >: A, C](f: PartialFunction[B, C]): Option[C] =
    _jsArray.collectFirst(f)

  def compact[B >: A]: Batch.Wrapped[B] =
    Batch.Wrapped(_jsArray.asInstanceOf[js.Array[B]])

  def contains[B >: A](p: B): Boolean =
    given CanEqual[B, B] = CanEqual.derived
    _jsArray.exists(_ == p)

  def distinct: Batch[A] =
    Batch(_jsArray.distinct)

  def distinctBy[B](f: A => B): Batch[A] =
    Batch(_jsArray.distinctBy(f))

  def take(n: Int): Batch[A] =
    Batch.Wrapped(_jsArray.take(n))

  def takeRight(n: Int): Batch[A] =
    Batch.Wrapped(_jsArray.takeRight(n))

  def takeWhile(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_jsArray.takeWhile(p))

  def drop(count: Int): Batch[A] =
    Batch.Wrapped(_jsArray.drop(count))

  def dropRight(count: Int): Batch[A] =
    Batch.Wrapped(_jsArray.dropRight(count))

  def dropWhile(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_jsArray.dropWhile(p))

  def exists(p: A => Boolean): Boolean =
    _jsArray.exists(p)

  def find(p: A => Boolean): Option[A] =
    _jsArray.find(p)

  def filter(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_jsArray.filter(p))

  def filterNot(p: A => Boolean): Batch[A] =
    Batch.Wrapped(_jsArray.filterNot(p))

  def flatMap[B](f: A => Batch[B]): Batch[B] =
    Batch.Wrapped(toJSArray.flatMap(v => f(v).toJSArray))

  def flatten[B](using asBatch: A => Batch[B]): Batch[B] =
    flatMap(asBatch)

  def forall(p: A => Boolean): Boolean =
    _jsArray.forall(p)

  def fold[B >: A](z: B)(f: (B, B) => B): B =
    _jsArray.fold(z)(f)

  def foldLeft[B](z: B)(f: (B, A) => B): B =
    _jsArray.foldLeft(z)(f)

  def foldRight[B](z: B)(f: (A, B) => B): B =
    _jsArray.foldRight(z)(f)

  def foreach(f: A => Unit): Unit =
    _jsArray.foreach(f)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def foreachWithIndex(f: (A, Int) => Unit): Unit =
    var idx: Int = 0
    foreach { v =>
      f(v, idx)
      idx = idx + 1
    }

  def groupBy[K](f: A => K): Map[K, Batch[A]] =
    _jsArray.groupBy(f).map(p => (p._1, Batch(p._2)))

  def insert[B >: A](index: Int, value: B): Batch[B] =
    val p = _jsArray.splitAt(index)
    Batch((p._1 :+ value) ++ p._2)

  def lift(index: Int): Option[A] =
    _jsArray.lift(index)

  def padTo[B >: A](len: Int, elem: B): Batch[B] =
    Batch(_jsArray.padTo(len, elem))

  def partition(p: A => Boolean): (Batch[A], Batch[A]) =
    val (a, b) = _jsArray.partition(p)
    (Batch.Wrapped(a), Batch.Wrapped(b))

  def map[B](f: A => B): Batch[B] =
    Batch.Wrapped(_jsArray.map(f))

  def maxBy[B](f: A => B)(using ord: Ordering[B]): A =
    _jsArray.maxBy(f)(ord)

  def maxByOption[B](f: A => B)(using ord: Ordering[B]): Option[A] =
    Option.when(_jsArray.nonEmpty)(_jsArray.maxBy(f)(ord))

  def minBy[B](f: A => B)(using ord: Ordering[B]): A =
    _jsArray.minBy(f)(ord)

  def minByOption[B](f: A => B)(using ord: Ordering[B]): Option[A] =
    Option.when(_jsArray.nonEmpty)(_jsArray.minBy(f)(ord))

  /** Converts the batch into a String`
    * @return
    *   `String`
    */
  def mkString: String =
    toJSArray.mkString

  /** Converts the batch into a String
    * @param separator
    *   A string to add between the elements
    * @return
    *   `String`
    */
  def mkString(separator: String): String =
    toJSArray.mkString(separator)

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
    toJSArray.mkString(prefix, separator, suffix)

  def nonEmpty: Boolean =
    !isEmpty

  def reduce[B >: A](f: (B, B) => B): B =
    _jsArray.reduce(f)

  def reverse: Batch[A] =
    Batch.Wrapped(_jsArray.reverse)

  def sortBy[B](f: A => B)(implicit ord: Ordering[B]): Batch[A] =
    Batch.Wrapped(_jsArray.sortBy(f))

  def sorted[B >: A](implicit ord: Ordering[B]): Batch[A] =
    Batch.Wrapped(_jsArray.sorted)

  def sortWith(f: (A, A) => Boolean): Batch[A] =
    Batch.Wrapped(_jsArray.sortWith(f))

  def splitAt(n: Int): (Batch[A], Batch[A]) =
    val p = _jsArray.splitAt(n)
    (Batch.Wrapped(p._1), Batch.Wrapped(p._2))

  def sum[B >: A](implicit num: Numeric[B]): B =
    _jsArray.sum

  def tail: Batch[A] =
    Batch.Wrapped(_jsArray.tail)

  def tailOrEmpty: Batch[A] =
    if _jsArray.isEmpty then Batch.empty
    else Batch.Wrapped(_jsArray.tail)

  def tailOption: Option[Batch[A]] =
    if _jsArray.isEmpty then None
    else Option(Batch.Wrapped(_jsArray.tail))

  def uncons: Option[(A, Batch[A])] =
    headOption.map(a => (a, tailOrEmpty))

  def toArray[B >: A: ClassTag]: Array[B] =
    _jsArray.asInstanceOf[js.Array[B]].toArray

  def toList: List[A] =
    _jsArray.toList

  def toMap[K, V](using A <:< (K, V)) =
    _jsArray.toMap

  def toSet[B >: A]: Set[B] =
    _jsArray.toSet

  override def toString: String =
    "Batch(" + _jsArray.mkString(", ") + ")"

  def update[B >: A](index: Int, value: B): Batch[B] =
    val p = _jsArray.splitAt(index)
    Batch((p._1 :+ value) ++ p._2.tail)

  def zipWithIndex: Batch[(A, Int)] =
    Batch.Wrapped(_jsArray.zipWithIndex)

  def zip[B](other: Batch[B]): Batch[(A, B)] =
    Batch.Wrapped(_jsArray.zip(other.toJSArray))

  override def hashCode(): Int =
    _jsArray.foldLeft(31)((acc, v) => 31 * acc + v.hashCode())

object Batch:

  extension [A](s: Seq[A]) def toBatch: Batch[A] = Batch.fromSeq(s)

  given CanEqual[Batch[?], Batch[?]]         = CanEqual.derived
  given CanEqual[Batch[?], Batch.Combine[?]] = CanEqual.derived
  given CanEqual[Batch[?], Batch.Wrapped[?]] = CanEqual.derived

  given CanEqual[Batch.Combine[?], Batch[?]]         = CanEqual.derived
  given CanEqual[Batch.Combine[?], Batch.Combine[?]] = CanEqual.derived
  given CanEqual[Batch.Combine[?], Batch.Wrapped[?]] = CanEqual.derived

  given CanEqual[Batch.Wrapped[?], Batch[?]]         = CanEqual.derived
  given CanEqual[Batch.Wrapped[?], Batch.Combine[?]] = CanEqual.derived
  given CanEqual[Batch.Wrapped[?], Batch.Wrapped[?]] = CanEqual.derived

  def apply[A](value: A): Batch[A] =
    Wrapped(js.Array(value))

  def apply[A](values: js.Array[A]): Batch[A] =
    Wrapped(values)

  def apply[A](values: A*): Batch[A] =
    Wrapped(values.toJSArray)

  def unapplySeq[A](b: Batch[A]): Seq[A] =
    b.toList

  object ==: {
    def unapply[A](b: Batch[A]): Option[(A, Batch[A])] =
      if b.isEmpty then None
      else Some(b.head -> b.tail)
  }

  object :== {
    def unapply[A](b: Batch[A]): Option[(Batch[A], A)] =
      if b.isEmpty then None
      else
        val r = b.reverse
        Some(r.tail.reverse -> r.head)
  }

  def fill[A](n: Int)(elem: => A): Batch[A] =
    Batch.fromList(List.fill[A](n)(elem))

  def fromJSArray[A](values: js.Array[A]): Batch[A] =
    Wrapped(values)

  def fromArray[A](values: Array[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def fromList[A](values: List[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def fromSet[A](values: Set[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def fromSeq[A](values: Seq[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def fromIndexedSeq[A](values: IndexedSeq[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def fromIterator[A](values: Iterator[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def fromMap[K, V](values: Map[K, V]): Batch[(K, V)] =
    Wrapped(values.toJSArray)

  def fromOption[A](value: Option[A]): Batch[A] =
    Wrapped(value.toJSArray)

  def fromRange[A](value: Range): Batch[Int] =
    Wrapped(value.toJSArray)

  def empty[A]: Batch[A] =
    Batch()

  def combine[A](batch1: Batch[A], batch2: Batch[A]): Batch[A] =
    batch1 ++ batch2

  def combineAll[A](batches: Batch[A]*): Batch[A] =
    batches.foldLeft(Batch.empty[A])(_ ++ _)

  private[collections] final case class Combine[A](batch1: Batch[A], batch2: Batch[A]) extends Batch[A]:
    val isEmpty: Boolean = batch1.isEmpty && batch2.isEmpty

    export batch1.head
    export batch1.headOption

    def last: A =
      if batch2.isEmpty then batch1.last else batch2.last

    def lastOption: Option[A] =
      if batch2.isEmpty then batch1.lastOption else batch2.lastOption

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
    def toJSArray[B >: A]: js.Array[B] =
      val arr = new js.Array[B](size)

      @tailrec
      def rec(remaining: List[Batch[A]], i: Int): Unit =
        remaining match
          case Nil =>
            ()

          case Batch.Combine(c1, c2) :: xs =>
            rec(c1 :: c2 :: xs, i)

          case Batch.Wrapped(vs) :: xs =>
            val count = vs.size
            var j     = 0

            while (j < count) {
              arr(i + j) = vs(j)
              j = j + 1
            }

            rec(xs, i + count)

      rec(List(batch1, batch2), 0)
      arr

    lazy val size: Int = batch1.size + batch2.size

    override def equals(that: Any): Boolean =
      given CanEqual[Combine[?], Any] = CanEqual.derived
      given CanEqual[Wrapped[?], Any] = CanEqual.derived
      given CanEqual[A, A]            = CanEqual.derived

      try
        that match

          case c @ Combine(_, _) =>
            compact.values.sameElements(c.compact.values)

          case Wrapped(arr) =>
            compact.values.sameElements(arr)

      catch { case NonFatal(_) => false }

  private[collections] final case class Wrapped[A](values: js.Array[A]) extends Batch[A]:
    val isEmpty: Boolean               = values.isEmpty
    def head: A                        = values.head
    def headOption: Option[A]          = values.headOption
    def last: A                        = values.last
    def lastOption: Option[A]          = values.lastOption
    def toJSArray[B >: A]: js.Array[B] = values.asInstanceOf[js.Array[B]]

    lazy val size: Int = values.length

    override def equals(that: Any): Boolean =
      given CanEqual[Combine[?], Any] = CanEqual.derived
      given CanEqual[Wrapped[?], Any] = CanEqual.derived
      given CanEqual[A, A]            = CanEqual.derived

      try
        that match
          case c @ Combine(_, _) =>
            values.sameElements(c.compact.values)

          case Wrapped(arr) =>
            values.sameElements(arr)

      catch { case NonFatal(_) => false }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def sequenceOption[A](b: Batch[Option[A]]): Option[Batch[A]] =
    @tailrec
    def rec(remaining: Batch[Option[A]], acc: Batch[A]): Option[Batch[A]] =
      if remaining.isEmpty then Option(acc.reverse)
      else
        remaining match
          case None ==: xs =>
            rec(xs, acc)

          case Some(x) ==: xs =>
            rec(xs, x :: acc)

          case _ =>
            throw new Exception("Error encountered sequencing Batch[Option[A]]")

    rec(b, Batch.empty[A])
