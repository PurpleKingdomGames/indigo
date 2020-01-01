package indigojs.delegates

import indigo.shared.scenegraph.CloneBlank
import indigo.shared.scenegraph.CloneId

import scala.scalajs.js.annotation._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("CloneBlank")
final class CloneBlankDelegate(_id: String, _cloneable: CloneableDelegate) {

  @JSExport
  val id = _id
  @JSExport
  val cloneable = _cloneable


  def toInternal: CloneBlank =
    new CloneBlank(new CloneId(id), cloneable.toInternal)

}
