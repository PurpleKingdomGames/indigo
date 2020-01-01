package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Rectangle")
final class RectangleDelegate(_x: Int, _y: Int, _width: Int, _height: Int) {

  @JSExport
  val x = _x
  @JSExport
  val y = _y
  @JSExport
  val width = _width
  @JSExport
  val height = _height

  def toInternal: Rectangle =
    Rectangle(Point(x, y), Point(width, height))
}

object RectangleDelegate {

  def fromRectangle(r: Rectangle): RectangleDelegate =
    new RectangleDelegate(r.x, r.y, r.width, r.height)

}
