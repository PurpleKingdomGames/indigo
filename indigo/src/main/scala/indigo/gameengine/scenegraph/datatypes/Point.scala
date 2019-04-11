package indigo.gameengine.scenegraph.datatypes

import indigo.shared.{AsString, EqualTo}

final case class Point(x: Int, y: Int) {
  def +(pt: Point): Point = Point(x + pt.x, y + pt.y)
  def +(i: Int): Point    = Point(x + i, y + i)
  def -(pt: Point): Point = Point(x - pt.x, y - pt.y)
  def -(i: Int): Point    = Point(x - i, y - i)
  def *(pt: Point): Point = Point(x * pt.x, y * pt.y)
  def *(i: Int): Point    = Point(x * i, y * i)
  def /(pt: Point): Point = Point(x / pt.x, y / pt.y)
  def /(i: Int): Point    = Point(x / i, y / i)

  def withX(x: Int): Point = this.copy(x = x)
  def withY(y: Int): Point = this.copy(y = y)

  def invert: Point =
    Point(-x, -y)

  def distanceTo(other: Point): Double =
    Point.distanceBetween(this, other)
}

object Point {
  val zero: Point = Point(0, 0)

  def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)

  implicit def show(implicit showI: AsString[Int]): AsString[Point] =
    AsString.create(p => s"""Point(${showI.show(p.x)}, ${showI.show(p.y)})""")

  implicit def eq(implicit eqI: EqualTo[Int]): EqualTo[Point] =
    EqualTo.create { (a, b) =>
      eqI.equal(a.x, b.x) && eqI.equal(a.y, b.y)
    }

  def linearInterpolation(a: Point, b: Point, divisor: Double, multiplier: Double): Point =
    Point(a.x + (((b.x - a.x) / divisor) * multiplier).toInt, a.y + (((b.y - a.y) / divisor) * multiplier).toInt)

  def distanceBetween(a: Point, b: Point): Double = {
    val aa = b.x.toDouble - a.x.toDouble
    val bb = b.y.toDouble - a.y.toDouble
    val cc = (aa * aa) - (bb * bb)

    Math.sqrt(Math.abs(cc))
  }
}
