package indigo.shared.collections

import scala.annotation.tailrec
import scala.reflect.ClassTag

import scalajs.js
import scalajs.js.JSConverters.*

sealed trait Batch[+A]:

  private[collections] def isBranch: Boolean
  private[collections] def isLeaf: Boolean
  private[collections] def split: (Batch[A], Batch[A])

  def isEmpty: Boolean
  def map[B](f: A => B): Batch[B]
  def foreach(f: A => Unit): Unit
  def head: A
  def headOption: Option[A]
  def size: Int
  def apply(index: Int): A
  def toJSArray[B >: A]: js.Array[B]

  override def toString: String =
    "Batch(" + toJSArray.mkString(", ") + ")"

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

  def compact[B >: A]: Batch.Wrapped[B] =
    Batch.Wrapped(toJSArray)

  def toArray[B >: A: ClassTag]: Array[B] =
    toJSArray.toArray

  def toList: List[A] =
    toJSArray.toList

object Batch:

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

  def apply[A](batch1: Batch[A], batch2: Batch[A]): Batch[A] =
    Combine(batch1, batch2)

  def empty[A]: Batch[A] =
    Batch.Empty

  def combineAll[A](batchs: Batch[A]*): Batch[A] =
    batchs.toList.foldLeft(Batch.empty[A])(_ ++ _)

  /** Structural hasNext. Checks for the presence of a non-empty right-side of a combine.
    */
  def hasNextBatch[A](batch: Batch[A]): Boolean =
    if batch.isBranch then !batch.split._2.isEmpty
    else false

  /** Structural split of the batch, returning the next non-empty batch, and the remaining batch. Note that it is _not_
    * returning the values, so the next batch could be an array of values.
    */
  def splitBatch[A](batch: Batch[A]): (Batch[A], Batch[A]) =
    if batch.isBranch then
      val (a, b) = batch.split
      if a.isEmpty then splitBatch(b)
      else if a.isLeaf then (a, b)
      else
        val (aa, bb) = a.split
        if aa.isEmpty then splitBatch(bb ++ b)
        else (aa, bb ++ b)
    else (batch, Batch.empty)

  case object Empty extends Batch[Nothing]:
    private[collections] val isBranch: Boolean = false
    private[collections] val isLeaf: Boolean   = true
    private[collections] lazy val split: (Batch[Nothing], Batch[Nothing]) =
      (this, this)

    val isEmpty: Boolean                  = true
    def map[B](f: Nothing => B): Batch[B] = this
    def foreach(f: Nothing => Unit): Unit = ()
    def toJSArray[B]: js.Array[B]         = js.Array[B]()

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): Nothing =
      throw new NoSuchElementException(s"Batch.Empty.apply($index)")

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def head: Nothing               = throw new NoSuchElementException("Batch.Empty.head")
    def headOption: Option[Nothing] = None

    val size: Int = 0

    override def equals(that: Any): Boolean =
      that.isInstanceOf[Batch.Empty.type]

  final case class Singleton[A](value: A) extends Batch[A]:
    private[collections] val isBranch: Boolean = false
    private[collections] val isLeaf: Boolean   = true
    private[collections] lazy val split: (Batch[A], Batch[A]) =
      (this, this)

    val isEmpty: Boolean               = false
    def map[B](f: A => B): Batch[B]    = this.copy(value = f(value))
    def foreach(f: A => Unit): Unit    = f(value)
    def head: A                        = value
    def headOption: Option[A]          = Some(value)
    def toJSArray[B >: A]: js.Array[B] = js.Array[B](value)

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): A =
      if index == 0 then value else throw new IndexOutOfBoundsException

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

  final case class Combine[A](batch1: Batch[A], batch2: Batch[A]) extends Batch[A]:
    private[collections] val isBranch: Boolean = true
    private[collections] val isLeaf: Boolean   = false
    private[collections] lazy val split: (Batch[A], Batch[A]) =
      (batch1, batch2)

    val isEmpty: Boolean            = batch1.isEmpty && batch2.isEmpty
    def map[B](f: A => B): Batch[B] = this.copy(batch1 = batch1.map(f), batch2 = batch2.map(f))
    def foreach(f: A => Unit): Unit =
      batch1.foreach(f)
      batch2.foreach(f)
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

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): A =
      if index < 0 || index > size then throw new IndexOutOfBoundsException
      else if index < batch1.size then batch1(index)
      else batch2(index - batch1.size)

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

  final case class Wrapped[A](values: js.Array[A]) extends Batch[A]:
    private[collections] val isBranch: Boolean = false
    private[collections] val isLeaf: Boolean   = true
    private[collections] lazy val split: (Batch[A], Batch[A]) =
      (this, this)

    val isEmpty: Boolean               = values.isEmpty
    def map[B](f: A => B): Batch[B]    = this.copy(values = values.map(f))
    def foreach(f: A => Unit): Unit    = values.foreach(f)
    def head: A                        = values.head
    def headOption: Option[A]          = values.headOption
    def toJSArray[B >: A]: js.Array[B] = values.asInstanceOf[js.Array[B]]

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): A =
      if index < 0 || index > size then throw new IndexOutOfBoundsException
      else values(index)

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
