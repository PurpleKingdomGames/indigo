package indigo.shared.collections

import scala.annotation.tailrec
import scala.annotation.targetName
import scala.reflect.ClassTag

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
    Batch.Singleton(value) ++ this

  def +:[B >: A](value: B): Batch[B] =
    Batch.Singleton(value) ++ this

  def :+[B >: A](value: B): Batch[B] =
    this ++ Batch.Singleton(value)

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

  def drop(count: Int): Batch[A] =
    Batch.Wrapped(_jsArray.drop(count))

  def dropRight(count: Int): Batch[A] =
    Batch.Wrapped(_jsArray.dropRight(count))

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

  def partition(p: A => Boolean): (Batch[A], Batch[A]) =
    val (a, b) = _jsArray.partition(p)
    (Batch.Wrapped(a), Batch.Wrapped(b))

  def map[B](f: A => B): Batch[B] =
    Batch.Wrapped(_jsArray.map(f))

  /** Delegates to `mkString(separator: String): String`
    * @return
    *   `String`
    */
  def mkString: String =
    if isEmpty then ""
    else mkString("")

  /** Converts the batch into a String
    * @param separator
    *   A string to add between the elements
    * @return
    *   `String`
    */
  def mkString(separator: String): String =
    if isEmpty then ""
    else head.toString + separator + tail.toJSArray.mkString(separator)

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
    if isEmpty then prefix + suffix
    else prefix + head.toString + separator + tail.toJSArray.mkString(separator) + suffix

  def nonEmpty: Boolean =
    !isEmpty

  def reduce[B >: A](f: (B, B) => B): B =
    _jsArray.reduce(f)

  def reverse: Batch[A] =
    Batch.Wrapped(_jsArray.reverse)

  def sortBy[B](f: A => B)(implicit ord: Ordering[B]): Batch[A] =
    Batch.Wrapped(_jsArray.sortBy(f))

  def sortWith(f: (A, A) => Boolean): Batch[A] =
    Batch.Wrapped(_jsArray.sortWith(f))

  def splitAt(n: Int): (Batch[A], Batch[A]) =
    val p = _jsArray.splitAt(n)
    (Batch.Wrapped(p._1), Batch.Wrapped(p._2))

  def sum[B >: A](implicit num: Numeric[B]): B =
    _jsArray.sum

  def tail: Batch[A] =
    if _jsArray.isEmpty then Batch.empty
    else Batch.Wrapped(_jsArray.tail)

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

