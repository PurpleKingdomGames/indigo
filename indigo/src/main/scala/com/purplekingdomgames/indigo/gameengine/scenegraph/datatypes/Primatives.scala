package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

// Data types
case class Point(x: Int, y: Int) {
  def +(pt: Point): Point = Point(x + pt.x, y + pt.y)
  def +(i: Int): Point = Point(x + i, y + i)
  def -(pt: Point): Point = Point(x - pt.x, y - pt.y)
  def -(i: Int): Point = Point(x - i, y - i)
  def *(pt: Point): Point = Point(x * pt.x, y * pt.y)
  def *(i: Int): Point = Point(x * i, y * i)
  def /(pt: Point): Point = Point(x / pt.x, y / pt.y)
  def /(i: Int): Point = Point(x / i, y / i)

  def withX(x: Int): Point = this.copy(x = x)
  def withY(y: Int): Point = this.copy(y = y)
}
case class Rectangle(position: Point, size: Point) {
  val x: Int = position.x
  val y: Int = position.y
  val width: Int = size.x
  val height: Int = size.y
  val hash: String = s"$x$y$width$height"

  val left: Int = x
  val right: Int = x + width
  val top: Int = y
  val bottom: Int = y + height

  def isPointWithin(pt: Point): Boolean =
    pt.x >= x && pt.x <= x + width && pt.y >= y && pt.y <= y + height
  def isPointWithin(x: Int, y: Int): Boolean = isPointWithin(Point(x, y))

  def +(rect: Rectangle): Rectangle = Rectangle(x + rect.x, y + rect.y, width + rect.width, height + rect.height)
  def +(i: Int): Rectangle = Rectangle(x + i, y + i, width + i, height + i)
  def -(rect: Rectangle): Rectangle = Rectangle(x - rect.x, y - rect.y, width - rect.width, height - rect.height)
  def -(i: Int): Rectangle = Rectangle(x - i, y - i, width - i, height - i)
  def *(rect: Rectangle): Rectangle = Rectangle(x * rect.x, y * rect.y, width * rect.width, height * rect.height)
  def *(i: Int): Rectangle = Rectangle(x * i, y * i, width * i, height * i)
  def /(rect: Rectangle): Rectangle = Rectangle(x / rect.x, y / rect.y, width / rect.width, height / rect.height)
  def /(i: Int): Rectangle = Rectangle(x / i, y / i, width / i, height / i)

  def enclosing(other: Rectangle): Rectangle =
    Rectangle.enclosing(this, other)

  def moveTo(point: Point): Rectangle =
    Rectangle(x + point.x, y + point.y, width, height)
}

case class Depth(zIndex: Int) extends AnyVal {
  def +(other: Depth): Depth =
    Depth.append(this, other)
}
object Depth {
  val Base: Depth = Depth(1)

  implicit def intToDepth(i: Int): Depth = Depth(i)

  def append(a: Depth, b: Depth): Depth =
    Depth(a.zIndex + b.zIndex)
}

object Point {
  val zero: Point = Point(0, 0)
  implicit def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)
}

object Rectangle {

  val zero: Rectangle = Rectangle(0, 0, 0, 0)

  def apply(x: Int, y: Int, width: Int, height: Int): Rectangle = Rectangle(Point(x, y), Point(width, height))

  implicit def tuple4ToRectangle(t: (Int, Int, Int, Int)): Rectangle = Rectangle(t._1, t._2, t._3, t._4)

  def enclosing(a: Rectangle, b: Rectangle): Rectangle = {
    val newX: Int = if (a.left < b.left) a.left else b.left
    val newY: Int = if (a.top < b.top) a.top else b.top

    Rectangle(
      x = newX,
      y = newY,
      width = (if (a.right > b.right) a.right else b.right) - newX,
      height = (if (a.bottom > b.bottom) a.bottom else b.bottom) - newY
    )
  }

}
