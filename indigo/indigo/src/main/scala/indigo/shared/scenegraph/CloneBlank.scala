package indigo.shared.scenegraph

/** Used as the blueprint for any clones that want to copy it.
  *
  * @param id
  *   The CloneId of this blank
  * @param cloneable
  *   The primitive to clone, can be a Shape, Graphic or Sprite, or any custom entity that extends Cloneable
  * @param isStatic
  *   Static clone blanks are only processed once and cached. This means that static sprites will never play their
  *   animations!
  */
final case class CloneBlank(id: CloneId, cloneable: () => Cloneable, isStatic: Boolean) derives CanEqual:
  def withCloneId(newCloneId: CloneId): CloneBlank =
    this.copy(id = newCloneId)

  def withCloneable(newCloneable: => Cloneable): CloneBlank =
    this.copy(cloneable = () => newCloneable)

  def static: CloneBlank =
    this.copy(isStatic = true)
  def dynamic: CloneBlank =
    this.copy(isStatic = false)

object CloneBlank:
  def apply(id: CloneId, cloneable: => Cloneable): CloneBlank =
    CloneBlank(id, () => cloneable, false)

/** Used to distingush between cloneable and non-clonable scene graph nodes.
  */
trait Cloneable

/** A CloneId is used to connect a Clone instance to a CloneBlank.
  */
opaque type CloneId = String
object CloneId:
  inline def apply(value: String): CloneId             = value
  extension (cid: CloneId) inline def toString: String = cid
