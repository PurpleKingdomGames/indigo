package indigo.shared.scenegraph

import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.events.GlobalEvent
import indigo.shared.shader.ShaderData

final case class BlankEntity(
    size: Size,
    eventHandlerEnabled: Boolean,
    eventHandler: ((BlankEntity, GlobalEvent)) => Option[GlobalEvent],
    shaderData: ShaderData,
    position: Point,
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    flip: Flip
) extends EntityNode[BlankEntity]
    with Cloneable
    with SpatialModifiers[BlankEntity] derives CanEqual:

  lazy val x: Int      = position.x
  lazy val y: Int      = position.y
  lazy val width: Int  = size.width
  lazy val height: Int = size.height

  def moveTo(pt: Point): BlankEntity =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): BlankEntity =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): BlankEntity =
    moveTo(newPosition)

  def moveBy(pt: Point): BlankEntity =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): BlankEntity =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): BlankEntity =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): BlankEntity =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): BlankEntity =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): BlankEntity =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): BlankEntity =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): BlankEntity =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): BlankEntity =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): BlankEntity =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def flipHorizontal(isFlipped: Boolean): BlankEntity =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): BlankEntity =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): BlankEntity =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): BlankEntity =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): BlankEntity =
    withRef(Point(x, y))

  def resizeTo(newSize: Size): BlankEntity =
    this.copy(size = newSize)
  def resizeTo(w: Int, h: Int): BlankEntity =
    resizeTo(Size(width, height))
  def withSize(newSize: Size): BlankEntity =
    resizeTo(newSize)

  def resizeBy(amount: Size): BlankEntity =
    this.copy(size = size + amount)
  def resizeBy(w: Int, h: Int): BlankEntity =
    resizeBy(Size(w, h))

  lazy val toShaderData: ShaderData =
    shaderData

  def withShaderData(newShaderData: ShaderData): BlankEntity =
    this.copy(shaderData = newShaderData)

  def modifyShaderData(alter: ShaderData => ShaderData): BlankEntity =
    this.copy(shaderData = alter(shaderData))

  def withEventHandler(f: ((BlankEntity, GlobalEvent)) => Option[GlobalEvent]): BlankEntity =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def onEvent(f: PartialFunction[(BlankEntity, GlobalEvent), GlobalEvent]): BlankEntity =
    withEventHandler(f.lift)
  def enableEvents: BlankEntity =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: BlankEntity =
    this.copy(eventHandlerEnabled = false)

object BlankEntity:

  def apply(shaderData: ShaderData): BlankEntity =
    BlankEntity(
      size = Size.zero,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      shaderData = shaderData,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(x: Int, y: Int, width: Int, height: Int, shaderData: ShaderData): BlankEntity =
    BlankEntity(
      size = Size(width, height),
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      shaderData = shaderData,
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(bounds: Rectangle, shaderData: ShaderData): BlankEntity =
    BlankEntity(
      size = bounds.size,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      shaderData = shaderData,
      position = bounds.position,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(width: Int, height: Int, shaderData: ShaderData): BlankEntity =
    BlankEntity(
      size = Size(width, height),
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      shaderData = shaderData,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply(size: Size, shaderData: ShaderData): BlankEntity =
    BlankEntity(
      size = size,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      shaderData = shaderData,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      flip = Flip.default
    )
