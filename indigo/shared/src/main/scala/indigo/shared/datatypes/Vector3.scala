package indigo.shared.datatypes

import indigo.shared.EqualTo
import indigo.shared.EqualTo._

final case class Vector3(x: Double, y: Double, z: Double) {

  def withX(newX: Double): Vector3 =
    this.copy(x = newX)

  def withY(newY: Double): Vector3 =
    this.copy(y = newY)

  def withZ(newZ: Double): Vector3 =
    this.copy(z = newZ)

  def translate(vec: Vector3): Vector3 =
    Vector3.add(this, vec)

  def scale(vec: Vector3): Vector3 =
    Vector3.multiply(this, vec)

  def round: Vector3 =
    Vector3(Math.round(x).toDouble, Math.round(y).toDouble, Math.round(z).toDouble)

  def toList: List[Double] =
    List(x, y, z)

  def +(other: Vector3): Vector3 = Vector3.add(this, other)
  def -(other: Vector3): Vector3 = Vector3.subtract(this, other)
  def *(other: Vector3): Vector3 = Vector3.multiply(this, other)
  def /(other: Vector3): Vector3 = Vector3.divide(this, other)

  def +(value: Double): Vector3 = Vector3.add(this, Vector3(value, value, value))
  def -(value: Double): Vector3 = Vector3.subtract(this, Vector3(value, value, value))
  def *(value: Double): Vector3 = Vector3.multiply(this, Vector3(value, value, value))
  def /(value: Double): Vector3 = Vector3.divide(this, Vector3(value, value, value))

  def dot(other: Vector3): Double =
    Vector3.dotProduct(this, other)

  def normalise: Vector3 =
    Vector3(
      if (x === 0) 0 else (x / Math.abs(x)),
      if (y === 0) 0 else (y / Math.abs(y)),
      if (z === 0) 0 else (z / Math.abs(z))
    )

  def applyMatrix4(matrix4: Matrix4): Vector3 = Vector3.applyMatrix4(this, matrix4)

  def toVector2: Vector2 =
    Vector2(x, y)

  def ===(other: Vector3): Boolean =
    implicitly[EqualTo[Vector3]].equal(this, other)
}

object Vector3 {

  implicit val eq: EqualTo[Vector3] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.x, b.x) && ev.equal(a.y, b.y) && ev.equal(a.z, b.z)
    }
  }

  def apply(i: Int): Vector3 =
    Vector3(i.toDouble, i.toDouble, i.toDouble)

  val zero: Vector3 = Vector3(0d, 0d, 0d)
  val one: Vector3  = Vector3(1d, 1d, 1d)

  @inline def add(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z)

  @inline def subtract(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z)

  @inline def multiply(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z)

  @inline def divide(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x / vec2.x, vec1.y / vec2.y, vec1.z / vec2.z)

  def dotProduct(vec1: Vector3, vec2: Vector3): Double =
    (vec1.x * vec2.x) + (vec1.y * vec2.y) + (vec1.z * vec2.z)

  def applyMatrix4(vector3: Vector3, matrix4: Matrix4): Vector3 = {
    val m  = matrix4.transpose
    val vl = vector3.toList

    Vector3(
      x = m.row1.zip(vl).map(p => p._1 * p._2).sum,
      y = m.row2.zip(vl).map(p => p._1 * p._2).sum,
      z = m.row3.zip(vl).map(p => p._1 * p._2).sum
    )
  }

}
