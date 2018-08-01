package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes
import com.purplekingdomgames.indigo.runtime.Show

case class Point(x: Int, y: Int) {
  def +(pt: Point): Point = Point(x + pt.x, y + pt.y)
  def +(i: Int): Point    = Point(x + i, y + i)
  def -(pt: Point): Point = Point(x - pt.x, y - pt.y)
  def -(i: Int): Point    = Point(x - i, y - i)
  def *(pt: Point): Point = Point(x * pt.x, y * pt.y)
  def *(i: Int): Point    = Point(x * i, y * i)
  def /(pt: Point): Point = Point(x / pt.x, y / pt.y)
  def /(i: Int): Point    = Point(x / i, y / i)

  def withX(x: Int): Point = this.copy(x = x)
  def withY(y: Int): Point = this.copy(y = y)

  def invert: Point =
    Point(-x, -y)
}

object Point {
  val zero: Point = Point(0, 0)

  implicit def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)

  implicit val show: Show[Point] =
    Show.create(p => s"""Point(${p.x}, ${p.y})""")

  def linearInterpolation(a: Point, b: Point, divisor: Double, multiplier: Double): Point =
    Point(a.x + (((b.x - a.x) / divisor) * multiplier).toInt, a.y + (((b.y - a.y) / divisor) * multiplier).toInt)
}
