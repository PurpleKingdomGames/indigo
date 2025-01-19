package indigo.shared.geometry

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.dice.Dice

/** A `Vertex` is another co-ordinate-like type that specifically represents a point on a graph.
  */
final case class Vertex(x: Double, y: Double) derives CanEqual:

  def withX(newX: Double): Vertex =
    this.copy(x = newX)

  def withY(newY: Double): Vertex =
    this.copy(y = newY)

  /** Dot product. Here for convenience but really this is vector operation.
    */
  def dot(other: Vertex): Double =
    (x * other.x) + (y * other.y)
  def dot(other: Vector2): Double =
    dot(Vertex.fromVector2(other))

  def abs: Vertex =
    Vertex(Math.abs(x), Math.abs(y))

  def min(other: Vertex): Vertex =
    Vertex(Math.min(other.x, x), Math.min(other.y, y))
  def min(value: Double): Vertex =
    Vertex(Math.min(value, x), Math.min(value, y))
  def min(v: Vector2): Vertex =
    min(Vertex.fromVector2(v))

  def max(other: Vertex): Vertex =
    Vertex(Math.max(other.x, x), Math.max(other.y, y))
  def max(value: Double): Vertex =
    Vertex(Math.max(value, x), Math.max(value, y))
  def max(v: Vector2): Vertex =
    max(Vertex.fromVector2(v))

  def clamp(min: Double, max: Double): Vertex =
    Vertex(Math.min(max, Math.max(min, x)), Math.min(max, Math.max(min, y)))
  def clamp(min: Vertex, max: Vertex): Vertex =
    Vertex(Math.min(max.x, Math.max(min.x, x)), Math.min(max.y, Math.max(min.y, y)))
  def clamp(v1: Vector2, v2: Vector2): Vertex =
    clamp(Vertex.fromVector2(v1), Vertex.fromVector2(v1))

  def length: Double =
    Math.sqrt(x * x + y * y)

  def invert: Vertex =
    Vertex(-x, -y)

  def translate(vec: Vertex): Vertex =
    this + vec
  def translate(vec: Vector2): Vertex =
    this + Vertex.fromVector2(vec)

  def moveTo(newPosition: Vertex): Vertex =
    newPosition
  def moveTo(x: Double, y: Double): Vertex =
    moveTo(Vertex(x, y))
  def moveTo(v: Vector2): Vertex =
    moveTo(Vertex.fromVector2(v))

  def moveBy(amount: Vertex): Vertex =
    this + amount
  def moveBy(x: Double, y: Double): Vertex =
    moveBy(Vertex(x, y))
  def moveBy(v: Vector2): Vertex =
    moveBy(Vertex.fromVector2(v))

  def scaleBy(vec: Vertex): Vertex =
    this * vec
  def scaleBy(amount: Double): Vertex =
    scaleBy(Vertex(amount))
  def scaleBy(v: Vector2): Vertex =
    scaleBy(Vertex.fromVector2(v))

  def rotateBy(angle: Radians): Vertex =
    val a = angle.wrap.toDouble
    val s = Math.sin(a)
    val c = Math.cos(a)

    Vertex(
      this.x * c - this.y * s,
      this.x * s + this.y * c
    )

  def rotateBy(angle: Radians, origin: Vertex): Vertex =
    (this - origin).rotateBy(angle) + origin

  def rotateTo(angle: Radians): Vertex =
    val a = angle.wrap.toDouble
    Vertex(this.length * Math.cos(a), this.length * Math.sin(a))

  def angle: Radians = Radians(Math.atan2(this.y, this.x))

  def ceil: Vertex =
    Vertex(Math.ceil(x), Math.ceil(y))

  def floor: Vertex =
    Vertex(Math.floor(x), Math.floor(y))

  def round: Vertex =
    Vertex(Math.round(x).toDouble, Math.round(y).toDouble)

  def +(other: Vertex): Vertex = Vertex(x + other.x, y + other.y)
  def -(other: Vertex): Vertex = Vertex(x - other.x, y - other.y)
  def *(other: Vertex): Vertex = Vertex(x * other.x, y * other.y)
  def /(other: Vertex): Vertex = Vertex(x / other.x, y / other.y)
  def %(other: Vertex): Vertex = Vertex.mod(this, other)

  def +(other: Vector2): Vertex = Vertex(x + other.x, y + other.y)
  def -(other: Vector2): Vertex = Vertex(x - other.x, y - other.y)
  def *(other: Vector2): Vertex = Vertex(x * other.x, y * other.y)
  def /(other: Vector2): Vertex = Vertex(x / other.x, y / other.y)
  def %(other: Vector2): Vertex = Vertex.mod(this, other.toVertex)

  def +(value: Double): Vertex = this + Vertex(value, value)
  def -(value: Double): Vertex = this - Vertex(value, value)
  def *(value: Double): Vertex = this * Vertex(value, value)
  def /(value: Double): Vertex = this / Vertex(value, value)
  def %(value: Double): Vertex = Vertex.mod(this, Vertex(value))

  def distanceTo(other: Vertex): Double =
    Math.sqrt(Math.abs(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2)))

  def toList: List[Double] =
    List(x, y)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def toSize: Size =
    Size(x.toInt, y.toInt)

  def toVector2: Vector2 =
    Vector2(x, y)

  def makeVectorWith(other: Vertex): Vector2 =
    Vector2(other.x - x, other.y - y)

  def ~==(other: Vertex): Boolean =
    Math.abs(x - other.x) < 0.0001 && Math.abs(y - other.y) < 0.0001

object Vertex:

  def apply(d: Double): Vertex =
    Vertex(d, d)

  def fromPoint(point: Point): Vertex =
    Vertex(point.x.toDouble, point.y.toDouble)

  def fromSize(size: Size): Vertex =
    Vertex(size.width.toDouble, size.height.toDouble)

  def fromVector2(vector: Vector2): Vertex =
    Vertex(vector.x, vector.y)

  def tuple2ToVertex(t: (Double, Double)): Vertex =
    Vertex(t._1, t._2)

  val zero: Vertex = Vertex(0d, 0d)
  val one: Vertex  = Vertex(1d, 1d)

  def mod(dividend: Vertex, divisor: Vertex): Vertex =
    Vertex(
      x = (dividend.x % divisor.x + divisor.x) % divisor.x,
      y = (dividend.y % divisor.y + divisor.y) % divisor.y
    )

  def random(dice: Dice): Vertex =
    Vertex(dice.rollDouble, dice.rollDouble)
