package indigo.shared.scenegraph

import indigo.shared.datatypes._

/** A single cloned instance of a cloneblank
  */
final case class Clone(
    id: CloneId,
    depth: Depth,
    x: Int,
    y: Int,
    rotation: Radians,
    scaleX: Double,
    scaleY: Double,
    flipHorizontal: Boolean,
    flipVertical: Boolean
) extends DependentNode
    with BasicSpatialModifiers[Clone]
    derives CanEqual:
  lazy val scale: Vector2  = Vector2(scaleX, scaleY)
  lazy val ref: Point      = Point.zero
  lazy val position: Point = Point(x, y)
  lazy val flip: Flip      = Flip(flipHorizontal, flipVertical)

  def withCloneId(newCloneId: CloneId): Clone =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): Clone =
    this.copy(depth = newDepth)

  def withTransforms(
      newX: Int,
      newY: Int
  ): Clone =
    this.copy(
      x = newX,
      y = newY
    )

  def withTransforms(
      newX: Int,
      newY: Int,
      newRotation: Radians
  ): Clone =
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
  ): Clone =
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
  ): Clone =
    this.copy(
      x = newX,
      y = newY,
      rotation = newRotation,
      scaleX = newScaleX,
      scaleY = newScaleY,
      flipHorizontal = newFlipHorizontal,
      flipVertical = newFlipVertical
    )

  def applyCloneTransformData(transform: CloneTransformData): Clone =
    withTransforms(
      transform.position.x,
      transform.position.y,
      transform.rotation,
      transform.scale.x,
      transform.scale.y,
      transform.flipHorizontal,
      transform.flipVertical
    )

  def withX(newX: Int): Clone =
    this.copy(x = newX)
  def withY(newY: Int): Clone =
    this.copy(y = newY)

  def withPosition(newX: Int, newY: Int): Clone =
    this.copy(x = newX, y = newY)
  def withPosition(newPosition: Point): Clone =
    withPosition(newPosition.x, newPosition.y)

  def withRotation(newRotation: Radians): Clone =
    this.copy(rotation = newRotation)

  def withScaleX(newScaleX: Int): Clone =
    this.copy(scaleX = newScaleX)
  def withScaleY(newScaleY: Int): Clone =
    this.copy(scaleY = newScaleY)

  def withScale(newScaleX: Double, newScaleY: Double): Clone =
    this.copy(scaleX = newScaleX, scaleY = newScaleY)
  def withScale(newScale: Vector2): Clone =
    withScale(newScale.x, newScale.y)

  def withHorizontalFlip(isFlipped: Boolean): Clone =
    this.copy(flipHorizontal = isFlipped)
  def withVerticalFlip(isFlipped: Boolean): Clone =
    this.copy(flipVertical = isFlipped)

  def withFlip(flipH: Boolean, flipV: Boolean): Clone =
    this.copy(flipHorizontal = flipH, flipVertical = flipV)
  def withFlip(newFlip: Flip): Clone =
    withFlip(newFlip.horizontal, newFlip.vertical)

object Clone:
  def apply(id: CloneId): Clone =
    Clone(
      id,
      Depth.one,
      0,
      0,
      Radians.zero,
      1.0,
      1.0,
      false,
      false
    )

  def apply(id: CloneId, x: Int, y: Int): Clone =
    Clone(
      id,
      Depth.one,
      x,
      y,
      Radians.zero,
      1.0,
      1.0,
      false,
      false
    )

  def apply(id: CloneId, x: Int, y: Int, rotation: Radians): Clone =
    Clone(
      id,
      Depth.one,
      x,
      y,
      rotation,
      1.0,
      1.0,
      false,
      false
    )

  def apply(id: CloneId, x: Int, y: Int, rotation: Radians, scaleX: Double, scaleY: Double): Clone =
    Clone(
      id,
      Depth.one,
      x,
      y,
      rotation,
      scaleX,
      scaleY,
      false,
      false
    )