object Batch:

  extension [A](s: Seq[A]) def toBatch: Batch[A] = Batch.fromSeq(s)

  object Unapply:
    object :: {
      def unapply[A](b: Batch[A]): Option[(A, Batch[A])] =
        if b.isEmpty then None
        else Some((b.head, b.tail))
    }

  given CanEqual[Batch[_], Batch[_]]           = CanEqual.derived
  given CanEqual[Batch[_], Batch.Empty.type]   = CanEqual.derived
  given CanEqual[Batch[_], Batch.Singleton[_]] = CanEqual.derived
  given CanEqual[Batch[_], Batch.Combine[_]]   = CanEqual.derived
  given CanEqual[Batch[_], Batch.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Batch.Empty.type, Batch[_]]           = CanEqual.derived
  given CanEqual[Batch.Empty.type, Batch.Empty.type]   = CanEqual.derived
  given CanEqual[Batch.Empty.type, Batch.Singleton[_]] = CanEqual.derived
  given CanEqual[Batch.Empty.type, Batch.Combine[_]]   = CanEqual.derived
  given CanEqual[Batch.Empty.type, Batch.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Batch.Singleton[_], Batch[_]]           = CanEqual.derived
  given CanEqual[Batch.Singleton[_], Batch.Empty.type]   = CanEqual.derived
  given CanEqual[Batch.Singleton[_], Batch.Singleton[_]] = CanEqual.derived
  given CanEqual[Batch.Singleton[_], Batch.Combine[_]]   = CanEqual.derived
  given CanEqual[Batch.Singleton[_], Batch.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Batch.Combine[_], Batch[_]]           = CanEqual.derived
  given CanEqual[Batch.Combine[_], Batch.Empty.type]   = CanEqual.derived
  given CanEqual[Batch.Combine[_], Batch.Singleton[_]] = CanEqual.derived
  given CanEqual[Batch.Combine[_], Batch.Combine[_]]   = CanEqual.derived
  given CanEqual[Batch.Combine[_], Batch.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Batch.Wrapped[_], Batch[_]]           = CanEqual.derived
  given CanEqual[Batch.Wrapped[_], Batch.Empty.type]   = CanEqual.derived
  given CanEqual[Batch.Wrapped[_], Batch.Singleton[_]] = CanEqual.derived
  given CanEqual[Batch.Wrapped[_], Batch.Combine[_]]   = CanEqual.derived
  given CanEqual[Batch.Wrapped[_], Batch.Wrapped[_]]   = CanEqual.derived

  def apply[A](value: A): Batch[A] =
    Singleton(value)

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
    Batch.Empty

  def combine[A](batch1: Batch[A], batch2: Batch[A]): Batch[A] =
    batch1 ++ batch2

  def combineAll[A](batches: Batch[A]*): Batch[A] =
    batches.foldLeft(Batch.empty[A])(_ ++ _)

  private[collections] case object Empty extends Batch[Nothing]:
    val isEmpty: Boolean          = true
    def toJSArray[B]: js.Array[B] = js.Array[B]()

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def head: Nothing               = throw new NoSuchElementException("Batch.Empty.head")
    def headOption: Option[Nothing] = None

    val size: Int = 0

    override def equals(that: Any): Boolean =
      that.isInstanceOf[Batch.Empty.type]

  private[collections] final case class Singleton[A](value: A) extends Batch[A]:
    val isEmpty: Boolean               = false
    def head: A                        = value
    def headOption: Option[A]          = Some(value)
    def toJSArray[B >: A]: js.Array[B] = js.Array[B](value)

    val size: Int = 1

    override def equals(that: Any): Boolean =
      given CanEqual[Empty.type, Any]   = CanEqual.derived
      given CanEqual[Singleton[_], Any] = CanEqual.derived
      given CanEqual[Combine[_], Any]   = CanEqual.derived
      given CanEqual[Wrapped[_], Any]   = CanEqual.derived
      given CanEqual[A, A]              = CanEqual.derived

      try
        that match
          case Empty =>
            false

          case Singleton(v) =>
            value == v.asInstanceOf[A]

          case c @ Combine(_, _) =>
            val cc = c.compact
            cc.size == 1 && cc.head.asInstanceOf[A] == value

          case Wrapped(arr) =>
            arr.size == 1 && arr.head.asInstanceOf[A] == value

      catch { _ => false }

  private[collections] final case class Combine[A](batch1: Batch[A], batch2: Batch[A]) extends Batch[A]:
    val isEmpty: Boolean      = batch1.isEmpty && batch2.isEmpty
    def head: A               = batch1.head
    def headOption: Option[A] = batch1.headOption

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
    def toJSArray[B >: A]: js.Array[B] =
      val arr = new js.Array[B](size)

      @tailrec
      def rec(remaining: List[Batch[A]], i: Int): Unit =
        remaining match
          case Nil =>
            ()

          case Batch.Empty :: xs =>
            rec(xs, i)

          case Batch.Singleton(v) :: xs =>
            arr(i) = v
            rec(xs, i + 1)

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
      given CanEqual[Empty.type, Any]   = CanEqual.derived
      given CanEqual[Singleton[_], Any] = CanEqual.derived
      given CanEqual[Combine[_], Any]   = CanEqual.derived
      given CanEqual[Wrapped[_], Any]   = CanEqual.derived
      given CanEqual[A, A]              = CanEqual.derived

      try
        that match
          case Empty =>
            isEmpty

          case Singleton(v) =>
            val cc = compact
            cc.size == 1 && cc.head == v.asInstanceOf[A]

          case c @ Combine(_, _) =>
            compact.values.sameElements(c.compact.values)

          case Wrapped(arr) =>
            compact.values.sameElements(arr)

      catch { _ => false }

  private[collections] final case class Wrapped[A](values: js.Array[A]) extends Batch[A]:
    val isEmpty: Boolean               = values.isEmpty
    def head: A                        = values.head
    def headOption: Option[A]          = values.headOption
    def toJSArray[B >: A]: js.Array[B] = values.asInstanceOf[js.Array[B]]

    lazy val size: Int = values.length

    override def equals(that: Any): Boolean =
      given CanEqual[Empty.type, Any]   = CanEqual.derived
      given CanEqual[Singleton[_], Any] = CanEqual.derived
      given CanEqual[Combine[_], Any]   = CanEqual.derived
      given CanEqual[Wrapped[_], Any]   = CanEqual.derived
      given CanEqual[A, A]              = CanEqual.derived

      try
        that match
          case Empty =>
            isEmpty

          case Singleton(v) =>
            values.size == 1 && values.head == v.asInstanceOf[A]

          case c @ Combine(_, _) =>
            values.sameElements(c.compact.values)

          case Wrapped(arr) =>
            values.sameElements(arr)

      catch { _ => false }
