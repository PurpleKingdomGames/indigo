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
}
case class Depth(zIndex: Int)

object Point {
  val zero: Point = Point(0, 0)
  implicit def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)
}

object Depth {
  implicit def intToDepth(i: Int): Depth = Depth(i)
}

object Rectangle {
  val zero: Rectangle = Rectangle(0, 0, 0, 0)
  def apply(x: Int, y: Int, width: Int, height: Int): Rectangle = Rectangle(Point(x, y), Point(width, height))
  implicit def tuple4ToRectangle(t: (Int, Int, Int, Int)): Rectangle = Rectangle(t._1, t._2, t._3, t._4)
}
