package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes._
import indigo.shared.events.GlobalEvent

/** Represents many clones of the same cloneblank, differentiated by their transform data and which part of the texture
  * it is cropped on.
  */
final case class CloneTiles(
    id: CloneId,
    depth: Depth,
    cloneData: Array[CloneTileData],
    staticBatchKey: Option[BindingKey]
) extends DependentNode[CloneTiles]
    derives CanEqual:

  lazy val scale: Vector2    = Vector2.one
  lazy val rotation: Radians = Radians.zero
  lazy val ref: Point        = Point.zero
  lazy val position: Point   = Point.zero
  lazy val flip: Flip        = Flip.default

  def withCloneId(newCloneId: CloneId): CloneTiles =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): CloneTiles =
    this.copy(depth = newDepth)
  def addClones(additionalClones: Array[CloneTileData]): CloneTiles =
    this.copy(cloneData = cloneData ++ additionalClones)
  def addClone(x: Int, y: Int, cropX: Int, cropY: Int, cropWidth: Int, cropHeight: Int): CloneTiles =
    addClones(Array(CloneTileData(x, y, cropX, cropY, cropWidth, cropHeight)))
  def addClone(x: Int, y: Int, rotation: Radians, cropX: Int, cropY: Int, cropWidth: Int, cropHeight: Int): CloneTiles =
    addClones(Array(CloneTileData(x, y, rotation, cropX, cropY, cropWidth, cropHeight)))
  def addClones(
      x: Int,
      y: Int,
      rotation: Radians,
      scaleX: Double,
      scaleY: Double,
      cropX: Int,
      cropY: Int,
      cropWidth: Int,
      cropHeight: Int
  ): CloneTiles =
    addClones(Array(CloneTileData(x, y, rotation, scaleX, scaleY, cropX, cropY, cropWidth, cropHeight)))

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneTiles =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneTiles =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneTiles =
    withMaybeStaticBatchKey(None)

  val eventHandlerEnabled: Boolean                     = false
  def eventHandler: GlobalEvent => Option[GlobalEvent] = Function.const(None)

object CloneTiles:

  def apply(id: CloneId, cloneData: Array[CloneTileData]): CloneTiles =
    CloneTiles(
      id,
      Depth.zero,
      cloneData,
      None
    )

  def apply(id: CloneId, cloneData: CloneTileData): CloneTiles =
    CloneTiles(
      id,
      Depth.zero,
      Array(cloneData),
      None
    )

  def apply(id: CloneId, cloneData: CloneTileData*): CloneTiles =
    CloneTiles(
      id,
      Depth.zero,
      cloneData.toArray,
      None
    )
