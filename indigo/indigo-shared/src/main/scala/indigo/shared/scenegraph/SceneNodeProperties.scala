package indigo.shared.scenegraph

import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Flip

trait DepthProperty {
  def depth: Depth

  def withDepth(newDepth: Depth): SceneNode with DepthProperty
}

trait RefProperty {
  def ref: Point
}

trait SpacialProperties {
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip
}

trait RefPropertyMethod {
  def withRef(newRef: Point): SceneNode with RefProperty
}

trait SpacialPropertyMethods {
  def withPosition(newPosition: Point): SceneNode with SpacialProperties
  def withRotation(newRotation: Radians): SceneNode with SpacialProperties
  def withScale(newScale: Vector2): SceneNode with SpacialProperties
  def withFlip(newFlip: Flip): SceneNode with SpacialProperties
}
