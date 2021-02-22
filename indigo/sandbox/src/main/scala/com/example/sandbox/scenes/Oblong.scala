package com.example.sandbox.scenes

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Depth
import indigo.shared.scenegraph.SceneEntity
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Rectangle

final case class Oblong(
    surface: Rectangle,
    depth: Depth,
    material: OblongMaterial
) extends SceneEntity {

  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val flip: Flip        = Flip.default
  val ref: Point        = Point.zero

  lazy val position: Point =
    surface.position - (material.strokeWidth / 2)

  lazy val bounds: Rectangle =
    Rectangle(
      position,
      surface.size + material.strokeWidth
    )
}
object Oblong {

  def apply(surface: Rectangle, material: OblongMaterial): Oblong =
    Oblong(
      surface,
      Depth(1),
      material
    )

}
