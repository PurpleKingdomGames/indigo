package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2


/**
  * Represents the standard allowable transformations of a clone.
  *
  * @param position
  * @param rotation
  * @param scale
  * @param flipHorizontal
  * @param flipVertical
  */
final case class CloneTransformData(position: Point, rotation: Radians, scale: Vector2, flipHorizontal: Boolean, flipVertical: Boolean) derives CanEqual {

  def |+|(other: CloneTransformData): CloneTransformData =
    CloneTransformData(
      position = position + other.position,
      rotation = rotation + other.rotation,
      scale = scale * other.scale,
      flipHorizontal = if (flipHorizontal) !other.flipHorizontal else other.flipHorizontal,
      flipVertical = if (flipVertical) !other.flipVertical else other.flipVertical
    )

  def withPosition(newPosition: Point): CloneTransformData =
    this.copy(position = newPosition)

  def withRotation(newRotation: Radians): CloneTransformData =
    this.copy(rotation = newRotation)

  def withScale(newScale: Vector2): CloneTransformData =
    this.copy(scale = newScale)

  def withHorizontalFlip(isFlipped: Boolean): CloneTransformData =
    this.copy(flipHorizontal = isFlipped)

  def withVerticalFlip(isFlipped: Boolean): CloneTransformData =
    this.copy(flipVertical = isFlipped)
}

object CloneTransformData {
  def startAt(position: Point): CloneTransformData =
    CloneTransformData(position, Radians.zero, Vector2.one, false, false)

  val identity: CloneTransformData =
    CloneTransformData(Point.zero, Radians.zero, Vector2.one, false, false)
}
