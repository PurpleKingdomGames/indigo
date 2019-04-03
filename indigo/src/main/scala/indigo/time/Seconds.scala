package indigo.time

import indigo.shared.{EqualTo, AsString}

final class Seconds(val value: Double) extends AnyVal {

  def +(other: Seconds): Seconds =
    Seconds.plus(this, other)

  def -(other: Seconds): Seconds =
    Seconds.minus(this, other)

  def *(other: Seconds): Seconds =
    Seconds.multiply(this, other)

  def /(other: Seconds): Seconds =
    Seconds.divide(this, other)

  def <(other: Seconds): Boolean =
    Seconds.lessThan(this, other)

  def >(other: Seconds): Boolean =
    Seconds.greaterThan(this, other)

  def <=(other: Seconds): Boolean =
    Seconds.lessThan(this, other) || implicitly[EqualTo[Double]].equal(this.value, other.value)

  def >=(other: Seconds): Boolean =
    Seconds.greaterThan(this, other) || implicitly[EqualTo[Double]].equal(this.value, other.value)

  def toInt: Int =
    value.toInt

  def toLong: Long =
    value.toLong

  def toFloat: Float =
    value.toFloat

  def toDouble: Double =
    value

  def toMillis: Millis =
    Millis(Math.floor(value * 1000).toLong)

}
object Seconds {

  val zero: Seconds =
    Seconds(0)

  implicit val equalToSeconds: EqualTo[Seconds] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[Double]].equal(a.value, b.value)
    }

  implicit val asStringSeconds: AsString[Seconds] =
    AsString.create(d => s"Seconds(${implicitly[AsString[Double]].show(d.value)})")

  def apply(value: Double): Seconds =
    new Seconds(value)

  def plus(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value + b.value)

  def minus(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value - b.value)

  def multiply(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value * b.value)

  def divide(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value / b.value)

  def greaterThan(a: Seconds, b: Seconds): Boolean =
    a.value > b.value

  def lessThan(a: Seconds, b: Seconds): Boolean =
    a.value < b.value

}
