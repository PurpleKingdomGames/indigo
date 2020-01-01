package indigojs.delegates

import indigo.shared.scenegraph.Cloneable

trait CloneableDelegate {
  def toInternal: Cloneable
}
