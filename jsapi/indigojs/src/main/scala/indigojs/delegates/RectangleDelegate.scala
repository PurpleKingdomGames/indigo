package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._

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

  @JSExport
  val left: Int   = x
  @JSExport
  val right: Int  = x + width
  @JSExport
  val top: Int    = y
  @JSExport
  val bottom: Int = y + height

  @JSExport
  val horizontalCenter: Int  = x + (width / 2)
  @JSExport
  val verticalCenter: Int  = y + (height / 2)

  @JSExport
  def topLeft: PointDelegate     = new PointDelegate(left, top)
  @JSExport
  def topRight: PointDelegate    = new PointDelegate(right, top)
  @JSExport
  def bottomRight: PointDelegate = new PointDelegate(right, bottom)
  @JSExport
  def bottomLeft: PointDelegate  = new PointDelegate(left, bottom)

  @JSExport
  def corners =
    List(topLeft, topRight, bottomRight, bottomLeft).toJSArray

  @JSExport
  def isPointWithin(pt: PointDelegate): Boolean =
    pt.x >= left && pt.x < right && pt.y >= top && pt.y < bottom

  @JSExport
  def isPointWithin(x: Int, y: Int): Boolean = isPointWithin(new PointDelegate(x, y))


  def toInternal: Rectangle =
    Rectangle(Point(x, y), Point(width, height))
}

object RectangleDelegate {

  def fromRectangle(r: Rectangle): RectangleDelegate =
    new RectangleDelegate(r.x, r.y, r.width, r.height)

}
