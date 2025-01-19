package indigo.shared.datatypes

import indigo.shared.collections.Batch
import indigo.shared.dice.Dice
import indigo.shared.geometry.Vertex

final case class Vector2(x: Double, y: Double) derives CanEqual:

  def withX(newX: Double): Vector2 =
    this.copy(x = newX)

  def withY(newY: Double): Vector2 =
    this.copy(y = newY)

  def abs: Vector2 =
    Vector2(Math.abs(x), Math.abs(y))

  def min(other: Vector2): Vector2 =
    Vector2(Math.min(other.x, x), Math.min(other.y, y))
  def min(value: Double): Vector2 =
    Vector2(Math.min(value, x), Math.min(value, y))

  def max(other: Vector2): Vector2 =
    Vector2(Math.max(other.x, x), Math.max(other.y, y))
  def max(value: Double): Vector2 =
    Vector2(Math.max(value, x), Math.max(value, y))

  def clamp(min: Double, max: Double): Vector2 =
    Vector2(Math.min(max, Math.max(min, x)), Math.min(max, Math.max(min, y)))
  def clamp(min: Vector2, max: Vector2): Vector2 =
    this.min(max).max(min)

  def length: Double =
    Math.sqrt(x * x + y * y)
  def magnitude: Double =
    length

  def invert: Vector2 =
    Vector2(-x, -y)

  def `unary_-`: Vector2 = invert

  def translate(vec: Vector2): Vector2 =
    Vector2.add(this, vec)

  def moveTo(newPosition: Vector2): Vector2 =
    newPosition
  def moveTo(x: Double, y: Double): Vector2 =
    moveTo(Vector2(x, y))

  def moveBy(amount: Vector2): Vector2 =
    Vector2.add(this, amount)
  def moveBy(x: Double, y: Double): Vector2 =
    moveBy(Vector2(x, y))

  def scaleBy(vec: Vector2): Vector2 =
    Vector2.multiply(this, vec)
  def scaleBy(amount: Double): Vector2 =
    scaleBy(Vector2(amount))

  def rotateBy(angle: Radians): Vector2 = {
    val a = angle.wrap.toDouble
    val s = Math.sin(a)
    val c = Math.cos(a)

    Vector2(
      this.x * c - this.y * s,
      this.x * s + this.y * c
    )
  }
  def rotateBy(angle: Radians, origin: Vector2): Vector2 =
    Vector2.add(
      Vector2.subtract(this, origin).rotateBy(angle),
      origin
    )

  def rotateTo(angle: Radians): Vector2 = {
    val a = angle.wrap.toDouble
    Vector2(this.length * Math.cos(a), this.length * Math.sin(a))
  }

  def angle: Radians = Radians(Math.atan2(this.y, this.x))

  def ceil: Vector2 =
    Vector2(Math.ceil(x), Math.ceil(y))

  def floor: Vector2 =
    Vector2(Math.floor(x), Math.floor(y))

  def round: Vector2 =
    Vector2(Math.round(x).toDouble, Math.round(y).toDouble)

  def toBatch: Batch[Double] =
    Batch(x, y)

  def +(other: Vector2): Vector2 = Vector2.add(this, other)
  def -(other: Vector2): Vector2 = Vector2.subtract(this, other)
  def *(other: Vector2): Vector2 = Vector2.multiply(this, other)
  def /(other: Vector2): Vector2 = Vector2.divide(this, other)
  def %(other: Vector2): Vector2 = Vector2.mod(this, other)

  def +(other: Vertex): Vector2 = Vector2.add(this, other.toVector2)
  def -(other: Vertex): Vector2 = Vector2.subtract(this, other.toVector2)
  def *(other: Vertex): Vector2 = Vector2.multiply(this, other.toVector2)
  def /(other: Vertex): Vector2 = Vector2.divide(this, other.toVector2)
  def %(other: Vertex): Vector2 = Vector2.mod(this, other.toVector2)

  def +(value: Double): Vector2 = Vector2.add(this, Vector2(value, value))
  def -(value: Double): Vector2 = Vector2.subtract(this, Vector2(value, value))
  def *(value: Double): Vector2 = Vector2.multiply(this, Vector2(value, value))
  def /(value: Double): Vector2 = Vector2.divide(this, Vector2(value, value))
  def %(value: Double): Vector2 = Vector2.mod(this, Vector2(value))

  def dot(other: Vector2): Double =
    Vector2.dotProduct(this, other)

  def normalise: Vector2 =
    if magnitude == 0 then Vector2.zero
    else
      Vector2(
        x / magnitude,
        y / magnitude
      )

  def distanceTo(other: Vector2): Double =
    Vector2.distance(this, other)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def toSize: Size =
    Size(x.toInt, y.toInt)

  def toVertex: Vertex =
    Vertex(x, y)

  def transform(matrix3: Matrix3): Vector2 =
    matrix3.transform(this)

  def transform(matrix4: Matrix4): Vector2 =
    matrix4.transform(toVector3).toVector2

  def toVector3: Vector3 =
    Vector3(x, y, 0)

  def toVector4: Vector4 =
    Vector4(x, y, 0, 0)

  def ~==(other: Vector2): Boolean =
    Math.abs(x - other.x) < 0.0001 &&
      Math.abs(y - other.y) < 0.0001

object Vector2:

  def apply(d: Double): Vector2 =
    Vector2(d, d)

  val zero: Vector2     = Vector2(0d, 0d)
  val one: Vector2      = Vector2(1d, 1d)
  val minusOne: Vector2 = Vector2(-1d, -1d)
  val max: Vector2      = Vector2(Double.MaxValue, Double.MaxValue)

  def fromPoints(start: Point, end: Point): Vector2 =
    Vector2((end.x - start.x).toDouble, (end.y - start.y).toDouble)

  def fromPoint(point: Point): Vector2 =
    Vector2(point.x.toDouble, point.y.toDouble)

  def fromSize(size: Size): Vector2 =
    Vector2(size.width.toDouble, size.height.toDouble)

  def fromVertex(vertex: Vertex): Vector2 =
    Vector2(vertex.x, vertex.y)

  inline def add(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x + vec2.x, vec1.y + vec2.y)

  inline def subtract(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x - vec2.x, vec1.y - vec2.y)

  inline def multiply(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x * vec2.x, vec1.y * vec2.y)

  inline def divide(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x / vec2.x, vec1.y / vec2.y)

  def dotProduct(vec1: Vector2, vec2: Vector2): Double =
    (vec1.x * vec2.x) + (vec1.y * vec2.y)

  def distance(v1: Vector2, v2: Vector2): Double =
    Math.sqrt(Math.abs(Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2)))

  def mod(dividend: Vector2, divisor: Vector2): Vector2 =
    Vector2(
      x = (dividend.x % divisor.x + divisor.x) % divisor.x,
      y = (dividend.y % divisor.y + divisor.y) % divisor.y
    )

  def random(dice: Dice): Vector2 =
    Vector2(dice.rollDouble, dice.rollDouble)
