package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes
import com.purplekingdomgames.indigo.runtime.Show

case class Rectangle(position: Point, size: Point) {
  val x: Int       = position.x
  val y: Int       = position.y
  val width: Int   = size.x
  val height: Int  = size.y
  val hash: String = s"$x$y$width$height"

  val left: Int   = x
  val right: Int  = x + width
  val top: Int    = y
  val bottom: Int = y + height

  def topLeft: Point     = Point(left, top)
  def topRight: Point    = Point(right, top)
  def bottomRight: Point = Point(right, bottom)
  def bottomLeft: Point  = Point(left, bottom)

  def corners: List[Point] =
    List(topLeft, topRight, bottomRight, bottomLeft)

  def isPointWithin(pt: Point): Boolean =
    pt.x >= x && pt.x <= x + width && pt.y >= y && pt.y <= y + height
  def isPointWithin(x: Int, y: Int): Boolean = isPointWithin(Point(x, y))

  def +(rect: Rectangle): Rectangle = Rectangle(x + rect.x, y + rect.y, width + rect.width, height + rect.height)
  def +(i: Int): Rectangle          = Rectangle(x + i, y + i, width + i, height + i)
  def -(rect: Rectangle): Rectangle = Rectangle(x - rect.x, y - rect.y, width - rect.width, height - rect.height)
  def -(i: Int): Rectangle          = Rectangle(x - i, y - i, width - i, height - i)
  def *(rect: Rectangle): Rectangle = Rectangle(x * rect.x, y * rect.y, width * rect.width, height * rect.height)
  def *(i: Int): Rectangle          = Rectangle(x * i, y * i, width * i, height * i)
  def /(rect: Rectangle): Rectangle = Rectangle(x / rect.x, y / rect.y, width / rect.width, height / rect.height)
  def /(i: Int): Rectangle          = Rectangle(x / i, y / i, width / i, height / i)

  def expandToInclude(other: Rectangle): Rectangle =
    Rectangle.expandToInclude(this, other)

  def intersects(other: Rectangle): Boolean =
    Rectangle.intersecting(this, other)

  def moveTo(point: Point): Rectangle =
    Rectangle(x + point.x, y + point.y, width, height)
}

object Rectangle {

  val zero: Rectangle = Rectangle(0, 0, 0, 0)

  // TODO: Should we validate that Rectangles are always top left to bottom right?
  def apply(x: Int, y: Int, width: Int, height: Int): Rectangle = Rectangle(Point(x, y), Point(width, height))

  implicit def tuple4ToRectangle(t: (Int, Int, Int, Int)): Rectangle = Rectangle(t._1, t._2, t._3, t._4)

  implicit val show: Show[Rectangle] =
    Show.create(p => s"""Rectangle(${p.x}, ${p.y}, ${p.width}, ${p.height})""")

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

  def intersecting(a: Rectangle, b: Rectangle): Boolean =
    b.corners.exists(p => a.isPointWithin(p))

}
