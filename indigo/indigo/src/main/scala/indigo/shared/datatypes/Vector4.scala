package indigo.shared.datatypes

import indigo.shared.collections.Batch
import indigo.shared.dice.Dice

final case class Vector4(x: Double, y: Double, z: Double, w: Double) derives CanEqual:

  def withX(newX: Double): Vector4 =
    this.copy(x = newX)

  def withY(newY: Double): Vector4 =
    this.copy(y = newY)

  def withZ(newZ: Double): Vector4 =
    this.copy(z = newZ)

  def withW(newW: Double): Vector4 =
    this.copy(w = newW)

  def abs: Vector4 =
    Vector4(Math.abs(x), Math.abs(y), Math.abs(z), Math.abs(w))

  def min(other: Vector4): Vector4 =
    Vector4(Math.min(other.x, x), Math.min(other.y, y), Math.min(other.z, z), Math.min(other.w, w))
  def min(value: Double): Vector4 =
    Vector4(Math.min(value, x), Math.min(value, y), Math.min(value, z), Math.min(value, w))

  def max(other: Vector4): Vector4 =
    Vector4(Math.max(other.x, x), Math.max(other.y, y), Math.max(other.z, z), Math.max(other.w, w))
  def max(value: Double): Vector4 =
    Vector4(Math.max(value, x), Math.max(value, y), Math.max(value, z), Math.max(value, w))

  def clamp(min: Double, max: Double): Vector4 =
    Vector4(
      Math.min(max, Math.max(min, x)),
      Math.min(max, Math.max(min, y)),
      Math.min(max, Math.max(min, z)),
      Math.min(max, Math.max(min, w))
    )
  def clamp(min: Vector4, max: Vector4): Vector4 =
    this.min(max).max(min)

  def length: Double =
    Math.sqrt(x * x + y * y + z * z + w * w)
  def magnitude: Double =
    length

  def invert: Vector4 =
    Vector4(-x, -y, -z, -w)

  def `unary_-`: Vector4 = invert

  def translate(vec: Vector4): Vector4 =
    Vector4.add(this, vec)

  def moveTo(newPosition: Vector4): Vector4 =
    newPosition
  def moveTo(x: Double, y: Double, z: Double, w: Double): Vector4 =
    moveTo(Vector4(x, y, z, w))

  def moveBy(amount: Vector4): Vector4 =
    Vector4.add(this, amount)
  def moveBy(x: Double, y: Double, z: Double, w: Double): Vector4 =
    moveBy(Vector4(x, y, z, w))

  def scaleBy(vec: Vector4): Vector4 =
    Vector4.multiply(this, vec)
  def scaleBy(amount: Double): Vector4 =
    scaleBy(Vector4(amount))

  def ceil: Vector4 =
    Vector4(Math.ceil(x), Math.ceil(y), Math.ceil(z), Math.ceil(w))

  def floor: Vector4 =
    Vector4(Math.floor(x), Math.floor(y), Math.floor(z), Math.floor(w))

  def round: Vector4 =
    Vector4(Math.round(x).toDouble, Math.round(y).toDouble, Math.round(z).toDouble, Math.round(w).toDouble)

  def toBatch: Batch[Double] =
    Batch(x, y, z, w)

  def +(other: Vector4): Vector4 = Vector4.add(this, other)
  def -(other: Vector4): Vector4 = Vector4.subtract(this, other)
  def *(other: Vector4): Vector4 = Vector4.multiply(this, other)
  def /(other: Vector4): Vector4 = Vector4.divide(this, other)
  def %(other: Vector4): Vector4 = Vector4.mod(this, other)

  def +(value: Double): Vector4 = Vector4.add(this, Vector4(value, value, value, value))
  def -(value: Double): Vector4 = Vector4.subtract(this, Vector4(value, value, value, value))
  def *(value: Double): Vector4 = Vector4.multiply(this, Vector4(value, value, value, value))
  def /(value: Double): Vector4 = Vector4.divide(this, Vector4(value, value, value, value))
  def %(value: Double): Vector4 = Vector4.mod(this, Vector4(value))

  def dot(other: Vector4): Double =
    Vector4.dotProduct(this, other)

  def normalise: Vector4 =
    val magnitude = length
    if magnitude == 0 then Vector4.zero
    else
      Vector4(
        x / magnitude,
        y / magnitude,
        z / magnitude,
        w / magnitude
      )

  def transform(matrix4: Matrix4): Vector4 =
    matrix4.transform(toVector3).toVector4

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def toSize: Size =
    Size(x.toInt, y.toInt)

  def toVector2: Vector2 =
    Vector2(x, y)

  def toVector3: Vector3 =
    Vector3(x, y, z)

  def distanceTo(other: Vector4): Double =
    Vector4.distance(this, other)

  override def toString: String =
    s"Vector4(x = ${x.toString()}, y = ${y.toString()}, z = ${z.toString()}, w = ${z.toString()})"

  def ~==(other: Vector4): Boolean =
    Math.abs(x - other.x) < 0.0001 &&
      Math.abs(y - other.y) < 0.0001 &&
      Math.abs(z - other.z) < 0.0001 &&
      Math.abs(w - other.w) < 0.0001

object Vector4:

  def apply(d: Double): Vector4 =
    Vector4(d, d, d, d)

  val zero: Vector4 = Vector4(0d, 0d, 0d, 0d)
  val one: Vector4  = Vector4(1d, 1d, 1d, 1d)

  def fromPoint(point: Point): Vector4 =
    Vector4(point.x.toDouble, point.y.toDouble, 0, 0)

  def fromSize(size: Size): Vector4 =
    Vector4(size.width.toDouble, size.height.toDouble, 0, 0)

  def fromVector2(vector: Vector2): Vector4 =
    Vector4(vector.x, vector.y, 0, 0)

  def fromVector3(vector: Vector3): Vector4 =
    Vector4(vector.x, vector.y, vector.z, 0)

  inline def add(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z, vec1.w + vec2.w)

  inline def subtract(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z, vec1.w - vec2.w)

  inline def multiply(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z, vec1.w * vec2.w)

  inline def divide(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x / vec2.x, vec1.y / vec2.y, vec1.z / vec2.z, vec1.w / vec2.w)

  def position(x: Double, y: Double, z: Double): Vector4 =
    Vector4(x, y, z, 0)

  def direction(x: Double, y: Double, z: Double): Vector4 =
    Vector4(x, y, z, 0)

  def dotProduct(vec1: Vector4, vec2: Vector4): Double =
    (vec1.x * vec2.x) + (vec1.y * vec2.y) + (vec1.z * vec2.z) + (vec1.w * vec2.w)

  def distance(v1: Vector4, v2: Vector4): Double =
    Math.sqrt(
      Math.abs(
        Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2) + Math.pow(v2.z - v1.z, 2) + Math.pow(v2.w - v1.w, 2)
      )
    )

  def mod(dividend: Vector4, divisor: Vector4): Vector4 =
    Vector4(
      x = (dividend.x % divisor.x + divisor.x) % divisor.x,
      y = (dividend.y % divisor.y + divisor.y) % divisor.y,
      z = (dividend.z % divisor.z + divisor.z) % divisor.z,
      w = (dividend.w % divisor.w + divisor.w) % divisor.w
    )

  def random(dice: Dice): Vector4 =
    Vector4(dice.rollDouble, dice.rollDouble, dice.rollDouble, dice.rollDouble)
