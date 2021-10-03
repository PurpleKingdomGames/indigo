package indigo.shared.scenegraph

import indigo.shared.datatypes._

/** Represents many clones of the same cloneblank, differentiated only by their transform data.
  */
final case class CloneBatch(
    id: CloneId,
    depth: Depth,
    cloneData: CloneTransformData,
    staticBatchKey: Option[BindingKey]
) extends DependentNode
    derives CanEqual:

  lazy val scale: Vector2    = Vector2.one
  lazy val rotation: Radians = Radians.zero
  lazy val ref: Point        = Point.zero
  lazy val position: Point   = Point.zero
  lazy val flip: Flip        = Flip.default

  def withCloneId(newCloneId: CloneId): CloneBatch =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): CloneBatch =
    this.copy(depth = newDepth)

  def addCloneData(additionalCloneData: CloneTransformData): CloneBatch =
    this.copy(cloneData = cloneData ++ additionalCloneData)
  def addCloneData(x: Int, y: Int): CloneBatch =
    addCloneData(CloneTransformData(x, y))
  def addCloneData(x: Int, y: Int, rotation: Radians): CloneBatch =
    addCloneData(CloneTransformData(x, y, rotation))
  def addCloneData(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double): CloneBatch =
    addCloneData(CloneTransformData(x, y, rotation, scaleX, scaleY))

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneBatch =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneBatch =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneBatch =
    withMaybeStaticBatchKey(None)

object CloneBatch:

  def apply(id: CloneId, cloneData: CloneTransformData): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      cloneData,
      None
    )
