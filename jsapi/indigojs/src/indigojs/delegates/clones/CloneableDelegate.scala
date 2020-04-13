package indigojs.delegates.clones

import indigo.shared.scenegraph.Cloneable

trait CloneableDelegate {
  def toInternal: Cloneable
}
