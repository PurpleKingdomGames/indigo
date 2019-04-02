package indigo.gameengine.scenegraph.animation

import indigo.Rectangle
import indigo.Point

final case class Frame(bounds: Rectangle, duration: Int)

object Frame {
  def apply(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), 1)
  def apply(x: Int, y: Int, width: Int, height: Int, duration: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), duration)
}