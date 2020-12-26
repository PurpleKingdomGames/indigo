package indigo.shared.datatypes

import scala.annotation.tailrec

final case class Rectangle(position: Point, size: Point) {
  lazy val x: Int       = position.x
  lazy val y: Int       = position.y
  lazy val width: Int   = size.x
  lazy val height: Int  = size.y
  lazy val hash: String = s"${x.toString()}${y.toString()}${width.toString()}${height.toString()}"

  lazy val left: Int   = x
  lazy val right: Int  = x + width
  lazy val top: Int    = y
  lazy val bottom: Int = y + height

  lazy val horizontalCenter: Int = x + (width / 2)
  lazy val verticalCenter: Int   = y + (height / 2)

  lazy val topLeft: Point     = Point(left, top)
  lazy val topRight: Point    = Point(right, top)
  lazy val bottomRight: Point = Point(right, bottom)
  lazy val bottomLeft: Point  = Point(left, bottom)
  lazy val center: Point      = Point(horizontalCenter, verticalCenter)
  lazy val halfSize: Point    = size / 2

  lazy val corners: List[Point] =
    List(topLeft, topRight, bottomRight, bottomLeft)

  def isPointWithin(pt: Point): Boolean =
    pt.x >= left && pt.x < right && pt.y >= top && pt.y < bottom

  def isPointWithin(x: Int, y: Int): Boolean = isPointWithin(Point(x, y))

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

  def encompasses(other: Rectangle): Boolean =
    Rectangle.encompassing(this, other)

  def overlaps(other: Rectangle): Boolean =
    Rectangle.overlapping(this, other)

  def moveBy(point: Point): Rectangle =
    this.copy(position = position + point)

  def moveTo(point: Point): Rectangle =
    this.copy(position = point)

  def resize(newSize: Point): Rectangle =
    this.copy(size = newSize)

  def ===(other: Rectangle): Boolean =
    position === other.position && size === other.size

}

object Rectangle {

  val zero: Rectangle = Rectangle(0, 0, 0, 0)

  def apply(x: Int, y: Int, width: Int, height: Int): Rectangle =
    Rectangle(Point(x, y), Point(width, height))

  def fromTwoPoints(pt1: Point, pt2: Point): Rectangle = {
    val x = Math.min(pt1.x, pt2.x)
    val y = Math.min(pt1.y, pt2.y)
    val w = Math.max(pt1.x, pt2.x) - x
    val h = Math.max(pt1.y, pt2.y) - y

    Rectangle(x, y, w, h)
  }

  def fromPointCloud(points: List[Point]): Rectangle = {
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
  }

  def expand(rectangle: Rectangle, amount: Int): Rectangle =
    Rectangle(
      x = rectangle.x - amount,
      y = rectangle.y - amount,
      width = rectangle.width + (amount * 2),
      height = rectangle.height + (amount * 2)
    )

  def expandToInclude(a: Rectangle, b: Rectangle): Rectangle = {
    val newX: Int = if (a.left < b.left) a.left else b.left
    val newY: Int = if (a.top < b.top) a.top else b.top

    Rectangle(
      x = newX,
      y = newY,
      width = (if (a.right > b.right) a.right else b.right) - newX,
      height = (if (a.bottom > b.bottom) a.bottom else b.bottom) - newY
    )
  }

  def encompassing(a: Rectangle, b: Rectangle): Boolean =
    b.x >= a.x && b.y >= a.y && (b.width + (b.x - a.x)) <= a.width && (b.height + (b.y - a.y)) <= a.height

  def overlapping(a: Rectangle, b: Rectangle): Boolean =
    Math.abs(a.center.x - b.center.x) < a.halfSize.x + b.halfSize.x && Math.abs(a.center.y - b.center.y) < a.halfSize.y + b.halfSize.y

}
