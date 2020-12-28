package indigoextras.geometry

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2

final case class Vertex(x: Double, y: Double) {

  def withX(newX: Double): Vertex =
    this.copy(x = newX)

  def withY(newY: Double): Vertex =
    this.copy(y = newY)

  /**
    * Dot product. Here for convenience but really this is vector operation.
    */
  def dot(other: Vertex): Double =
    (x * other.x) + (y * other.y)

  def abs: Vertex =
    Vertex(Math.abs(x), Math.abs(y))

  def min(other: Vertex): Vertex =
    Vertex(Math.min(other.x, x), Math.min(other.y, y))
  def min(value: Double): Vertex =
    Vertex(Math.min(value, x), Math.min(value, y))

  def max(other: Vertex): Vertex =
    Vertex(Math.max(other.x, x), Math.max(other.y, y))
  def max(value: Double): Vertex =
    Vertex(Math.max(value, x), Math.max(value, y))

  def clamp(min: Double, max: Double): Vertex =
    Vertex(Math.min(max, Math.max(min, x)), Math.min(max, Math.max(min, y)))
  def clamp(min: Vertex, max: Vertex): Vertex =
    Vertex(Math.min(max.x, Math.max(min.x, x)), Math.min(max.y, Math.max(min.y, y)))

  def length: Double =
    distanceTo(Vertex.zero)

  def invert: Vertex =
    Vertex(-x, -y)

  def translate(vec: Vertex): Vertex =
    this + vec

  def moveTo(newPosition: Vertex): Vertex =
    newPosition
  def moveTo(x: Double, y: Double): Vertex =
    moveTo(Vertex(x, y))

  def moveBy(amount: Vertex): Vertex =
    this + amount
  def moveBy(x: Double, y: Double): Vertex =
    moveBy(Vertex(x, y))

  def scaleBy(vec: Vertex): Vertex =
    this * vec
  def scaleBy(amount: Double): Vertex =
    scaleBy(Vertex(amount))

  def round: Vertex =
    Vertex(Math.round(x).toDouble, Math.round(y).toDouble)

  def +(other: Vertex): Vertex = Vertex(x + other.x, y + other.y)
  def -(other: Vertex): Vertex = Vertex(x - other.x, y - other.y)
  def *(other: Vertex): Vertex = Vertex(x * other.x, y * other.y)
  def /(other: Vertex): Vertex = Vertex(x / other.x, y / other.y)

  def +(value: Double): Vertex = this + Vertex(value, value)
  def -(value: Double): Vertex = this - Vertex(value, value)
  def *(value: Double): Vertex = this * Vertex(value, value)
  def /(value: Double): Vertex = this / Vertex(value, value)

  def distanceTo(other: Vertex): Double =
    Math.sqrt(Math.abs(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2)))

  def toList: List[Double] =
    List(x, y)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def toVector2: Vector2 =
    Vector2(x, y)

  def makeVectorWith(other: Vertex): Vector2 =
    Vector2((other.x - x), (other.y - y))

  def ~==(other: Vertex): Boolean =
    Math.abs(x - other.x) < 0.0001 &&
    Math.abs(y - other.y) < 0.0001

}

object Vertex {

  def apply(d: Double): Vertex =
    Vertex(d, d)

  def fromPoint(point: Point): Vertex =
    Vertex(point.x.toDouble, point.y.toDouble)

  def fromVector(vector: Vector2): Vertex =
    Vertex(vector.x, vector.y)

  def tuple2ToVertex(t: (Double, Double)): Vertex =
    Vertex(t._1, t._2)

  val zero: Vertex = Vertex(0d, 0d)
  val one: Vertex  = Vertex(1d, 1d)

}
