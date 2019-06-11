package indigoexts.geometry

import indigo.shared.AsString
import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2

final class Vertex(val x: Double, val y: Double) {

  def translate(vec: Vertex): Vertex =
    Vertex.add(this, vec)

  def scale(vec: Vertex): Vertex =
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

  override def toString: String =
    asString

  def asString: String =
    implicitly[AsString[Vertex]].show(this)

  def ===(other: Vertex): Boolean =
    implicitly[EqualTo[Vertex]].equal(this, other)

  def ~==(other: Vertex): Boolean =
    Vertex.nearEnoughEqual(this, other, 0.001)
}

object Vertex {

  implicit val show: AsString[Vertex] = {
    val sD = implicitly[AsString[Double]]

    AsString.create { v =>
      s"Vertex(x = ${sD.show(v.x)}, y = ${sD.show(v.y)})"
    }
  }

  implicit val eq: EqualTo[Vertex] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.x, b.x) && ev.equal(a.y, b.y)
    }
  }

  def apply(x: Double, y: Double): Vertex =
    new Vertex(x, y)

  def apply(d: Double): Vertex =
    Vertex(d, d)

  def unapply(vertex: Vertex): Option[(Double, Double)] =
    Some((vertex.x, vertex.y))

  def fromPoint(point: Point): Vertex =
    Vertex(point.x.toDouble, point.y.toDouble)

  def tuple2ToVertex(t: (Double, Double)): Vertex =
    Vertex(t._1, t._2)

  val zero: Vertex = Vertex(0d, 0d)
  val one: Vertex  = Vertex(1d, 1d)

  def add(v1: Vertex, v2: Vertex): Vertex =
    Vertex(v1.x + v2.x, v1.y + v2.y)

  def subtract(v1: Vertex, v2: Vertex): Vertex =
    Vertex(v1.x - v2.x, v1.y - v2.y)

  def multiply(v1: Vertex, v2: Vertex): Vertex =
    Vertex(v1.x * v2.x, v1.y * v2.y)

  def divide(v1: Vertex, v2: Vertex): Vertex =
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

        Math.sqrt(Math.abs((aa * aa) * (bb * bb)))
    }

  def nearEnoughEqual(v1: Vertex, v2: Vertex, tolerance: Double): Boolean =
    v1.x >= v2.x - tolerance &&
      v1.x <= v2.x + tolerance &&
      v1.y >= v2.y - tolerance &&
      v1.y <= v2.y + tolerance

}
