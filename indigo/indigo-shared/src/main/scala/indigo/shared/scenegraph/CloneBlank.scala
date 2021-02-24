package indigo.shared.scenegraph

/**
  * Used as the blueprint for any clones that want to copy it.
  *
  * @param id
  * @param cloneable
  */
final case class CloneBlank(id: CloneId, cloneable: Cloneable) {
  def withCloneId(newCloneId: CloneId): CloneBlank =
    this.copy(id = newCloneId)

  def withCloneable(newCloneable: Cloneable): CloneBlank =
    this.copy(cloneable = newCloneable)
}

/**
  * Used to distingush between cloneable and non-clonable scene graph nodes.
  */
trait Cloneable

/**
  * A CloneId is used to connect a Clone instance to a CloneBlank.
  *
  * @param value
  */
final case class CloneId(value: String) extends AnyVal
