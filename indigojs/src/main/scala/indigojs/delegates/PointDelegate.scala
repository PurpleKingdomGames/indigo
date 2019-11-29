package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.datatypes.Point

@JSExportTopLevel("Point")
final class PointDelegate(val x: Int, val y: Int) {
  def toInternal: Point =
    Point(x, y)
}
