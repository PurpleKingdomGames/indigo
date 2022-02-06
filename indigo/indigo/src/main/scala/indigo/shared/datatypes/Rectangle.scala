package indigo.shared.datatypes

import scala.annotation.tailrec

final case class Rectangle(position: Point, size: Size) derives CanEqual:
  lazy val x: Int      = position.x
  lazy val y: Int      = position.y
  lazy val width: Int  = size.width
  lazy val height: Int = size.height

  lazy val left: Int   = if width >= 0 then x else x + width
  lazy val right: Int  = if width >= 0 then x + width else x
  lazy val top: Int    = if height >= 0 then y else y + height
  lazy val bottom: Int = if height >= 0 then y + height else y

  lazy val horizontalCenter: Int = x + (width / 2)
  lazy val verticalCenter: Int   = y + (height / 2)

  lazy val topLeft: Point     = Point(left, top)
  lazy val topRight: Point    = Point(right, top)
  lazy val bottomRight: Point = Point(right, bottom)
  lazy val bottomLeft: Point  = Point(left, bottom)
  lazy val center: Point      = Point(horizontalCenter, verticalCenter)
  lazy val halfSize: Size     = (size / 2).abs

  lazy val corners: List[Point] =
    List(topLeft, topRight, bottomRight, bottomLeft)

  def contains(pt: Point): Boolean =
    pt.x >= left && pt.x < right && pt.y >= top && pt.y < bottom
  def contains(x: Int, y: Int): Boolean =
    contains(Point(x, y))
  def isPointWithin(pt: Point): Boolean =
    contains(pt)
  def isPointWithin(x: Int, y: Int): Boolean =
    contains(Point(x, y))

  def +(rect: Rectangle): Rectangle = Rectangle(x + rect.x, y + rect.y, width + rect.width, height + rect.height)
  def +(i: Int): Rectangle          = Rectangle(x + i, y + i, width + i, height + i)
  def -(rect: Rectangle): Rectangle = Rectangle(x - rect.x, y - rect.y, width - rect.width, height - rect.height)
  def -(i: Int): Rectangle          = Rectangle(x - i, y - i, width - i, height - i)
  def *(rect: Rectangle): Rectangle = Rectangle(x * rect.x, y * rect.y, width * rect.width, height * rect.height)
  def *(i: Int): Rectangle          = Rectangle(x * i, y * i, width * i, height * i)
  def /(rect: Rectangle): Rectangle = Rectangle(x / rect.x, y / rect.y, width / rect.width, height / rect.height)
  def /(i: Int): Rectangle          = Rectangle(x / i, y / i, width / i, height / i)

  def expand(amount: Int): Rectangle =
    Rectangle.expand(this, amount)

  def expandToInclude(other: Rectangle): Rectangle =
    Rectangle.expandToInclude(this, other)

  def contract(amount: Int): Rectangle =
    Rectangle.contract(this, amount)

  def encompasses(other: Rectangle): Boolean =
    Rectangle.encompassing(this, other)

  def overlaps(other: Rectangle): Boolean =
    Rectangle.overlapping(this, other)

  def moveBy(point: Point): Rectangle =
    this.copy(position = position + point)
  def moveBy(x: Int, y: Int): Rectangle =
    moveBy(Point(x, y))

  def moveTo(point: Point): Rectangle =
    this.copy(position = point)
  def moveTo(x: Int, y: Int): Rectangle =
    moveTo(Point(x, y))

  def resize(newSize: Size): Rectangle =
    this.copy(size = newSize)
  def resize(x: Int, y: Int): Rectangle =
    resize(Size(x, y))

  def toSquare: Rectangle =
    this.copy(size = Size(Math.max(size.width, size.height)))

