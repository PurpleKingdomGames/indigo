package indigo.shared.datatypes

final case class Point(x: Int, y: Int) derives CanEqual {
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

  def abs: Point =
    Point(Math.abs(x), Math.abs(y))

  def min(other: Point): Point =
    Point(Math.min(other.x, x), Math.min(other.y, y))
  def min(value: Int): Point =
    Point(Math.min(value, x), Math.min(value, y))

  def max(other: Point): Point =
    Point(Math.max(other.x, x), Math.max(other.y, y))
  def max(value: Int): Point =
    Point(Math.max(value, x), Math.max(value, y))

  def clamp(min: Int, max: Int): Point =
    Point(Math.min(max, Math.max(min, x)), Math.min(max, Math.max(min, y)))

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

  def rotateBy(angle: Radians): Point = {
    val a = angle.wrap.toDouble
    val s = Math.sin(a)
    val c = Math.cos(a)

    Point(
      Math.round(this.x * c - this.y * s).toInt,
      Math.round(this.x * s + this.y * c).toInt
    )
  }
  def rotateBy(angle: Radians, origin: Point): Point =
    (this - origin).rotateBy(angle) + origin

  def rotateTo(angle: Radians): Point = {
    val a = angle.wrap.toDouble
    val r = this.distanceTo(Point.zero)
    Point(
      Math.round(r * Math.cos(a)).toInt,
      Math.round(r * Math.sin(a)).toInt
    )
  }

  def angle: Radians = Radians(Math.atan2(this.y, this.x))

  def distanceTo(other: Point): Double =
    Point.distanceBetween(this, other)

  def toVector: Vector2 =
    Vector2(x.toDouble, y.toDouble)

  def toSize: Size =
    Size(x, y)
}

object Point {

  given CanEqual[Option[Point], Option[Point]] = CanEqual.derived

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
