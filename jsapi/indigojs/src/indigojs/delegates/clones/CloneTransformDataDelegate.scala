package indigojs.delegates.clones

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.CloneTransformData
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Point

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("CloneTransformData")
final class CloneTransformDataDelegate(_x: Int, _y: Int, _rotation: Double, _scaleX: Double, _scaleY: Double, _alpha: Double, _flipHorizontal: Boolean, _flipVertical: Boolean) {

  @JSExport
  val x = _x
  @JSExport
  val y = _y
  @JSExport
  val rotation = _rotation
  @JSExport
  val scaleX = _scaleX
  @JSExport
  val scaleY = _scaleY
  @JSExport
  val alpha = _alpha
  @JSExport
  val flipHorizontal = _flipHorizontal
  @JSExport
  val flipVertical = _flipVertical

  def toInternal: CloneTransformData =
    new CloneTransformData(new Point(x, y), new Radians(rotation), new Vector2(scaleX, scaleY), alpha, flipHorizontal, flipVertical)
}
