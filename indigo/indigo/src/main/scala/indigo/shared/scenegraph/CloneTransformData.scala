package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

/** Represents the standard allowable transformations of a clone.
  */
final case class CloneTransformData(
    x: Int,
    y: Int,
    rotation: Radians,
    scaleX: Double,
    scaleY: Double
) derives CanEqual:

  def |+|(other: CloneTransformData): CloneTransformData =
    CloneTransformData(
      x = x + other.x,
      y = y + other.y,
      rotation = rotation + other.rotation,
      scaleX = scaleX * other.scaleX,
      scaleY = scaleY * other.scaleY
    )

  def withPosition(newPosition: Point): CloneTransformData =
    this.copy(x = newPosition.x, y = newPosition.y)

  def withRotation(newRotation: Radians): CloneTransformData =
    this.copy(rotation = newRotation)

  def withScale(newScale: Vector2): CloneTransformData =
    this.copy(scaleX = newScale.x, scaleY = newScale.y)

object CloneTransformData:
  def apply(x: Int, y: Int): CloneTransformData =
    CloneTransformData(x, y, Radians.zero, 1.0d, 1.0d)

  def apply(x: Int, y: Int, rotation: Radians): CloneTransformData =
    CloneTransformData(x, y, rotation, 1.0d, 1.0d)

  def startAt(position: Point): CloneTransformData =
    CloneTransformData(position.x, position.y, Radians.zero, 1.0d, 1.0d)

  val identity: CloneTransformData =
    CloneTransformData(0, 0, Radians.zero, 1.0d, 1.0d)
