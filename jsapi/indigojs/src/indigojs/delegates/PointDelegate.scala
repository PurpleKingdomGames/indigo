package indigojs.delegates

import scala.scalajs.js.annotation._

import indigo.shared.datatypes.Point
import indigojs.delegates.geometry.Vector2Delegate

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Point")
final class PointDelegate(_x: Int, _y: Int) {

  @JSExport
  val x = _x
  @JSExport
  val y = _y

  @JSExport
  def toVector: Vector2Delegate =
    new Vector2Delegate(x.toDouble, y.toDouble)

  def toInternal: Point =
    Point(x, y)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("PointHelper")
object PointDelegate {
  def fromPoint(pt: Point): PointDelegate =
    new PointDelegate(pt.x, pt.y)

  @JSExport
  def add(a: PointDelegate, b: PointDelegate): PointDelegate =
    new PointDelegate(a.x + b.x, a.y + b.y)

  @JSExport
  def subtract(a: PointDelegate, b: PointDelegate): PointDelegate =
    new PointDelegate(a.x - b.x, a.y - b.y)

  @JSExport
  def multiply(a: PointDelegate, b: PointDelegate): PointDelegate =
    new PointDelegate(a.x * b.x, a.y * b.y)

  @JSExport
  def divide(a: PointDelegate, b: PointDelegate): PointDelegate =
    new PointDelegate(a.x / b.x, a.y / b.y)
}
