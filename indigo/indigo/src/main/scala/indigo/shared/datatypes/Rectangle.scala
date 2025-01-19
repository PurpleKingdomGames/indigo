package indigo.shared.datatypes

import indigo.shared.collections.Batch
import indigo.shared.dice.Dice
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.BoundingCircle

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

  lazy val corners: Batch[Point] =
    Batch(topLeft, topRight, bottomRight, bottomLeft)

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
  def +(d: Double): Rectangle =
    Rectangle((x.toDouble + d).toInt, (y.toDouble + d).toInt, (width.toDouble + d).toInt, (height.toDouble + d).toInt)

  def -(rect: Rectangle): Rectangle = Rectangle(x - rect.x, y - rect.y, width - rect.width, height - rect.height)
  def -(i: Int): Rectangle          = Rectangle(x - i, y - i, width - i, height - i)
  def -(d: Double): Rectangle =
    Rectangle((x.toDouble - d).toInt, (y.toDouble - d).toInt, (width.toDouble - d).toInt, (height.toDouble - d).toInt)

  def *(rect: Rectangle): Rectangle = Rectangle(x * rect.x, y * rect.y, width * rect.width, height * rect.height)
  def *(i: Int): Rectangle          = Rectangle(x * i, y * i, width * i, height * i)
  def *(d: Double): Rectangle =
    Rectangle((x.toDouble * d).toInt, (y.toDouble * d).toInt, (width.toDouble * d).toInt, (height.toDouble * d).toInt)

  def /(rect: Rectangle): Rectangle = Rectangle(x / rect.x, y / rect.y, width / rect.width, height / rect.height)
  def /(i: Int): Rectangle          = Rectangle(x / i, y / i, width / i, height / i)
  def /(d: Double): Rectangle =
    Rectangle((x.toDouble / d).toInt, (y.toDouble / d).toInt, (width.toDouble / d).toInt, (height.toDouble / d).toInt)

  def expand(amount: Int): Rectangle =
    Rectangle.expand(this, amount)
  def expand(amount: Size): Rectangle =
    Rectangle.expand(this, amount)

  def expandToInclude(other: Rectangle): Rectangle =
    Rectangle.expandToInclude(this, other)

  def contract(amount: Int): Rectangle =
    Rectangle.contract(this, amount)
  def contract(amount: Size): Rectangle =
    Rectangle.contract(this, amount)

  def encompasses(other: Rectangle): Boolean =
    Rectangle.encompassing(this, other)
  def encompasses(other: Circle): Boolean =
    Rectangle.encompassing(this, other)

  def overlaps(other: Rectangle): Boolean =
    Rectangle.overlapping(this, other)
  def overlaps(other: Circle): Boolean =
    Rectangle.overlapping(this, other)

  def moveBy(point: Point): Rectangle =
    this.copy(position = position + point)
  def moveBy(x: Int, y: Int): Rectangle =
    moveBy(Point(x, y))

  def moveTo(point: Point): Rectangle =
    this.copy(position = point)
  def moveTo(x: Int, y: Int): Rectangle =
    moveTo(Point(x, y))

  /** Resize the rectangle to the given size or the current size, whichever is smaller.
    */
  def min(value: Size): Rectangle =
    this.copy(size = size.min(value))

  /** Resize the rectangle to the given size or the current size, whichever is smaller.
    */
  def min(width: Int, height: Int): Rectangle =
    min(Size(width, height))

  /** Ensure the rectangle is at least, the given size.
    */
  def minSize(min: Size): Rectangle =
    max(min)

  /** Ensure the rectangle is at least, the given size.
    */
  def minSize(width: Int, height: Int): Rectangle =
    minSize(Size(width, height))

  /** Resize the rectangle to the given size or the current size, whichever is larger.
    */
  def max(value: Size): Rectangle =
    this.copy(size = size.max(value))

  /** Resize the rectangle to the given size or the current size, whichever is larger.
    */
  def max(width: Int, height: Int): Rectangle =
    max(Size(width, height))

  /** Ensure the rectangle is at most, the given size.
    */
  def maxSize(max: Size): Rectangle =
    min(max)

  /** Ensure the rectangle is at most, the given size.
    */
  def maxSize(width: Int, height: Int): Rectangle =
    maxSize(Size(width, height))

  def resize(newSize: Size): Rectangle =
    this.copy(size = newSize)
  def resize(x: Int, y: Int): Rectangle =
    resize(Size(x, y))
  def resize(value: Int): Rectangle =
    resize(Size(value))

  def resizeBy(amount: Size): Rectangle =
    this.copy(size = size + amount)
  def resizeBy(x: Int, y: Int): Rectangle =
    resizeBy(Size(x, y))
  def resizeBy(amount: Int): Rectangle =
    resizeBy(Size(amount))

  def withPosition(point: Point): Rectangle =
    moveTo(point)
  def withPosition(x: Int, y: Int): Rectangle =
    moveTo(Point(x, y))

  def withSize(newSize: Size): Rectangle =
    resize(newSize)
  def withSize(x: Int, y: Int): Rectangle =
    resize(Size(x, y))

  def toSquare: Rectangle =
    this.copy(size = Size(Math.max(size.width, size.height)))

  @deprecated("Please use `toIncircle`, or alternatively `toCircumcircle`.")
  def toCircle: Circle =
    Circle.incircle(this)
  def toIncircle: Circle =
    Circle.incircle(this)
  def toCircumcircle: Circle =
    Circle.circumcircle(this)

  def toBoundingBox: BoundingBox =
    BoundingBox.fromRectangle(this)

  @deprecated("Please use `toBoundingIncircle`, or alternatively `toBoundingCircumcircle`.")
  def toBoundingCircle: BoundingCircle =
    BoundingCircle.incircle(this.toBoundingBox)
  def toBoundingIncircle: BoundingCircle =
    BoundingCircle.incircle(this.toBoundingBox)
  def toBoundingCircumcircle: BoundingCircle =
    BoundingCircle.circumcircle(this.toBoundingBox)

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

  def fromPointCloud(points: Batch[Point]): Rectangle =
    @tailrec
    def rec(remaining: Batch[Point], left: Int, top: Int, right: Int, bottom: Int): Rectangle =
      if remaining.isEmpty then Rectangle(left, top, right - left, bottom - top)
      else
        val p = remaining.head
        rec(
          remaining.tail,
          Math.min(left, p.x),
          Math.min(top, p.y),
          Math.max(right, p.x),
          Math.max(bottom, p.y)
        )

    rec(points, Int.MaxValue, Int.MaxValue, Int.MinValue, Int.MinValue)

  def fromIncircle(circle: Circle): Rectangle =
    Rectangle(Point(circle.left, circle.top), Size(circle.diameter))

  def fromCircumcircle(circle: Circle): Rectangle =
    val sideLength = (circle.diameter * Math.sqrt(2)) / 2
    Rectangle(circle.center - (sideLength / 2).toInt, Size(sideLength.toInt))

  def expand(rectangle: Rectangle, amount: Int): Rectangle =
    Rectangle(
      x = if rectangle.width >= 0 then rectangle.x - amount else rectangle.x + amount,
      y = if rectangle.height >= 0 then rectangle.y - amount else rectangle.y + amount,
      width = if rectangle.width >= 0 then rectangle.width + (amount * 2) else rectangle.width - (amount * 2),
      height = if rectangle.height >= 0 then rectangle.height + (amount * 2) else rectangle.height - (amount * 2)
    )
  def expand(rectangle: Rectangle, amount: Size): Rectangle =
    Rectangle(
      x = if rectangle.width >= 0 then rectangle.x - amount.width else rectangle.x + amount.width,
      y = if rectangle.height >= 0 then rectangle.y - amount.height else rectangle.y + amount.height,
      width =
        if rectangle.width >= 0 then rectangle.width + (amount.width * 2) else rectangle.width - (amount.width * 2),
      height =
        if rectangle.height >= 0 then rectangle.height + (amount.height * 2) else rectangle.height - (amount.height * 2)
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
  def contract(rectangle: Rectangle, amount: Size): Rectangle =
    Rectangle(
      x = if rectangle.width >= 0 then rectangle.x + amount.width else rectangle.x - amount.width,
      y = if rectangle.height >= 0 then rectangle.y + amount.height else rectangle.y - amount.height,
      width =
        if rectangle.width >= 0 then rectangle.width - (amount.width * 2) else rectangle.width + (amount.width * 2),
      height =
        if rectangle.height >= 0 then rectangle.height - (amount.height * 2) else rectangle.height + (amount.height * 2)
    )

  def encompassing(a: Rectangle, b: Rectangle): Boolean =
    b.x >= a.x && b.y >= a.y && (b.width + (b.x - a.x)) <= a.width && (b.height + (b.y - a.y)) <= a.height
  def encompassing(a: Rectangle, b: Circle): Boolean =
    encompassing(a, b.toIncircleRectangle)

  def overlapping(a: Rectangle, b: Rectangle): Boolean =
    a.toBoundingBox.overlaps(b.toBoundingBox)
  def overlapping(a: Rectangle, b: Circle): Boolean =
    a.toBoundingBox.overlaps(b.toBoundingCircle)

  def random(dice: Dice, max: Int): Rectangle =
    Rectangle(Point.random(dice, max), Size.random(dice, max))

  def random(dice: Dice, max: Rectangle): Rectangle =
    Rectangle(Point.random(dice, max.position), Size.random(dice, max.size))

  def random(dice: Dice, min: Int, max: Int): Rectangle =
    Rectangle(Point.random(dice, min, max), Size.random(dice, min, max))

  def random(dice: Dice, min: Rectangle, max: Rectangle): Rectangle =
    Rectangle(Point.random(dice, min.position, max.position), Size.random(dice, min.size, max.size))
