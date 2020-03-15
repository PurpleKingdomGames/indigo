package indigo.shared.datatypes

import indigo.shared.{AsString, EqualTo}
import indigo.shared.EqualTo._

final class Radians(val value: Double) extends AnyVal {

  def +(other: Radians): Radians =
    Radians.add(this, other)

  def -(other: Radians): Radians =
    Radians.subtract(this, other)

  def *(other: Radians): Radians =
    Radians.multiply(this, other)

  def /(other: Radians): Radians =
    Radians.divide(this, other)

  def hash: String =
    value.toString()

  def asString: String =
    implicitly[AsString[Radians]].show(this)

  override def toString: String =
    asString

  def ===(other: Radians): Boolean =
    implicitly[EqualTo[Radians]].equal(this, other)

}
object Radians {

  implicit def show(implicit showD: AsString[Double]): AsString[Radians] =
    AsString.create(p => s"""Radians(${showD.show(p.value)})""")

  implicit def equalTo(implicit eqD: EqualTo[Double]): EqualTo[Radians] =
    EqualTo.create { (a, b) =>
      eqD.equal(a.value, b.value)
    }

  val PI: Radians = Radians(Math.PI)
  val TAU: Radians = Radians(Math.PI * 2)

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

  def fromDegrees(degrees: Double): Radians =
    Radians((TAU.value / 360d) * (degrees % 360d))

}
