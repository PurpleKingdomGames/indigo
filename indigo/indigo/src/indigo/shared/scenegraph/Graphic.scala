package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.shader.ShaderData

/** Graphics are used to draw images on the screen, in a cheap efficient but expressive way. Graphic's party trick is
  * it's ability to crop images.
  */
final case class Graphic[M <: Material](
    material: M,
    crop: Rectangle,
    eventHandlerEnabled: Boolean,
    eventHandler: ((Graphic[?], GlobalEvent)) => Option[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    flip: Flip
) extends RenderNode[Graphic[M]]
    with Cloneable
    with SpatialModifiers[Graphic[M]] derives CanEqual:

  def bounds: Rectangle =
    BoundaryLocator.findBounds(this, position, crop.size, ref)

  lazy val size: Size =
    crop.size

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial[MB <: Material](newMaterial: MB): Graphic[MB] =
    this.copy(material = newMaterial)

  def modifyMaterial[MB <: Material](alter: M => MB): Graphic[MB] =
    this.copy(material = alter(material))

  def moveTo(pt: Point): Graphic[M] =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Graphic[M] =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Graphic[M] =
    moveTo(newPosition)

  def moveBy(pt: Point): Graphic[M] =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Graphic[M] =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Graphic[M] =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Graphic[M] =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Graphic[M] =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Graphic[M] =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Graphic[M] =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Graphic[M] =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Graphic[M] =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Graphic[M] =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def flipHorizontal(isFlipped: Boolean): Graphic[M] =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Graphic[M] =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Graphic[M] =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Graphic[M] =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Graphic[M] =
    withRef(Point(x, y))

  def withCrop(newCrop: Rectangle): Graphic[M] =
    this.copy(crop = newCrop)
  def withCrop(x: Int, y: Int, width: Int, height: Int): Graphic[M] =
    withCrop(Rectangle(x, y, width, height))

  lazy val toShaderData: ShaderData =
    material.toShaderData

  def withEventHandler(f: ((Graphic[?], GlobalEvent)) => Option[GlobalEvent]): Graphic[M] =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def onEvent(f: PartialFunction[(Graphic[?], GlobalEvent), GlobalEvent]): Graphic[M] =
    withEventHandler(f.lift)
  def enableEvents: Graphic[M] =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: Graphic[M] =
    this.copy(eventHandlerEnabled = false)

object Graphic:

  def apply[M <: Material](x: Int, y: Int, width: Int, height: Int, material: M): Graphic[M] =
    Graphic(
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default,
      crop = Rectangle(0, 0, width, height),
      material = material
    )

  def apply[M <: Material](bounds: Rectangle, material: M): Graphic[M] =
    Graphic(
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = bounds.position,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default,
      crop = bounds,
      material = material
    )

  def apply[M <: Material](width: Int, height: Int, material: M): Graphic[M] =
    Graphic(
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default,
      crop = Rectangle(0, 0, width, height),
      material = material
    )

  def apply[M <: Material](size: Size, material: M): Graphic[M] =
    Graphic(
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default,
      crop = Rectangle(Point.zero, size),
      material = material
    )
