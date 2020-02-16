package indigojs.delegates.clones

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.CloneTransformData
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Point

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("CloneTransformData")
final class CloneTransformDataDelegate(val x: Int, val y: Int, val rotation: Double, val scaleX: Double, val scaleY: Double) {
  def toInternal: CloneTransformData =
    new CloneTransformData(new Point(x, y), new Radians(rotation), new Vector2(scaleX, scaleY))
}
