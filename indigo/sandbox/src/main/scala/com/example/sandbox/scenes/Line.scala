package com.example.sandbox.scenes

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Depth
import indigo.shared.scenegraph.SceneEntity
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Rectangle

final case class Line(
    depth: Depth,
    material: StrokeMaterial
) extends SceneEntity {

  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val flip: Flip        = Flip.default
  val ref: Point        = Point.zero

  lazy val position: Point =
    Point(
      Math.min(material.start.x, material.end.x),
      Math.min(material.start.y, material.end.y)
    ) - (material.strokeWidth / 2)

  lazy val bounds: Rectangle = {
    val w = Math.max(material.start.x, material.end.x) - position.x
    val h = Math.max(material.start.y, material.end.y) - position.y

    Rectangle(position, Point(Math.max(w, h)) + material.strokeWidth)
  }
}
object Line {

  def apply(material: StrokeMaterial): Line =
    Line(
      Depth(1),
      material
    )

}
