package indigojs.delegates

import indigo.shared.scenegraph.CloneBlank
import indigo.shared.scenegraph.CloneId

final class CloneBlankDelegate(val id: String, val cloneable: CloneableDelegate) {

  def toInternal: CloneBlank =
    new CloneBlank(new CloneId(id), cloneable.toInternal)

}
