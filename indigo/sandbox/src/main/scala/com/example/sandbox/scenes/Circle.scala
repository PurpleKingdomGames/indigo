package com.example.sandbox.scenes

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Depth
import indigo.shared.scenegraph.SceneEntity
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Rectangle

final case class Circle(
    center: Point,
    radius: Int,
    depth: Depth,
    material: ShapeMaterial
) extends SceneEntity {

  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val flip: Flip        = Flip.default
  val ref: Point        = Point.zero

  def position: Point =
    center - radius - (if (material.strokeIsInside) 0 else material.strokeWidth) - 1

  def bounds: Rectangle =
    Rectangle(
      position,
      Point(radius * 2) + (if (material.strokeIsInside) 0 else material.strokeWidth * 2) + 2
    )
}
object Circle {

  def apply(center: Point, radius: Int, material: ShapeMaterial): Circle =
    Circle(
      center,
      radius,
      Depth(1),
      material
    )

}
