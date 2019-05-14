package indigo.shared.display

import indigo.shared.datatypes.Point
import indigo.shared.AsString
import indigo.shared.EqualTo

final class Vector2(val x: Double, val y: Double) {

  def translate(vec: Vector2): Vector2 =
    Vector2.add(this, vec)

  def scale(vec: Vector2): Vector2 =
    Vector2.multiply(this, vec)

  def round: Vector2 = Vector2(Math.round(x).toDouble, Math.round(y).toDouble)

  def +(other: Vector2): Vector2 = Vector2.add(this, other)
  def -(other: Vector2): Vector2 = Vector2.subtract(this, other)
  def *(other: Vector2): Vector2 = Vector2.multiply(this, other)
  def /(other: Vector2): Vector2 = Vector2.divide(this, other)

  def +(value: Double): Vector2 = Vector2.add(this, Vector2(value, value))
  def -(value: Double): Vector2 = Vector2.subtract(this, Vector2(value, value))
  def *(value: Double): Vector2 = Vector2.multiply(this, Vector2(value, value))
  def /(value: Double): Vector2 = Vector2.divide(this, Vector2(value, value))

  def dot(other: Vector2): Double =
    Vector2.dotProduct(this, other)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def asString: String =
    implicitly[AsString[Vector2]].show(this)

  def ===(other: Vector2): Boolean =
    implicitly[EqualTo[Vector2]].equal(this, other)
}

object Vector2 {

  implicit val show: AsString[Vector2] = {
    val sD = implicitly[AsString[Double]]

    AsString.create { v =>
      s"Vector2(x = ${sD.show(v.x)}, y = ${sD.show(v.y)})"
    }
  }

  implicit val eq: EqualTo[Vector2] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.x, b.x) && ev.equal(a.y, b.y)
    }
  }

  def apply(x: Double, y: Double): Vector2 =
    new Vector2(x, y)

  val zero: Vector2 = Vector2(0d, 0d)
  val one: Vector2  = Vector2(1d, 1d)

  def fromPoints(start: Point, end: Point): Vector2 =
    Vector2((end.x - start.x).toDouble, (end.y - start.y).toDouble)

  def add(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x + vec2.x, vec1.y + vec2.y)

  def subtract(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x - vec2.x, vec1.y - vec2.y)

  def multiply(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x * vec2.x, vec1.y * vec2.y)

  def divide(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x / vec2.x, vec1.y / vec2.y)

  def dotProduct(vec1: Vector2, vec2: Vector2): Double =
    (vec1.x * vec2.x) + (vec1.y * vec2.y)

  def apply(i: Int): Vector2 = Vector2(i.toDouble, i.toDouble)

}
