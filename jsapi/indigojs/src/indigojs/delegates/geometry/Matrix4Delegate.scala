package indigojs.delegates.geometry

import scala.scalajs.js.annotation._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import indigo.shared.datatypes.Matrix4

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Matrix4")
final class Matrix4Delegate(val mat: js.Array[Double]) {

  def toInternal: Matrix4 =
    new Matrix4(mat.toList)

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Matrix4Helper")
object Matrix4Delegate {

  @JSExport
  def identity: Matrix4Delegate =
    fromMatrix4(Matrix4.identity)

  def fromMatrix4(mat4: Matrix4): Matrix4Delegate =
    new Matrix4Delegate(mat4.mat.toJSArray)

}
