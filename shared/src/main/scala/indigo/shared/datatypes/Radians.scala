package indigo.shared.datatypes

final class Radians(val value: Double) extends AnyVal {

  def +(other: Radians): Radians =
    Radians.add(this, other)

  def -(other: Radians): Radians =
    Radians.subtract(this, other)

  def *(other: Radians): Radians =
    Radians.multiply(this, other)

  def /(other: Radians): Radians =
    Radians.divide(this, other)

}
object Radians {

  def zero: Radians =
    Radians(0)

  def apply(value: Double): Radians =
    new Radians(value)

  def add(a: Radians, b: Radians): Radians =
    Radians(a.value + b.value)

  def subtract(a: Radians, b: Radians): Radians =
    Radians(a.value - b.value)

  def multiply(a: Radians, b: Radians): Radians =
    Radians(a.value * b.value)

  def divide(a: Radians, b: Radians): Radians =
    Radians(a.value / b.value)

}
