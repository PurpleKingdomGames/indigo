package indigo.gameengine.scenegraph.animation

import indigo.Rectangle
import indigo.Point

final class Frame(val bounds: Rectangle, val duration: Int)

object Frame {

  def apply(bounds: Rectangle, duration: Int): Frame =
    new Frame(bounds, duration)

  def fromBounds(x: Int, y: Int, width: Int, height: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), 1)

  def fromBoundsWithDuration(x: Int, y: Int, width: Int, height: Int, duration: Int): Frame =
    Frame(Rectangle(Point(x, y), Point(width, height)), duration)
}