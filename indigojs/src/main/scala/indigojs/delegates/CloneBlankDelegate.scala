package indigojs.delegates

import indigo.shared.scenegraph.CloneBlank
import indigo.shared.scenegraph.CloneId

import scala.scalajs.js.annotation._

@JSExportTopLevel("CloneBlank")
final class CloneBlankDelegate(val id: String, val cloneable: CloneableDelegate) {

  def toInternal: CloneBlank =
    new CloneBlank(new CloneId(id), cloneable.toInternal)

}
