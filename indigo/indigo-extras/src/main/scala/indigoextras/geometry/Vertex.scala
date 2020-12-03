package indigoextras.geometry

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2

final case class Vertex(x: Double, y: Double) {

  def withX(newX: Double): Vertex =
    this.copy(x = newX)

  def withY(newY: Double): Vertex =
    this.copy(y = newY)

  def invert: Vertex =
    Vertex(-x, -y)

  def translate(vec: Vertex): Vertex =
    Vertex.add(this, vec)

  def moveTo(newPosition: Vertex): Vertex =
    newPosition
  def moveTo(x: Double, y: Double): Vertex =
    moveTo(Vertex(x, y))

  def moveBy(amount: Vertex): Vertex =
    Vertex.add(this, amount)
  def moveBy(x: Double, y: Double): Vertex =
    moveBy(Vertex(x, y))

  def scaleBy(vec: Vertex): Vertex =
    Vertex.multiply(this, vec)

  def round: Vertex =
    Vertex(Math.round(x).toDouble, Math.round(y).toDouble)

  def +(other: Vertex): Vertex = Vertex.add(this, other)
  def -(other: Vertex): Vertex = Vertex.subtract(this, other)
  def *(other: Vertex): Vertex = Vertex.multiply(this, other)
  def /(other: Vertex): Vertex = Vertex.divide(this, other)

  def +(value: Double): Vertex = Vertex.add(this, Vertex(value, value))
  def -(value: Double): Vertex = Vertex.subtract(this, Vertex(value, value))
  def *(value: Double): Vertex = Vertex.multiply(this, Vertex(value, value))
  def /(value: Double): Vertex = Vertex.divide(this, Vertex(value, value))

  def distanceTo(other: Vertex): Double =
    Vertex.distanceBetween(this, other)

  def toList: List[Double] =
    List(x, y)

  def toPoint: Point =
    Point(x.toInt, y.toInt)

  def toVector2: Vector2 =
    Vector2(x, y)

  def ===(other: Vertex): Boolean =
    implicitly[EqualTo[Vertex]].equal(this, other)

  def ~==(other: Vertex): Boolean =
    Vertex.equalEnough(this, other, 0.001)

  @SuppressWarnings(Array("org.wartremover.warts.IsInstanceOf", "org.wartremover.warts.AsInstanceOf"))
  override def equals(obj: Any): Boolean =
    if (obj.isInstanceOf[Vertex])
      this === obj.asInstanceOf[Vertex]
    else false
}

object Vertex {

  implicit val eq: EqualTo[Vertex] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.x, b.x) && ev.equal(a.y, b.y)
    }
  }

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

  @inline def add(v1: Vertex, v2: Vertex): Vertex =
    Vertex(v1.x + v2.x, v1.y + v2.y)

  @inline def subtract(v1: Vertex, v2: Vertex): Vertex =
    Vertex(v1.x - v2.x, v1.y - v2.y)

  @inline def multiply(v1: Vertex, v2: Vertex): Vertex =
    Vertex(v1.x * v2.x, v1.y * v2.y)

  @inline def divide(v1: Vertex, v2: Vertex): Vertex =
    Vertex(v1.x / v2.x, v1.y / v2.y)

  def twoVerticesToVector2(start: Vertex, end: Vertex): Vector2 =
    Vector2((end.x - start.x).toDouble, (end.y - start.y).toDouble)

  def distanceBetween(a: Vertex, b: Vertex): Double =
    (a, b) match {
      case (Vertex(x1, y1), Vertex(x2, y2)) if x1 === x2 =>
        Math.abs(y2 - y1)

      case (Vertex(x1, y1), Vertex(x2, y2)) if y1 === y2 =>
        Math.abs(x2 - x1)

      case (Vertex(x1, y1), Vertex(x2, y2)) =>
        val aa = x2.toDouble - x1.toDouble
        val bb = y2.toDouble - y1.toDouble

        Math.sqrt(Math.abs((aa * aa) + (bb * bb)))
    }

  def equalEnough(v1: Vertex, v2: Vertex, tolerance: Double): Boolean =
    v1.x >= v2.x - tolerance &&
      v1.x <= v2.x + tolerance &&
      v1.y >= v2.y - tolerance &&
      v1.y <= v2.y + tolerance

}
