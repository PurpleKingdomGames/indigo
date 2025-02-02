package indigo.shared.datatypes

import indigo.Dice
import indigo.shared.geometry.Vertex

final case class Point(x: Int, y: Int) derives CanEqual:
  def +(pt: Point): Point = Point(x + pt.x, y + pt.y)
  def +(i: Int): Point    = Point(x + i, y + i)
  def +(d: Double): Point = Point((x.toDouble + d).toInt, (y.toDouble + d).toInt)
  def -(pt: Point): Point = Point(x - pt.x, y - pt.y)
  def -(i: Int): Point    = Point(x - i, y - i)
  def -(d: Double): Point = Point((x.toDouble - d).toInt, (y.toDouble - d).toInt)
  def *(pt: Point): Point = Point(x * pt.x, y * pt.y)
  def *(i: Int): Point    = Point(x * i, y * i)
  def *(d: Double): Point = Point((x.toDouble * d).toInt, (y.toDouble * d).toInt)
  def /(pt: Point): Point = Point(x / pt.x, y / pt.y)
  def /(i: Int): Point    = Point(x / i, y / i)
  def /(d: Double): Point = Point((x.toDouble / d).toInt, (y.toDouble / d).toInt)
  def %(pt: Point): Point = Point.mod(this, pt)
  def %(i: Int): Point    = Point.mod(this, Point(i))
  def %(d: Double): Point = Point.mod(this, Point(d.toInt))

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

  def rotateBy(angle: Radians): Point =
    val a = angle.wrap.toDouble
    val s = Math.sin(a)
    val c = Math.cos(a)

    Point(
      Math.round(this.x * c - this.y * s).toInt,
      Math.round(this.x * s + this.y * c).toInt
    )

  def rotateBy(angle: Radians, origin: Point): Point =
    (this - origin).rotateBy(angle) + origin

  def rotateTo(angle: Radians): Point =
    val a = angle.wrap.toDouble
    val r = this.distanceTo(Point.zero)
    Point(
      Math.round(r * Math.cos(a)).toInt,
      Math.round(r * Math.sin(a)).toInt
    )

  def angle: Radians = Radians(Math.atan2(this.y, this.x))

  def distanceTo(other: Point): Double =
    Point.distanceBetween(this, other)

  def toVector: Vector2 =
    Vector2(x.toDouble, y.toDouble)

  def toSize: Size =
    Size(x, y)

  def toVertex: Vertex =
    Vertex(x.toDouble, y.toDouble)

object Point:

  given CanEqual[Option[Point], Option[Point]] = CanEqual.derived

  def apply(xy: Int): Point =
    Point(xy, xy)

  val zero: Point = Point(0, 0)
  val one: Point  = Point(1, 1)

  def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)

  def linearInterpolation(a: Point, b: Point, divisor: Double, multiplier: Double): Point =
    Point(a.x + (((b.x - a.x) / divisor) * multiplier).toInt, a.y + (((b.y - a.y) / divisor) * multiplier).toInt)

  def distanceBetween(a: Point, b: Point): Double =
    (a, b) match
      case (Point(x1, y1), Point(x2, y2)) if x1 == x2 =>
        Math.abs((y2 - y1).toDouble)

      case (Point(x1, y1), Point(x2, y2)) if y1 == y2 =>
        Math.abs((x2 - x1).toDouble)

      case (Point(x1, y1), Point(x2, y2)) =>
        val aa = x2.toDouble - x1.toDouble
        val bb = y2.toDouble - y1.toDouble

        Math.sqrt(Math.abs((aa * aa) + (bb * bb)))

  def fromSize(size: Size): Point =
    Point(size.width, size.height)

  def fromVector2(vector2: Vector2): Point =
    Point(vector2.x.toInt, vector2.y.toInt)

  def fromVertex(vertex: Vertex): Point =
    Point(vertex.x.toInt, vertex.y.toInt)

  def mod(dividend: Point, divisor: Point): Point =
    Point(
      x = (dividend.x % divisor.x + divisor.x) % divisor.x,
      y = (dividend.y % divisor.y + divisor.y) % divisor.y
    )

  def random(dice: Dice, max: Int): Point =
    Point(
      if max <= 0 then 0 else dice.rollFromZero(max),
      if max <= 0 then 0 else dice.rollFromZero(max)
    )

  def random(dice: Dice, max: Point): Point =
    Point(
      if max.x <= 0 then 0 else dice.rollFromZero(max.x),
      if max.y <= 0 then 0 else dice.rollFromZero(max.y)
    )

  def random(dice: Dice, min: Int, max: Int): Point =
    Point(dice.rollFromZero(max - min) + min, dice.rollFromZero(max - min) + min)

  def random(dice: Dice, min: Point, max: Point): Point =
    Point(dice.rollFromZero(max.x - min.x) + min.x, dice.rollFromZero(max.y - min.y) + min.y)
