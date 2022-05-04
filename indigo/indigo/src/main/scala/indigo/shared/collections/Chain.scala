package indigo.shared.collections

import scala.reflect.ClassTag

import scalajs.js
import scalajs.js.JSConverters.*

sealed trait Chain[+A]:

  def isEmpty: Boolean
  def map[B](f: A => B): Chain[B]
  def foreach(f: A => Unit): Unit
  def head: A
  def headOption: Option[A]
  def apply(index: Int): A
  def toJSArray[B >: A]: js.Array[B]

  override def toString: String =
    "Chain(" + toJSArray.mkString(", ") + ")"

  def ++[B >: A](other: Chain[B]): Chain[B] =
    if this.isEmpty then other
    else if other.isEmpty then this
    else Chain.Combine(this, other)

  def |+|[B >: A](other: Chain[B]): Chain[B] =
    this ++ other

  def ::[B >: A](value: B): Chain[B] =
    Chain.Singleton(value) ++ this

  def +:[B >: A](value: B): Chain[B] =
    Chain.Singleton(value) ++ this

  def :+[B >: A](value: B): Chain[B] =
    this ++ Chain.Singleton(value)

  def compact[B >: A]: Chain.Wrapped[B] =
    Chain.Wrapped(toJSArray)

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  lazy val size: Int =
    var s = 0
    foreach(_ => s = s + 1)
    s

  def toArray[B >: A: ClassTag]: Array[B] =
    toJSArray.toArray

  def toList: List[A] =
    toJSArray.toList

object Chain:

  given CanEqual[Chain[_], Chain[_]]           = CanEqual.derived
  given CanEqual[Chain[_], Chain.Empty.type]   = CanEqual.derived
  given CanEqual[Chain[_], Chain.Singleton[_]] = CanEqual.derived
  given CanEqual[Chain[_], Chain.Combine[_]]   = CanEqual.derived
  given CanEqual[Chain[_], Chain.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Chain.Empty.type, Chain[_]]           = CanEqual.derived
  given CanEqual[Chain.Empty.type, Chain.Empty.type]   = CanEqual.derived
  given CanEqual[Chain.Empty.type, Chain.Singleton[_]] = CanEqual.derived
  given CanEqual[Chain.Empty.type, Chain.Combine[_]]   = CanEqual.derived
  given CanEqual[Chain.Empty.type, Chain.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Chain.Singleton[_], Chain[_]]           = CanEqual.derived
  given CanEqual[Chain.Singleton[_], Chain.Empty.type]   = CanEqual.derived
  given CanEqual[Chain.Singleton[_], Chain.Singleton[_]] = CanEqual.derived
  given CanEqual[Chain.Singleton[_], Chain.Combine[_]]   = CanEqual.derived
  given CanEqual[Chain.Singleton[_], Chain.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Chain.Combine[_], Chain[_]]           = CanEqual.derived
  given CanEqual[Chain.Combine[_], Chain.Empty.type]   = CanEqual.derived
  given CanEqual[Chain.Combine[_], Chain.Singleton[_]] = CanEqual.derived
  given CanEqual[Chain.Combine[_], Chain.Combine[_]]   = CanEqual.derived
  given CanEqual[Chain.Combine[_], Chain.Wrapped[_]]   = CanEqual.derived

  given CanEqual[Chain.Wrapped[_], Chain[_]]           = CanEqual.derived
  given CanEqual[Chain.Wrapped[_], Chain.Empty.type]   = CanEqual.derived
  given CanEqual[Chain.Wrapped[_], Chain.Singleton[_]] = CanEqual.derived
  given CanEqual[Chain.Wrapped[_], Chain.Combine[_]]   = CanEqual.derived
  given CanEqual[Chain.Wrapped[_], Chain.Wrapped[_]]   = CanEqual.derived

  def apply[A](value: A): Chain[A] =
    Singleton(value)

  def apply[A](values: js.Array[A]): Chain[A] =
    Wrapped(values)

  def apply[A](values: A*): Chain[A] =
    Wrapped(values.toJSArray)

  def apply[A](chain1: Chain[A], chain2: Chain[A]): Chain[A] =
    Combine(chain1, chain2)

  def empty[A]: Chain[A] =
    Chain.Empty

  def combineAll[A](chains: Chain[A]*): Chain[A] =
    chains.toList.foldLeft(Chain.empty[A])(_ ++ _)

  case object Empty extends Chain[Nothing]:
    val isEmpty: Boolean                  = true
    def map[B](f: Nothing => B): Chain[B] = this
    def foreach(f: Nothing => Unit): Unit = ()
    def toJSArray[B]: js.Array[B]         = js.Array[B]()

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): Nothing =
      throw new NoSuchElementException(s"Chain.Empty.apply($index)")

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def head: Nothing               = throw new NoSuchElementException("Chain.Empty.head")
    def headOption: Option[Nothing] = None

    override def equals(that: Any): Boolean =
      that.isInstanceOf[Chain.Empty.type]

  final case class Singleton[A](value: A) extends Chain[A]:
    val isEmpty: Boolean               = false
    def map[B](f: A => B): Chain[B]    = this.copy(value = f(value))
    def foreach(f: A => Unit): Unit    = f(value)
    def head: A                        = value
    def headOption: Option[A]          = Some(value)
    def toJSArray[B >: A]: js.Array[B] = js.Array[B](value)

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): A =
      if index == 0 then value else throw new IndexOutOfBoundsException

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

  final case class Combine[A](chain1: Chain[A], chain2: Chain[A]) extends Chain[A]:
    val isEmpty: Boolean            = chain1.isEmpty && chain2.isEmpty
    def map[B](f: A => B): Chain[B] = this.copy(chain1 = chain1.map(f), chain2 = chain2.map(f))
    def foreach(f: A => Unit): Unit =
      chain1.foreach(f)
      chain2.foreach(f)
    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): A =
      if index < 0 || index > size then throw new IndexOutOfBoundsException
      else if index < chain1.size then chain1(index)
      else chain2(index - chain1.size)


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

  final case class Wrapped[A](values: js.Array[A]) extends Chain[A]:
    val isEmpty: Boolean               = values.isEmpty
    def map[B](f: A => B): Chain[B]    = this.copy(values = values.map(f))
    def foreach(f: A => Unit): Unit    = values.foreach(f)
    def head: A                        = values.head
    def headOption: Option[A]          = values.headOption
    def toJSArray[B >: A]: js.Array[B] = values.asInstanceOf[js.Array[B]]

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    def apply(index: Int): A =
      if index < 0 || index > size then throw new IndexOutOfBoundsException
      else values(index)

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
