package indigo.shared.scenegraph

import indigo.shared.datatypes._

/** Represents many clones of the same clone blank, differentiated only by their transform data.
  */
final case class CloneBatch(
    id: CloneId,
    depth: Depth,
    cloneData: List[CloneBatchData],
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

  def addClones(additionalClones: List[CloneBatchData]): CloneBatch =
    this.copy(cloneData = cloneData ++ additionalClones)
  def addClone(x: Int, y: Int): CloneBatch =
    addClones(List(CloneBatchData(x, y)))
  def addClone(x: Int, y: Int, rotation: Radians): CloneBatch =
    addClones(List(CloneBatchData(x, y, rotation)))
  def addClones(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double): CloneBatch =
    addClones(List(CloneBatchData(x, y, rotation, scaleX, scaleY)))

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneBatch =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneBatch =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneBatch =
    withMaybeStaticBatchKey(None)

object CloneBatch:

  def apply(id: CloneId, cloneData: List[CloneBatchData]): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      cloneData,
      None
    )

  def apply(id: CloneId, cloneData: CloneBatchData): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      List(cloneData),
      None
    )
