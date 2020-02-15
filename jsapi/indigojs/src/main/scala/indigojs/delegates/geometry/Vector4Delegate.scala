package indigojs.delegates.geometry

import scala.scalajs.js.annotation._

import indigojs.delegates.PointDelegate
import indigo.shared.datatypes.Vector4

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Vector4")
final class Vector4Delegate(_x: Double, _y: Double, _z: Double, _w: Double) {

  @JSExport
  val x = _x
  @JSExport
  val y = _y
  @JSExport
  val z = _z
  @JSExport
  val w = _w

  def dot(other: Vector4Delegate): Double =
    Vector4.dotProduct(this.toInternal, other.toInternal)

  def toPoint: PointDelegate =
    new PointDelegate(x.toInt, y.toInt)

  def applyMatrix4(matrix4: Matrix4Delegate): Vector4Delegate =
    Vector4Delegate.fromVector4(Vector4.applyMatrix4(this.toInternal, matrix4.toInternal))

  def toInternal: Vector4 =
    new Vector4(x, y, z, w)

}

object Vector4Delegate {
  def fromVector4(vec4: Vector4): Vector4Delegate =
    new Vector4Delegate(vec4.x, vec4.y, vec4.z, vec4.w)
}
