package indigo.shared.datatypes

final case class Point(x: Int, y: Int) {
  def +(pt: Point): Point = Point(x + pt.x, y + pt.y)
  def +(i: Int): Point    = Point(x + i, y + i)
  def -(pt: Point): Point = Point(x - pt.x, y - pt.y)
  def -(i: Int): Point    = Point(x - i, y - i)
  def *(pt: Point): Point = Point(x * pt.x, y * pt.y)
  def *(i: Int): Point    = Point(x * i, y * i)
  def /(pt: Point): Point = Point(x / pt.x, y / pt.y)
  def /(i: Int): Point    = Point(x / i, y / i)

  def withX(newX: Int): Point = this.copy(x = newX)
  def withY(newY: Int): Point = this.copy(y = newY)

  def invert: Point =
    Point(-x, -y)

  def moveTo(newPosition: Point): Point =
    newPosition
  def moveTo(x: Int, y: Int): Point =
    moveTo(Point(x, y))

  def moveBy(amount: Point): Point =
    this + amount
  def moveBy(x: Int, y: Int): Point =
    moveBy(Point(x, y))

  def distanceTo(other: Point): Double =
    Point.distanceBetween(this, other)

  def toVector: Vector2 =
    Vector2(x.toDouble, y.toDouble)

  def ===(other: Point): Boolean =
    x == other.x && y == other.y

  val hash: String = s"${x.toString()}${y.toString()}"
}

object Point {

  def apply(xy: Int): Point =
    Point(xy, xy)

  val zero: Point = Point(0, 0)

  def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)

  def linearInterpolation(a: Point, b: Point, divisor: Double, multiplier: Double): Point =
    Point(a.x + (((b.x - a.x) / divisor) * multiplier).toInt, a.y + (((b.y - a.y) / divisor) * multiplier).toInt)

  def distanceBetween(a: Point, b: Point): Double =
    (a, b) match {
      case (Point(x1, y1), Point(x2, y2)) if x1 == x2 =>
        Math.abs((y2 - y1).toDouble)

      case (Point(x1, y1), Point(x2, y2)) if y1 == y2 =>
        Math.abs((x2 - x1).toDouble)

      case (Point(x1, y1), Point(x2, y2)) =>
        val aa = x2.toDouble - x1.toDouble
        val bb = y2.toDouble - y1.toDouble

        Math.sqrt(Math.abs((aa * aa) + (bb * bb)))
    }
}
