package indigo.shared.collections

import scala.annotation.tailrec
import scala.annotation.targetName
import scala.reflect.ClassTag

import scalajs.js
import scalajs.js.JSConverters.*

/** Batch is a really thin wrapper over `js.Array` to replace `List` on the Indigo APIs. Its purpose is to provide fast
  * scene construction and fast conversion back to js.Array for the engine to use. It also gives some basic List-like
  * features but not all of them since they aren't typically used in scene construction and are generally discouraged.
  */
sealed trait Batch[+A]:
  private lazy val _jsArray: js.Array[A] = toJSArray

  def isEmpty: Boolean
  def head: A
  def headOption: Option[A]
  def size: Int
  def toJSArray[B >: A]: js.Array[B]

  override def toString: String =
    "Batch(" + _jsArray.mkString(", ") + ")"

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

  def compact[B >: A]: Batch.Wrapped[B] =
    Batch.Wrapped(_jsArray.asInstanceOf[js.Array[B]])

  def toArray[B >: A: ClassTag]: Array[B] =
    _jsArray.asInstanceOf[js.Array[B]].toArray

  def toList: List[A] =
    _jsArray.toList

  def map[B](f: A => B): Batch[B] =
    Batch.Wrapped(_jsArray.map(f))

  def foreach(f: A => Unit): Unit =
    _jsArray.foreach(f)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def foreachWithIndex(f: (A, Int) => Unit): Unit =
    var idx: Int = 0
    foreach { v =>
      f(v, idx)
      idx = idx + 1
    }

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

  def fromJSArray[A](values: js.Array[A]): Batch[A] =
    Wrapped(values)

  def fromArray[A](values: Array[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def fromList[A](values: List[A]): Batch[A] =
    Wrapped(values.toJSArray)

  def empty[A]: Batch[A] =
    Batch.Empty

  def combineAll[A](batchs: Batch[A]*): Batch[A] =
    batchs.foldLeft(Batch.empty[A])(_ ++ _)

  case object Empty extends Batch[Nothing]:
    val isEmpty: Boolean          = true
    def toJSArray[B]: js.Array[B] = js.Array[B]()

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def head: Nothing               = throw new NoSuchElementException("Batch.Empty.head")
    def headOption: Option[Nothing] = None

    val size: Int = 0

    override def equals(that: Any): Boolean =
      that.isInstanceOf[Batch.Empty.type]

  final case class Singleton[A](value: A) extends Batch[A]:
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

  final case class Combine[A](batch1: Batch[A], batch2: Batch[A]) extends Batch[A]:
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

  final case class Wrapped[A](values: js.Array[A]) extends Batch[A]:
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