package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.collections.Batch
import indigo.shared.datatypes._
import indigo.shared.events.GlobalEvent

sealed trait Clones:
  def id: CloneId
  def depth: Depth
  def staticBatchKey: Option[BindingKey]

object Clones:

  final case class Instances(
      id: CloneId,
      depth: Depth,
      data: Batch[CloneBatchData],
      staticBatchKey: Option[BindingKey]
  ) extends Clones
      with DependentNode[Instances]
      derives CanEqual:

    lazy val scale: Vector2    = Vector2.one
    lazy val rotation: Radians = Radians.zero
    lazy val ref: Point        = Point.zero
    lazy val position: Point   = Point.zero
    lazy val flip: Flip        = Flip.default

    def withCloneId(newCloneId: CloneId): Instances =
      this.copy(id = newCloneId)

    def withDepth(newDepth: Depth): Instances =
      this.copy(depth = newDepth)

    def addClones(additionalClones: Batch[CloneBatchData]): Instances =
      this.copy(data = data ++ additionalClones)
    def addClone(x: Int, y: Int): Instances =
      addClones(Batch(CloneBatchData(x, y)))
    def addClone(x: Int, y: Int, rotation: Radians): Instances =
      addClones(Batch(CloneBatchData(x, y, rotation)))
    def addClones(x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double): Instances =
      addClones(Batch(CloneBatchData(x, y, rotation, scaleX, scaleY)))

    def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): Instances =
      this.copy(staticBatchKey = maybeKey)

    def withStaticBatchKey(key: BindingKey): Instances =
      withMaybeStaticBatchKey(Option(key))

    def clearStaticBatchKey: Instances =
      withMaybeStaticBatchKey(None)

    val eventHandlerEnabled: Boolean                                    = false
    def eventHandler: ((Instances, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

  object Instances:
    def apply(id: CloneId, data: Batch[CloneBatchData]): Instances =
      Instances(
        id,
        Depth.zero,
        data,
        None
      )

    def apply(id: CloneId, data: CloneBatchData): Instances =
      Instances(
        id,
        Depth.zero,
        Batch(data),
        None
      )

    def apply(id: CloneId, data: CloneBatchData*): Instances =
      Instances(
        id,
        Depth.zero,
        Batch.fromSeq(data),
        None
      )

  final case class RawInstances(
      id: CloneId,
      depth: Depth,
      count: Int,
      data: Batch[Float],
      staticBatchKey: Option[BindingKey]
  ) extends Clones
      with DependentNode[RawInstances]
      derives CanEqual:

    lazy val scale: Vector2    = Vector2.one
    lazy val rotation: Radians = Radians.zero
    lazy val ref: Point        = Point.zero
    lazy val position: Point   = Point.zero
    lazy val flip: Flip        = Flip.default

    def withCloneId(newCloneId: CloneId): RawInstances =
      this.copy(id = newCloneId)

    def withDepth(newDepth: Depth): RawInstances =
      this.copy(depth = newDepth)

    def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): RawInstances =
      this.copy(staticBatchKey = maybeKey)

    def withStaticBatchKey(key: BindingKey): RawInstances =
      withMaybeStaticBatchKey(Option(key))

    def clearStaticBatchKey: RawInstances =
      withMaybeStaticBatchKey(None)

    val eventHandlerEnabled: Boolean                                       = false
    def eventHandler: ((RawInstances, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

  object RawInstances:
    def apply(id: CloneId, count: Int, data: Batch[Float]): RawInstances =
      RawInstances(
        id,
        Depth.zero,
        count,
        data,
        None
      )

  /** Represents many clones of the same cloneblank, differentiated by their transform data and which part of the
    * texture it is cropped on.
    */
  final case class Tiles(
      id: CloneId,
      depth: Depth,
      data: Batch[CloneTileData],
      staticBatchKey: Option[BindingKey]
  ) extends Clones
      with DependentNode[Tiles]
      derives CanEqual:

    lazy val scale: Vector2    = Vector2.one
    lazy val rotation: Radians = Radians.zero
    lazy val ref: Point        = Point.zero
    lazy val position: Point   = Point.zero
    lazy val flip: Flip        = Flip.default

    def withCloneId(newCloneId: CloneId): Tiles =
      this.copy(id = newCloneId)

    def withDepth(newDepth: Depth): Tiles =
      this.copy(depth = newDepth)

    def addClones(additionalClones: Batch[CloneTileData]): Tiles =
      this.copy(data = data ++ additionalClones)
    def addClone(x: Int, y: Int, cropX: Int, cropY: Int, cropWidth: Int, cropHeight: Int): Tiles =
      addClones(Batch(CloneTileData(x, y, cropX, cropY, cropWidth, cropHeight)))
    def addClone(
        x: Int,
        y: Int,
        rotation: Radians,
        cropX: Int,
        cropY: Int,
        cropWidth: Int,
        cropHeight: Int
    ): Tiles =
      addClones(Batch(CloneTileData(x, y, rotation, cropX, cropY, cropWidth, cropHeight)))
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
    ): Tiles =
      addClones(Batch(CloneTileData(x, y, rotation, scaleX, scaleY, cropX, cropY, cropWidth, cropHeight)))

    def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): Tiles =
      this.copy(staticBatchKey = maybeKey)

    def withStaticBatchKey(key: BindingKey): Tiles =
      withMaybeStaticBatchKey(Option(key))

    def clearStaticBatchKey: Tiles =
      withMaybeStaticBatchKey(None)

    val eventHandlerEnabled: Boolean                                = false
    def eventHandler: ((Tiles, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

  object Tiles:

    def apply(id: CloneId, data: Batch[CloneTileData]): Tiles =
      Tiles(
        id,
        Depth.zero,
        data,
        None
      )

    def apply(id: CloneId, data: CloneTileData): Tiles =
      Tiles(
        id,
        Depth.zero,
        Batch(data),
        None
      )

    def apply(id: CloneId, data: CloneTileData*): Tiles =
      Tiles(
        id,
        Depth.zero,
        Batch.fromSeq(data),
        None
      )

  final case class RawTiles(
      id: CloneId,
      depth: Depth,
      count: Int,
      data: Batch[Float],
      staticBatchKey: Option[BindingKey]
  ) extends Clones
      with DependentNode[RawTiles]
      derives CanEqual:

    lazy val scale: Vector2    = Vector2.one
    lazy val rotation: Radians = Radians.zero
    lazy val ref: Point        = Point.zero
    lazy val position: Point   = Point.zero
    lazy val flip: Flip        = Flip.default

    def withCloneId(newCloneId: CloneId): RawTiles =
      this.copy(id = newCloneId)

    def withDepth(newDepth: Depth): RawTiles =
      this.copy(depth = newDepth)

    def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): RawTiles =
      this.copy(staticBatchKey = maybeKey)

    def withStaticBatchKey(key: BindingKey): RawTiles =
      withMaybeStaticBatchKey(Option(key))

    def clearStaticBatchKey: RawTiles =
      withMaybeStaticBatchKey(None)

    val eventHandlerEnabled: Boolean                                   = false
    def eventHandler: ((RawTiles, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

  object RawTiles:

    def apply(id: CloneId, count: Int, data: Batch[Float]): RawTiles =
      RawTiles(
        id,
        Depth.zero,
        count,
        data,
        None
      )
