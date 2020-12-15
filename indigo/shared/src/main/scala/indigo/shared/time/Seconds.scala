package indigo.shared.time

final case class Seconds(value: Double) extends AnyVal {

  def +(other: Seconds): Seconds =
    Seconds.plus(this, other)

  def -(other: Seconds): Seconds =
    Seconds.minus(this, other)

  def *(other: Seconds): Seconds =
    Seconds.multiply(this, other)

  def /(other: Seconds): Seconds =
    Seconds.divide(this, other)

  def %(other: Seconds): Seconds =
    Seconds.modulo(this, other)

  def <(other: Seconds): Boolean =
    Seconds.lessThan(this, other)

  def >(other: Seconds): Boolean =
    Seconds.greaterThan(this, other)

  def <=(other: Seconds): Boolean =
    Seconds.lessThan(this, other) || this.value == other.value

  def >=(other: Seconds): Boolean =
    Seconds.greaterThan(this, other) || this.value == other.value

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
    
  def ===(other: Seconds): Boolean =
    this.value == other.value

}
object Seconds {

  val zero: Seconds =
    Seconds(0)

  @inline def plus(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value + b.value)

  @inline def minus(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value - b.value)

  @inline def multiply(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value * b.value)

  @inline def divide(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value / b.value)

  @inline def modulo(a: Seconds, b: Seconds): Seconds =
    Seconds(a.value % b.value)

  @inline def greaterThan(a: Seconds, b: Seconds): Boolean =
    a.value > b.value

  @inline def lessThan(a: Seconds, b: Seconds): Boolean =
    a.value < b.value

}
