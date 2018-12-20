package indigo.gameengine.scenegraph.datatypes
import indigo.runtime.Show

final case class Point(x: Int, y: Int) {
  def +(pt: Point): Point = Point(x + pt.x, y + pt.y)
  def +(i: Int): Point    = Point(x + i, y + i)
  def -(pt: Point): Point = Point(x - pt.x, y - pt.y)
  def -(i: Int): Point    = Point(x - i, y - i)
  def *(pt: Point): Point = Point(x * pt.x, y * pt.y)
  def *(i: Int): Point    = Point(x * i, y * i)
  def /(pt: Point): Point = Point(x / pt.x, y / pt.y)
  def /(i: Int): Point    = Point(x / i, y / i)

  def ===(other: Point): Boolean =
    Point.equality(this, other)

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

  implicit val show: Show[Point] =
    Show.create(p => s"""Point(${p.x}, ${p.y})""")

  def linearInterpolation(a: Point, b: Point, divisor: Double, multiplier: Double): Point =
    Point(a.x + (((b.x - a.x) / divisor) * multiplier).toInt, a.y + (((b.y - a.y) / divisor) * multiplier).toInt)

  def equality(a: Point, b: Point): Boolean =
    a.x == b.x && a.y == b.y

  def distanceBetween(a: Point, b: Point): Double = {
    val aa = b.x.toDouble - a.x.toDouble
    val bb = b.y.toDouble - a.y.toDouble
    val cc = (aa * aa) - (bb * bb)

    Math.sqrt(Math.abs(cc))
  }
}
