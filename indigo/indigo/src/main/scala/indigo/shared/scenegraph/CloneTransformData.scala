package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

/** Represents the standard allowable transformations of a clone.
  */
final case class CloneTransformData(
    position: Point,
    rotation: Radians,
    scale: Vector2
) derives CanEqual:

  def |+|(other: CloneTransformData): CloneTransformData =
    CloneTransformData(
      position = position + other.position,
      rotation = rotation + other.rotation,
      scale = scale * other.scale
    )

  def withPosition(newPosition: Point): CloneTransformData =
    this.copy(position = newPosition)

  def withRotation(newRotation: Radians): CloneTransformData =
    this.copy(rotation = newRotation)

  def withScale(newScale: Vector2): CloneTransformData =
    this.copy(scale = newScale)

object CloneTransformData:
  def startAt(position: Point): CloneTransformData =
    CloneTransformData(position, Radians.zero, Vector2.one)

  val identity: CloneTransformData =
    CloneTransformData(Point.zero, Radians.zero, Vector2.one)
