package indigo.shared.scenegraph

import indigo.shared.datatypes._

/** Represents many clones of the same cloneblank, differentiated by their transform data and which part of the texture
  * it is cropped on.
  */
final case class CloneTiles(
    id: CloneId,
    depth: Depth,
    cloneData: CloneTileData,
    staticBatchKey: Option[BindingKey]
) extends DependentNode
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

  def addCloneData(additionalCloneData: CloneTileData): CloneTiles =
    this.copy(cloneData = cloneData ++ additionalCloneData)
  def addCloneData(x: Int, y: Int, cropX: Int, cropY: Int, cropWidth: Int, cropHeight: Int): CloneTiles =
    addCloneData(CloneTileData(x, y, cropX, cropY, cropWidth, cropHeight))
  def addCloneData(
      x: Int,
      y: Int,
      rotation: Radians,
      cropX: Int,
      cropY: Int,
      cropWidth: Int,
      cropHeight: Int
  ): CloneTiles =
    addCloneData(CloneTileData(x, y, rotation, cropX, cropY, cropWidth, cropHeight))
  def addCloneData(
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
    addCloneData(CloneTileData(x, y, rotation, scaleX, scaleY, cropX, cropY, cropWidth, cropHeight))

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneTiles =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneTiles =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneTiles =
    withMaybeStaticBatchKey(None)

object CloneTiles:

  def apply(id: CloneId, cloneData: CloneTileData): CloneTiles =
    CloneTiles(
      id,
      Depth.one,
      cloneData,
      None
    )
