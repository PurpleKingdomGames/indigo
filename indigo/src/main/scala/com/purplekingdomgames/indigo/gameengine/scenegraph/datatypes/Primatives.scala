package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

import scala.language.implicitConversions

// Data types
case class Point(x: Int, y: Int)
case class Rectangle(position: Point, size: Point) {
  val x: Int = position.x
  val y: Int = position.y
  val width: Int = size.x
  val height: Int = size.y
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
