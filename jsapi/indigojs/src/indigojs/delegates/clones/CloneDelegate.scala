package indigojs.delegates.clones

import indigojs.delegates.SceneGraphNodeDelegate
import indigo.shared.scenegraph.Clone
import indigo.shared.datatypes.Depth
import indigo.shared.scenegraph.CloneId
import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Clone")
final class CloneDelegate(_id: String, _depth: Int, _transform: CloneTransformDataDelegate) extends SceneGraphNodeDelegate {

  @JSExport
  val id = _id
  @JSExport
  val depth = _depth
  @JSExport
  val transform = _transform

  @JSExport
  def withTransforms(x: Int, y: Int, rotation: Double, scaleX: Double, scaleY: Double): CloneDelegate =
    new CloneDelegate(id, depth, new CloneTransformDataDelegate(x, y, rotation, scaleX, scaleY))

  @JSExport
  def withPosition(x: Int, y: Int): CloneDelegate =
    new CloneDelegate(id, depth, new CloneTransformDataDelegate(x, y, transform.rotation, transform.scaleX, transform.scaleY))

  @JSExport
  def withRotation(rotation: Double): CloneDelegate =
    new CloneDelegate(id, depth, new CloneTransformDataDelegate(transform.x, transform.y, rotation, transform.scaleX, transform.scaleY))

  @JSExport
  def withScale(scaleX: Double, scaleY: Double): CloneDelegate =
    new CloneDelegate(id, depth, new CloneTransformDataDelegate(transform.x, transform.y, transform.rotation, scaleX, scaleY))

  def toInternal: Clone=
    new Clone(new CloneId(id), new Depth(depth), transform.toInternal)
}
