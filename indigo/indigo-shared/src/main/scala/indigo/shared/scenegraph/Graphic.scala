package indigo.shared.scenegraph

import indigo.shared.materials.StandardMaterial
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Flip
import indigo.shared.materials.GLSLShader

/**
  * Graphics are used to draw images on the screen, in a cheap efficient but expressive way.
  * Graphics party trick is it's ability to crop images.
  *
  * @param position
  * @param rotation
  * @param scale
  * @param depth
  * @param ref
  * @param flip
  * @param crop
  * @param material
  */
final case class Graphic(
    material: StandardMaterial,
    crop: Rectangle,
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends EntityNode
    with Cloneable
    with RefPropertyMethod
    with SpacialPropertyMethods {

  def bounds: Rectangle =
    Rectangle(position, crop.size)

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial(newMaterial: StandardMaterial): Graphic =
    this.copy(material = newMaterial)

  def modifyMaterial(alter: StandardMaterial => StandardMaterial): Graphic =
    this.copy(material = alter(material))

  def moveTo(pt: Point): Graphic =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Graphic =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Graphic =
    moveTo(newPosition)

  def moveBy(pt: Point): Graphic =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Graphic =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Graphic =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Graphic =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Graphic =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Graphic =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Graphic =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Graphic =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Graphic =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Graphic =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): Graphic =
    this.copy(depth = newDepth)

  def flipHorizontal(isFlipped: Boolean): Graphic =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Graphic =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Graphic =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Graphic =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Graphic =
    withRef(Point(x, y))

  def withCrop(newCrop: Rectangle): Graphic =
    this.copy(crop = newCrop)
  def withCrop(x: Int, y: Int, width: Int, height: Int): Graphic =
    withCrop(Rectangle(x, y, width, height))

  def toGLSLShader: GLSLShader =
    material.toGLSLShader

}

object Graphic {

  def apply(x: Int, y: Int, width: Int, height: Int, depth: Int, material: StandardMaterial): Graphic =
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

  def apply(bounds: Rectangle, depth: Int, material: StandardMaterial): Graphic =
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

  def apply(width: Int, height: Int, material: StandardMaterial): Graphic =
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
}
