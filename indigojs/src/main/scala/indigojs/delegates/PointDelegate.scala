package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.datatypes.Point

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Point")
final class PointDelegate(_x: Int, _y: Int) {

  @JSExport
  val x = _x
  @JSExport
  val y = _y

  def toInternal: Point =
    Point(x, y)
}

object PointDelegate {
  def fromPoint(pt: Point): PointDelegate =
    new PointDelegate(pt.x, pt.y)
}
