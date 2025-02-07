package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.*
import indigo.shared.events.GlobalEvent

/** Represents many clones of the same clone blank, differentiated only by their transform data.
  */
final case class CloneBatch(
    id: CloneId,
    cloneData: Batch[CloneBatchData],
    staticBatchKey: Option[BindingKey]
) extends DependentNode[CloneBatch] derives CanEqual:

  lazy val scale: Vector2    = Vector2.one
  lazy val rotation: Radians = Radians.zero
  lazy val ref: Point        = Point.zero
  lazy val position: Point   = Point.zero
  lazy val flip: Flip        = Flip.default

  def withCloneId(newCloneId: CloneId): CloneBatch =
    this.copy(id = newCloneId)

  def addClones(additionalClones: Batch[CloneBatchData]): CloneBatch =
    this.copy(cloneData = cloneData ++ additionalClones)
  def addClone(x: Int, y: Int): CloneBatch =
    addClones(Batch(CloneBatchData(x, y)))
  def addClone(x: Int, y: Int, rotation: Radians): CloneBatch =
    addClones(Batch(CloneBatchData(x, y, rotation)))
  def addClones(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double): CloneBatch =
    addClones(Batch(CloneBatchData(x, y, rotation, scaleX, scaleY)))

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneBatch =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneBatch =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneBatch =
    withMaybeStaticBatchKey(None)

  val eventHandlerEnabled: Boolean                                     = false
  def eventHandler: ((CloneBatch, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

object CloneBatch:

  def apply(id: CloneId, cloneData: Batch[CloneBatchData]): CloneBatch =
    CloneBatch(
      id,
      cloneData,
      None
    )

  def apply(id: CloneId, cloneData: CloneBatchData): CloneBatch =
    CloneBatch(
      id,
      Batch(cloneData),
      None
    )

  def apply(id: CloneId, cloneData: CloneBatchData*): CloneBatch =
    CloneBatch(
      id,
      Batch.fromSeq(cloneData),
      None
    )
