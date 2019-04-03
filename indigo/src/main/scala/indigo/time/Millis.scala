package indigo.time

import indigo.shared.{EqualTo, AsString}

final class Millis(val value: Long) extends AnyVal {

  def +(other: Millis): Millis =
    Millis.plus(this, other)

  def -(other: Millis): Millis =
    Millis.minus(this, other)

  def *(other: Millis): Millis =
    Millis.multiply(this, other)

  def /(other: Millis): Millis =
    Millis.divide(this, other)

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

}
object Millis {

  val zero: Millis =
    Millis(0)

  implicit val equalToMillis: EqualTo[Millis] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[Long]].equal(a.value, b.value)
    }

  implicit val asStringMillis: AsString[Millis] =
    AsString.create(d => s"Millis(${implicitly[AsString[Long]].show(d.value)})")

  def apply(value: Long): Millis =
    new Millis(value)

  def plus(a: Millis, b: Millis): Millis =
    Millis(a.value + b.value)

  def minus(a: Millis, b: Millis): Millis =
    Millis(a.value - b.value)

  def multiply(a: Millis, b: Millis): Millis =
    Millis(a.value * b.value)

  def divide(a: Millis, b: Millis): Millis =
    Millis(a.value / b.value)

  def greaterThan(a: Millis, b: Millis): Boolean =
    a.value > b.value

  def lessThan(a: Millis, b: Millis): Boolean =
    a.value < b.value

}