object Rectangle:

  given CanEqual[Option[Rectangle], Option[Rectangle]] = CanEqual.derived

  val zero: Rectangle = Rectangle(0, 0, 0, 0)
  val one: Rectangle  = Rectangle(0, 0, 1, 1)

  def apply(x: Int, y: Int, width: Int, height: Int): Rectangle =
    Rectangle(Point(x, y), Size(width, height))

  def apply(width: Int, height: Int): Rectangle =
    Rectangle(Point.zero, Size(width, height))

  def apply(size: Size): Rectangle =
    Rectangle(Point.zero, size)

  def fromPoints(pt1: Point, pt2: Point): Rectangle =
    val x = Math.min(pt1.x, pt2.x)
    val y = Math.min(pt1.y, pt2.y)
    val w = Math.max(pt1.x, pt2.x) - x
    val h = Math.max(pt1.y, pt2.y) - y

    Rectangle(x, y, w, h)

  def fromPoints(pt1: Point, pt2: Point, pt3: Point): Rectangle =
    val x = Math.min(Math.min(pt1.x, pt2.x), pt3.x)
    val y = Math.min(Math.min(pt1.y, pt2.y), pt3.y)
    val w = Math.max(Math.max(pt1.x, pt2.x), pt3.x) - x
    val h = Math.max(Math.max(pt1.y, pt2.y), pt3.y) - y

    Rectangle(x, y, w, h)

  def fromPoints(pt1: Point, pt2: Point, pt3: Point, pt4: Point): Rectangle =
    val x = Math.min(Math.min(Math.min(pt1.x, pt2.x), pt3.x), pt4.x)
    val y = Math.min(Math.min(Math.min(pt1.y, pt2.y), pt3.y), pt4.y)
    val w = Math.max(Math.max(Math.max(pt1.x, pt2.x), pt3.x), pt4.x) - x
    val h = Math.max(Math.max(Math.max(pt1.y, pt2.y), pt3.y), pt4.y) - y

    Rectangle(x, y, w, h)

  @deprecated("Use `fromPoints(pt1, pt2)` instead")
  def fromTwoPoints(pt1: Point, pt2: Point): Rectangle =
    fromPoints(pt1, pt2)

  def fromPointCloud(points: List[Point]): Rectangle =
    @tailrec
    def rec(remaining: List[Point], left: Int, top: Int, right: Int, bottom: Int): Rectangle =
      remaining match {
        case Nil =>
          Rectangle(left, top, right - left, bottom - top)

        case p :: ps =>
          rec(
            ps,
            Math.min(left, p.x),
            Math.min(top, p.y),
            Math.max(right, p.x),
            Math.max(bottom, p.y)
          )
      }

    rec(points, Int.MaxValue, Int.MaxValue, Int.MinValue, Int.MinValue)

  def expand(rectangle: Rectangle, amount: Int): Rectangle =
    Rectangle(
      x = if rectangle.width >= 0 then rectangle.x - amount else rectangle.x + amount,
      y = if rectangle.height >= 0 then rectangle.y - amount else rectangle.y + amount,
      width = if rectangle.width >= 0 then rectangle.width + (amount * 2) else rectangle.width - (amount * 2),
      height = if rectangle.height >= 0 then rectangle.height + (amount * 2) else rectangle.height - (amount * 2)
    )

  def expandToInclude(a: Rectangle, b: Rectangle): Rectangle =
    val newX: Int = if (a.left < b.left) a.left else b.left
    val newY: Int = if (a.top < b.top) a.top else b.top

    Rectangle(
      x = newX,
      y = newY,
      width = (if (a.right > b.right) a.right else b.right) - newX,
      height = (if (a.bottom > b.bottom) a.bottom else b.bottom) - newY
    )

  def contract(rectangle: Rectangle, amount: Int): Rectangle =
    Rectangle(
      x = if rectangle.width >= 0 then rectangle.x + amount else rectangle.x - amount,
      y = if rectangle.height >= 0 then rectangle.y + amount else rectangle.y - amount,
      width = if rectangle.width >= 0 then rectangle.width - (amount * 2) else rectangle.width + (amount * 2),
      height = if rectangle.height >= 0 then rectangle.height - (amount * 2) else rectangle.height + (amount * 2)
    )

  def encompassing(a: Rectangle, b: Rectangle): Boolean =
    b.x >= a.x && b.y >= a.y && (b.width + (b.x - a.x)) <= a.width && (b.height + (b.y - a.y)) <= a.height

  def overlapping(a: Rectangle, b: Rectangle): Boolean =
    Math.abs(a.center.x - b.center.x) < a.halfSize.width + b.halfSize.width &&
      Math.abs(a.center.y - b.center.y) < a.halfSize.height + b.halfSize.height
