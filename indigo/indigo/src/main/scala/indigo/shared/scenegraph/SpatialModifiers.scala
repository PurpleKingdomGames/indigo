package indigo.shared.scenegraph

import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2

trait BasicSpatialModifiers[T <: SceneNode] {
  def withPosition(newPosition: Point): T
  def withRotation(newRotation: Radians): T
  def withScale(newScale: Vector2): T
  def withFlip(newFlip: Flip): T
}

trait SpatialModifiers[T <: SceneNode] extends BasicSpatialModifiers[T] {
  def moveTo(pt: Point): T
  def moveTo(x: Int, y: Int): T

  def moveBy(pt: Point): T
  def moveBy(x: Int, y: Int): T

  def rotateTo(angle: Radians): T
  def rotateBy(angle: Radians): T

  def scaleBy(amount: Vector2): T
  def scaleBy(x: Double, y: Double): T

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): T
  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): T

  def withRef(newRef: Point): T
  def withRef(x: Int, y: Int): T

  def flipHorizontal(isFlipped: Boolean): T
  def flipVertical(isFlipped: Boolean): T
}
