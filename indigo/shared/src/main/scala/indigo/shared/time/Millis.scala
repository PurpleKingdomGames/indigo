package indigo.shared.time

import indigo.shared.EqualTo

final case class Millis(value: Long) extends AnyVal {

  def +(other: Millis): Millis =
    Millis.plus(this, other)

  def -(other: Millis): Millis =
    Millis.minus(this, other)

  def *(other: Millis): Millis =
    Millis.multiply(this, other)

  def /(other: Millis): Millis =
    Millis.divide(this, other)

  def %(other: Millis): Millis =
    Millis.modulo(this, other)

  def <(other: Millis): Boolean =
    Millis.lessThan(this, other)

  def >(other: Millis): Boolean =
    Millis.greaterThan(this, other)

  def <=(other: Millis): Boolean =
    Millis.lessThan(this, other) || implicitly[EqualTo[Long]].equal(this.value, other.value)

  def >=(other: Millis): Boolean =
    Millis.greaterThan(this, other) || implicitly[EqualTo[Long]].equal(this.value, other.value)

  def toInt: Int =
    value.toInt

  def toLong: Long =
    value

  def toFloat: Float =
    value.toFloat

  def toDouble: Double =
    value.toDouble

  def toSeconds: Seconds =
    Seconds(value.toDouble / 1000)

  def ===(other: Millis): Boolean =
    implicitly[EqualTo[Millis]].equal(this, other)

}
object Millis {

  val zero: Millis =
    Millis(0)

  implicit val equalToMillis: EqualTo[Millis] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[Long]].equal(a.value, b.value)
    }

  @inline def plus(a: Millis, b: Millis): Millis =
    Millis(a.value + b.value)

  @inline def minus(a: Millis, b: Millis): Millis =
    Millis(a.value - b.value)

  @inline def multiply(a: Millis, b: Millis): Millis =
    Millis(a.value * b.value)

  @inline def divide(a: Millis, b: Millis): Millis =
    Millis(a.value / b.value)

  @inline def modulo(a: Millis, b: Millis): Millis =
    Millis(a.value % b.value)

  @inline def greaterThan(a: Millis, b: Millis): Boolean =
    a.value > b.value

  @inline def lessThan(a: Millis, b: Millis): Boolean =
    a.value < b.value

}
