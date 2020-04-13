package indigojs.delegates.geometry

import scala.scalajs.js.annotation._

import indigo.shared.datatypes.Vector2
import indigojs.delegates.PointDelegate

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Vector2")
final class Vector2Delegate(_x: Double, _y: Double) {

  @JSExport
  val x = _x
  @JSExport
  val y = _y

  @JSExport
  def dot(other: Vector2Delegate): Double =
    Vector2.dotProduct(this.toInternal, other.toInternal)

  @JSExport
  def normalise: Vector2Delegate =
    Vector2Delegate.fromVector2(this.toInternal.normalise)

  @JSExport
  def toPoint: PointDelegate =
    new PointDelegate(x.toInt, y.toInt)

  @JSExport
  def applyMatrix4(matrix4: Matrix4Delegate): Vector2Delegate =
    Vector2Delegate.fromVector2(Vector2.applyMatrix4(this.toInternal, matrix4.toInternal))

  def toInternal: Vector2 =
    new Vector2(x, y)

}

object Vector2Delegate {
  def fromVector2(vec2: Vector2): Vector2Delegate =
    new Vector2Delegate(vec2.x, vec2.y)
}
