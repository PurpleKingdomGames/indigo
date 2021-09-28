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
    flipHorizontal: Boolean,
    flipVertical: Boolean,
    clones: List[CloneTransformData],
    staticBatchKey: Option[BindingKey]
) extends DependentNode
    with BasicSpatialModifiers[CloneBatch]
    derives CanEqual:

  lazy val scale: Vector2  = Vector2(scaleX, scaleY)
  lazy val ref: Point      = Point.zero
  lazy val position: Point = Point(x, y)
  lazy val flip: Flip      = Flip(flipHorizontal, flipVertical)

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

  def withTransforms(
      newX: Int,
      newY: Int,
      newRotation: Radians,
      newScaleX: Double,
      newScaleY: Double,
      newFlipHorizontal: Boolean,
      newFlipVertical: Boolean
  ): CloneBatch =
    this.copy(
      x = newX,
      y = newY,
      rotation = newRotation,
      scaleX = newScaleX,
      scaleY = newScaleY,
      flipHorizontal = newFlipHorizontal,
      flipVertical = newFlipVertical
    )

  def applyCloneTransformData(transform: CloneTransformData): CloneBatch =
    withTransforms(
      transform.position.x,
      transform.position.y,
      transform.rotation,
      transform.scale.x,
      transform.scale.y,
      transform.flipHorizontal,
      transform.flipVertical
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

  def withHorizontalFlip(isFlipped: Boolean): CloneBatch =
    this.copy(flipHorizontal = isFlipped)
  def withVerticalFlip(isFlipped: Boolean): CloneBatch =
    this.copy(flipVertical = isFlipped)

  def withFlip(flipH: Boolean, flipV: Boolean): CloneBatch =
    this.copy(flipHorizontal = flipH, flipVertical = flipV)
  def withFlip(newFlip: Flip): CloneBatch =
    withFlip(newFlip.horizontal, newFlip.vertical)
    
  def withClones(newClones: List[CloneTransformData]): CloneBatch =
    this.copy(clones = newClones)

  def addClones(additionalClones: List[CloneTransformData]): CloneBatch =
    this.copy(clones = clones ++ additionalClones)

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneBatch =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneBatch =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneBatch =
    withMaybeStaticBatchKey(None)

object CloneBatch:

  def apply(id: CloneId, clones: List[CloneTransformData]): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      0,
      0,
      Radians.zero,
      1.0,
      1.0,
      false,
      false,
      clones,
      None
    )

  def apply(id: CloneId, x: Int, y: Int, clones: List[CloneTransformData]): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      x,
      y,
      Radians.zero,
      1.0,
      1.0,
      false,
      false,
      clones,
      None
    )

  def apply(id: CloneId, x: Int, y: Int, rotation: Radians, clones: List[CloneTransformData]): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      x,
      y,
      rotation,
      1.0,
      1.0,
      false,
      false,
      clones,
      None
    )

  def apply(id: CloneId, x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double, clones: List[CloneTransformData]): CloneBatch =
    CloneBatch(
      id,
      Depth.one,
      x,
      y,
      rotation,
      scaleX,
      scaleY,
      false,
      false,
      clones,
      None
    )
