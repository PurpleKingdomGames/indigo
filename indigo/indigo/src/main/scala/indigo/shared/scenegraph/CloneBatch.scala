package indigo.shared.scenegraph

import indigo.shared.datatypes._

/** Represents many clones of the same cloneblank, differentiated only by their transform data.
  */
final case class CloneBatch(
    id: CloneId,
    depth: Depth,
    x: Int,
    y: Int,
    rotation: Radians,
    scaleX: Double,
    scaleY: Double,
    cloneData: CloneTransformData,
    staticBatchKey: Option[BindingKey]
) extends DependentNode
    derives CanEqual:

  lazy val scale: Vector2  = Vector2(scaleX, scaleY)
  lazy val ref: Point      = Point.zero
  lazy val position: Point = Point(x, y)
  lazy val flip: Flip      = Flip.default

  def withCloneId(newCloneId: CloneId): CloneBatch =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): CloneBatch =
    this.copy(depth = newDepth)

  def withTransforms(
      newX: Int,
      newY: Int
  ): CloneBatch =
    this.copy(
      x = newX,
      y = newY
    )

  def withTransforms(
      newX: Int,
      newY: Int,
      newRotation: Radians
  ): CloneBatch =
    this.copy(
      x = newX,
      y = newY,
      rotation = newRotation
    )

  def withTransforms(
      newX: Int,
      newY: Int,
      newRotation: Radians,
      newScaleX: Double,
      newScaleY: Double
  ): CloneBatch =
    this.copy(
      x = newX,
      y = newY,
      rotation = newRotation,
      scaleX = newScaleX,
      scaleY = newScaleY
    )

  def withX(newX: Int): CloneBatch =
    this.copy(x = newX)
  def withY(newY: Int): CloneBatch =
    this.copy(y = newY)

  def withPosition(newX: Int, newY: Int): CloneBatch =
    this.copy(x = newX, y = newY)
  def withPosition(newPosition: Point): CloneBatch =
    withPosition(newPosition.x, newPosition.y)

  def withRotation(newRotation: Radians): CloneBatch =
    this.copy(rotation = newRotation)

  def withScaleX(newScaleX: Int): CloneBatch =
    this.copy(scaleX = newScaleX)
  def withScaleY(newScaleY: Int): CloneBatch =
    this.copy(scaleY = newScaleY)

  def withScale(newScaleX: Double, newScaleY: Double): CloneBatch =
    this.copy(scaleX = newScaleX, scaleY = newScaleY)
  def withScale(newScale: Vector2): CloneBatch =
    withScale(newScale.x, newScale.y)

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
      0,
      0,
      Radians.zero,
      1.0,
      1.0,
      cloneData,
      None
    )

  def apply(id: CloneId, x: Int, y: Int, cloneData: CloneTransformData): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      x,
      y,
      Radians.zero,
      1.0,
      1.0,
      cloneData,
      None
    )

  def apply(id: CloneId, x: Int, y: Int, rotation: Radians, cloneData: CloneTransformData): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      x,
      y,
      rotation,
      1.0,
      1.0,
      cloneData,
      None
    )

  def apply(
      id: CloneId,
      x: Int,
      y: Int,
      rotation: Radians,
      scaleX: Double,
      scaleY: Double,
      cloneData: CloneTransformData
  ): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      x,
      y,
      rotation,
      scaleX,
      scaleY,
      cloneData,
      None
    )
