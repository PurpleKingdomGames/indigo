package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point

@JSExportTopLevel("Rectangle")
final class RectangleDelegate(val x: Int, val y: Int, val width: Int, val height: Int) {
  def toInternal: Rectangle =
    Rectangle(Point(x, y), Point(width, height))
}
