package indigo.shared.datatypes

import indigo.shared.collections.Batch
import indigo.shared.dice.Dice

final case class Vector3(x: Double, y: Double, z: Double) derives CanEqual:

  def withX(newX: Double): Vector3 =
    this.copy(x = newX)

  def withY(newY: Double): Vector3 =
    this.copy(y = newY)

  def withZ(newZ: Double): Vector3 =
    this.copy(z = newZ)

  def abs: Vector3 =
    Vector3(Math.abs(x), Math.abs(y), Math.abs(z))

  def min(other: Vector3): Vector3 =
    Vector3(Math.min(other.x, x), Math.min(other.y, y), Math.min(other.z, z))
  def min(value: Double): Vector3 =
    Vector3(Math.min(value, x), Math.min(value, y), Math.min(value, z))

  def max(other: Vector3): Vector3 =
    Vector3(Math.max(other.x, x), Math.max(other.y, y), Math.max(other.z, z))
  def max(value: Double): Vector3 =
    Vector3(Math.max(value, x), Math.max(value, y), Math.max(value, z))

  def clamp(min: Double, max: Double): Vector3 =
    Vector3(Math.min(max, Math.max(min, x)), Math.min(max, Math.max(min, y)), Math.min(max, Math.max(min, z)))
  def clamp(min: Vector3, max: Vector3): Vector3 =
    this.min(max).max(min)

  def length: Double =
    Math.sqrt(x * x + y * y + z * z)
  def magnitude: Double =
    length

  def invert: Vector3 =
    Vector3(-x, -y, -z)

  def `unary_-`: Vector3 = invert

  def translate(vec: Vector3): Vector3 =
    Vector3.add(this, vec)

  def moveTo(newPosition: Vector3): Vector3 =
    newPosition
  def moveTo(x: Double, y: Double, z: Double): Vector3 =
    moveTo(Vector3(x, y, z))

  def moveBy(amount: Vector3): Vector3 =
    Vector3.add(this, amount)
  def moveBy(x: Double, y: Double, z: Double): Vector3 =
    moveBy(Vector3(x, y, z))

  def scaleBy(vec: Vector3): Vector3 =
    Vector3.multiply(this, vec)
  def scaleBy(amount: Double): Vector3 =
    scaleBy(Vector3(amount))

  def ceil: Vector3 =
    Vector3(Math.ceil(x), Math.ceil(y), Math.ceil(z))

  def floor: Vector3 =
    Vector3(Math.floor(x), Math.floor(y), Math.floor(z))

  def round: Vector3 =
    Vector3(Math.round(x).toDouble, Math.round(y).toDouble, Math.round(z).toDouble)

  def toBatch: Batch[Double] =
    Batch(x, y, z)

  def +(other: Vector3): Vector3 = Vector3.add(this, other)
  def -(other: Vector3): Vector3 = Vector3.subtract(this, other)
  def *(other: Vector3): Vector3 = Vector3.multiply(this, other)
  def /(other: Vector3): Vector3 = Vector3.divide(this, other)
  def %(other: Vector3): Vector3 = Vector3.mod(this, other)

  def +(value: Double): Vector3 = Vector3.add(this, Vector3(value, value, value))
  def -(value: Double): Vector3 = Vector3.subtract(this, Vector3(value, value, value))
  def *(value: Double): Vector3 = Vector3.multiply(this, Vector3(value, value, value))
  def /(value: Double): Vector3 = Vector3.divide(this, Vector3(value, value, value))
  def %(value: Double): Vector3 = Vector3.mod(this, Vector3(value))

  def dot(other: Vector3): Double =
    Vector3.dotProduct(this, other)

  def cross(other: Vector3): Vector3 =
    Vector3.crossProduct(this, other)

  def normalise: Vector3 =
    val magnitude = length
    if magnitude == 0 then Vector3.zero
    else
      Vector3(
        x / magnitude,
        y / magnitude,
        z / magnitude
      )

  def applyMatrix4(matrix4: Matrix4): Vector3 =
    matrix4.transform(this)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def toSize: Size =
    Size(x.toInt, y.toInt)

  def toVector2: Vector2 =
    Vector2(x, y)

  def toVector4: Vector4 =
    Vector4(x, y, z, 0)

  def distanceTo(other: Vector3): Double =
    Vector3.distance(this, other)

  def ~==(other: Vector3): Boolean =
    Math.abs(x - other.x) < 0.0001 &&
      Math.abs(y - other.y) < 0.0001 &&
      Math.abs(z - other.z) < 0.0001

object Vector3:

  def apply(d: Double): Vector3 =
    Vector3(d, d, d)

  val zero: Vector3 = Vector3(0d, 0d, 0d)
  val one: Vector3  = Vector3(1d, 1d, 1d)

  val unitX: Vector3 = Vector3(1d, 0d, 0d)
  val unitY: Vector3 = Vector3(0d, 1d, 0d)
  val unitZ: Vector3 = Vector3(0d, 0d, 1d)

  def fromPoint(point: Point): Vector3 =
    Vector3(point.x.toDouble, point.y.toDouble, 0)

  def fromSize(size: Size): Vector3 =
    Vector3(size.width.toDouble, size.height.toDouble, 0)

  def fromVector2(vector: Vector2): Vector3 =
    Vector3(vector.x, vector.y, 0)

  inline def add(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z)

  inline def subtract(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z)

  inline def multiply(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z)

  inline def divide(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(vec1.x / vec2.x, vec1.y / vec2.y, vec1.z / vec2.z)

  def dotProduct(vec1: Vector3, vec2: Vector3): Double =
    (vec1.x * vec2.x) + (vec1.y * vec2.y) + (vec1.z * vec2.z)

  def crossProduct(vec1: Vector3, vec2: Vector3): Vector3 =
    Vector3(
      vec1.y * vec2.z - vec1.z * vec2.y,
      vec1.z * vec2.x - vec1.x * vec2.z,
      vec1.x * vec2.y - vec1.y * vec2.x
    )

  def distance(v1: Vector3, v2: Vector3): Double =
    Math.sqrt(Math.abs(Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2) + Math.pow(v2.z - v1.z, 2)))

  def mod(dividend: Vector3, divisor: Vector3): Vector3 =
    Vector3(
      x = (dividend.x % divisor.x + divisor.x) % divisor.x,
      y = (dividend.y % divisor.y + divisor.y) % divisor.y,
      z = (dividend.z % divisor.z + divisor.z) % divisor.z
    )

  def random(dice: Dice): Vector3 =
    Vector3(dice.rollDouble, dice.rollDouble, dice.rollDouble)
