package indigo.shared.datatypes

import indigo.shared.{AsString, EqualTo}
import indigo.shared.EqualTo._

final class Point(val x: Int, val y: Int) {
  def +(pt: Point): Point = Point(x + pt.x, y + pt.y)
  def +(i: Int): Point    = Point(x + i, y + i)
  def -(pt: Point): Point = Point(x - pt.x, y - pt.y)
  def -(i: Int): Point    = Point(x - i, y - i)
  def *(pt: Point): Point = Point(x * pt.x, y * pt.y)
  def *(i: Int): Point    = Point(x * i, y * i)
  def /(pt: Point): Point = Point(x / pt.x, y / pt.y)
  def /(i: Int): Point    = Point(x / i, y / i)

  def withX(newX: Int): Point = Point(newX, y)
  def withY(newY: Int): Point = Point(x, newY)

  def invert: Point =
    Point(-x, -y)

  def distanceTo(other: Point): Double =
    Point.distanceBetween(this, other)

  def toVector: Vector2 =
    Vector2(x.toDouble, y.toDouble)

  def asString: String =
    implicitly[AsString[Point]].show(this)

  override def toString: String =
    asString

  def ===(other: Point): Boolean =
    implicitly[EqualTo[Point]].equal(this, other)
}

object Point {

  def apply(x: Int, y: Int): Point =
    new Point(x, y)

  def unapply(pt: Point): Option[(Int, Int)] =
    Option((pt.x, pt.y))

  val zero: Point = Point(0, 0)

  def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)

  implicit def show(implicit showI: AsString[Int]): AsString[Point] =
    AsString.create(p => s"""Point(${showI.show(p.x)}, ${showI.show(p.y)})""")

  implicit def equalTo(implicit eqI: EqualTo[Int]): EqualTo[Point] =
    EqualTo.create { (a, b) =>
      eqI.equal(a.x, b.x) && eqI.equal(a.y, b.y)
    }

  def linearInterpolation(a: Point, b: Point, divisor: Double, multiplier: Double): Point =
    Point(a.x + (((b.x - a.x) / divisor) * multiplier).toInt, a.y + (((b.y - a.y) / divisor) * multiplier).toInt)

  def distanceBetween(a: Point, b: Point): Double =
    (a, b) match {
      case (Point(x1, y1), Point(x2, y2)) if x1 === x2 =>
        Math.abs((y2 - y1).toDouble)

      case (Point(x1, y1), Point(x2, y2)) if y1 === y2 =>
        Math.abs((x2 - x1).toDouble)

      case (Point(x1, y1), Point(x2, y2)) =>
        val aa = x2.toDouble - x1.toDouble
        val bb = y2.toDouble - y1.toDouble

        Math.sqrt(Math.abs((aa * aa) * (bb * bb)))
    }
}
