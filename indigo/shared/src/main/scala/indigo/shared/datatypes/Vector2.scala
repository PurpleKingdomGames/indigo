package indigo.shared.datatypes

import indigo.shared.EqualTo
import indigo.shared.EqualTo._

final class Vector2(val x: Double, val y: Double) {

  def withX(newX: Double): Vector2 =
    Vector2(newX, y)
  def withY(newY: Double): Vector2 =
    Vector2(x, newY)

  def invert: Vector2 =
    Vector2(-x, -y)

  def translate(vec: Vector2): Vector2 =
    Vector2.add(this, vec)

  def scale(vec: Vector2): Vector2 =
    Vector2.multiply(this, vec)

  def round: Vector2 =
    Vector2(Math.round(x).toDouble, Math.round(y).toDouble)

  def toList: List[Double] =
    List(x, y)

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

  def normalise: Vector2 =
    Vector2(
      if (x === 0) 0 else (x / Math.abs(x)),
      if (y === 0) 0 else (y / Math.abs(y))
    )

  def distanceTo(other: Vector2): Double =
    Vector2.distance(this, other)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def applyMatrix4(matrix4: Matrix4): Vector2 = Vector2.applyMatrix4(this, matrix4)

  override def toString: String =
    s"Vector2(x = ${x.toString()}, y = ${y.toString()})"

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf", "org.wartremover.warts.IsInstanceOf"))
  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[Vector2])
      this === obj.asInstanceOf[Vector2]
    else false

  def ===(other: Vector2): Boolean =
    implicitly[EqualTo[Vector2]].equal(this, other)

  def hash: String =
    x.toString() + y.toString()
}

object Vector2 {

  implicit val eq: EqualTo[Vector2] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.x, b.x) && ev.equal(a.y, b.y)
    }
  }

  def apply(x: Double, y: Double): Vector2 =
    new Vector2(x, y)

  def apply(i: Int): Vector2 =
    Vector2(i.toDouble, i.toDouble)

  val zero: Vector2     = Vector2(0d, 0d)
  val one: Vector2      = Vector2(1d, 1d)
  val minusOne: Vector2 = Vector2(-1d, -1d)

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

  def distance(v1: Vector2, v2: Vector2): Double =
    Math.sqrt(Math.abs(Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2)))

  def applyMatrix4(vector2: Vector2, matrix4: Matrix4): Vector2 = {
    val m  = matrix4.transpose
    val vl = vector2.toList

    Vector2(
      x = m.row1.zip(vl).map(p => p._1 * p._2).sum,
      y = m.row2.zip(vl).map(p => p._1 * p._2).sum
    )
  }

}
