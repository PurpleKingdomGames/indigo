package indigo.shared.scenegraph

import indigo.shared.scenegraph.syntax.BasicSpatial
import indigo.shared.scenegraph.syntax.Spatial

import indigo.shared.materials.Material
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Flip
import indigo.shared.materials.ShaderData
import indigo.shared.BoundaryLocator

/** Graphics are used to draw images on the screen, in a cheap efficient but expressive way. Graphics party trick is
  * it's ability to crop images.
  *
  * @param position
  *   @param rotation
  * @param scale
  *   @param depth
  * @param ref
  *   @param flip
  * @param crop
  *   @param material
  */
final case class Graphic(
    material: Material,
    crop: Rectangle,
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends RenderNode
    with Cloneable derives CanEqual {

  def bounds: Rectangle =
    BoundaryLocator.findBounds(this, position, crop.size, ref)

  lazy val size: Size =
    crop.size

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial(newMaterial: Material): Graphic =
    this.copy(material = newMaterial)

  def modifyMaterial(alter: Material => Material): Graphic =
    this.copy(material = alter(material))

  def withDepth(newDepth: Depth): Graphic =
    this.copy(depth = newDepth)

  def withCrop(newCrop: Rectangle): Graphic =
    this.copy(crop = newCrop)
  def withCrop(x: Int, y: Int, width: Int, height: Int): Graphic =
    withCrop(Rectangle(x, y, width, height))

  def toShaderData: ShaderData =
    material.toShaderData

}

object Graphic {

  def apply(x: Int, y: Int, width: Int, height: Int, depth: Int, material: Material): Graphic =
    Graphic(
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(depth),
      ref = Point.zero,
      flip = Flip.default,
      crop = Rectangle(0, 0, width, height),
      material = material
    )

  def apply(bounds: Rectangle, depth: Int, material: Material): Graphic =
    Graphic(
      position = bounds.position,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(depth),
      ref = Point.zero,
      flip = Flip.default,
      crop = bounds,
      material = material
    )

  def apply(width: Int, height: Int, material: Material): Graphic =
    Graphic(
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(1),
      ref = Point.zero,
      flip = Flip.default,
      crop = Rectangle(0, 0, width, height),
      material = material
    )

  given BasicSpatial[Graphic] with
    extension (graphic: Graphic)
      def position: Point =
        graphic.position
      def rotation: Radians =
        graphic.rotation
      def scale: Vector2 =
        graphic.scale
      def depth: Depth =
        graphic.depth
      def ref: Point =
        graphic.ref
      def flip: Flip =
        graphic.flip

      def withPosition(newPosition: Point): Graphic =
        graphic.copy(position = newPosition)
      def withRotation(newRotation: Radians): Graphic =
        graphic.copy(rotation = newRotation)
      def withScale(newScale: Vector2): Graphic =
        graphic.copy(scale = newScale)
      def withRef(newRef: Point): Graphic =
        graphic.copy(ref = newRef)
      def withRef(x: Int, y: Int): Graphic =
        withRef(Point(x, y))
      def withDepth(newDepth: Depth): Graphic =
        graphic.copy(depth = newDepth)
      def withFlip(newFlip: Flip): Graphic =
        graphic.copy(flip = newFlip)

  given Spatial[Graphic] = Spatial.default[Graphic]
}
