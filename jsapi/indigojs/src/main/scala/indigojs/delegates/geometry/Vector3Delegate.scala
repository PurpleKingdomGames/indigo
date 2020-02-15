package indigojs.delegates.geometry

import scala.scalajs.js.annotation._

import indigojs.delegates.PointDelegate
import indigo.shared.datatypes.Vector3

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Vector3")
final class Vector3Delegate(_x: Double, _y: Double, _z: Double) {

  @JSExport
  val x = _x
  @JSExport
  val y = _y
  @JSExport
  val z = _z

  def dot(other: Vector3Delegate): Double =
    Vector3.dotProduct(this.toInternal, other.toInternal)

  def toPoint: PointDelegate =
    new PointDelegate(x.toInt, y.toInt)

  def applyMatrix4(matrix4: Matrix4Delegate): Vector3Delegate =
    Vector3Delegate.fromVector3(Vector3.applyMatrix4(this.toInternal, matrix4.toInternal))

  def toInternal: Vector3 =
    new Vector3(x, y, z)

}

object Vector3Delegate {
  def fromVector3(vec3: Vector3): Vector3Delegate =
    new Vector3Delegate(vec3.x, vec3.y, vec3.z)
}
