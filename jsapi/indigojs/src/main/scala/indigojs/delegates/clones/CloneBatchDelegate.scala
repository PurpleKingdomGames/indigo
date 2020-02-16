package indigojs.delegates.clones

import indigojs.delegates.SceneGraphNodeDelegate
import indigo.shared.scenegraph.CloneBatch
import indigo.shared.scenegraph.CloneId
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Depth
import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("CloneBatch")
final class CloneBatchDelegate(val id: String, val depth: Int, val transform: CloneTransformDataDelegate, val clones: List[CloneTransformDataDelegate], val staticBatchId: Option[String]) extends SceneGraphNodeDelegate {
  def toInternal: CloneBatch = 
    new CloneBatch(CloneId(id), new Depth(depth), transform.toInternal, clones.map(_.toInternal), staticBatchId.map(k => new BindingKey(k)))
}
