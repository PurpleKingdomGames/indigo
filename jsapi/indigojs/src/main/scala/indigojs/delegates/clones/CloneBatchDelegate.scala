package indigojs.delegates.clones

import indigojs.delegates.SceneGraphNodeDelegate
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.CloneId
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Depth
import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("CloneBatch")
final class CloneBatchDelegate(_id: String, _depth: Int, _transform: CloneTransformDataDelegate, _clones: List[CloneTransformDataDelegate], _staticBatchId: Option[String]) extends SceneGraphNodeDelegate {

  @JSExport
  val id = _id
  @JSExport
  val depth = _depth
  @JSExport
  val transform = _transform
  @JSExport
  val clones = _clones
  @JSExport
  val staticBatchId = _staticBatchId

  def toInternal: CloneBatch = 
    new CloneBatch(CloneId(id), new Depth(depth), transform.toInternal, clones.map(_.toInternal), staticBatchId.map(k => new BindingKey(k)))
}
