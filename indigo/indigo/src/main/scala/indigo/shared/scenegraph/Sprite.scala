package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationAction._
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.CycleLabel
import indigo.shared.datatypes._
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData

/** Sprites are used to represented key-frame animated screen elements.
  */
final case class Sprite[M <: Material](
    bindingKey: BindingKey,
    material: M,
    animationKey: AnimationKey,
    animationActions: List[AnimationAction],
    eventHandlerEnabled: Boolean,
    eventHandler: GlobalEvent => Option[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends DependentNode[Sprite[M]]
    with Cloneable
    with SpatialModifiers[Sprite[M]]
    derives CanEqual:

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withDepth(newDepth: Depth): Sprite[M] =
    this.copy(depth = newDepth)

  def withMaterial[MB <: Material](newMaterial: MB): Sprite[MB] =
    this.copy(material = newMaterial)

  def modifyMaterial[MB <: Material](alter: M => MB): Sprite[MB] =
    this.copy(material = alter(material))

  def moveTo(pt: Point): Sprite[M] =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Sprite[M] =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Sprite[M] =
    moveTo(newPosition)

  def moveBy(pt: Point): Sprite[M] =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Sprite[M] =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Sprite[M] =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Sprite[M] =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Sprite[M] =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Sprite[M] =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Sprite[M] =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Sprite[M] =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Sprite[M] =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Sprite[M] =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withBindingKey(newBindingKey: BindingKey): Sprite[M] =
    this.copy(bindingKey = newBindingKey)

  def flipHorizontal(isFlipped: Boolean): Sprite[M] =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Sprite[M] =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Sprite[M] =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Sprite[M] =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Sprite[M] =
    withRef(Point(x, y))

  def withAnimationKey(newAnimationKey: AnimationKey): Sprite[M] =
    this.copy(animationKey = newAnimationKey)

  def play(): Sprite[M] =
    this.copy(animationActions = animationActions ++ List(Play))

  def changeCycle(label: CycleLabel): Sprite[M] =
    this.copy(animationActions = animationActions ++ List(ChangeCycle(label)))

  def jumpToFirstFrame(): Sprite[M] =
    this.copy(animationActions = animationActions ++ List(JumpToFirstFrame))

  def jumpToLastFrame(): Sprite[M] =
    this.copy(animationActions = animationActions ++ List(JumpToLastFrame))

  def jumpToFrame(number: Int): Sprite[M] =
    this.copy(animationActions = animationActions ++ List(JumpToFrame(number)))

  def withEventHandler(f: GlobalEvent => Option[GlobalEvent]): Sprite[M] =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def enableEvents: Sprite[M] =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: Sprite[M] =
    this.copy(eventHandlerEnabled = false)

object Sprite:
  def apply[M <: Material](
      bindingKey: BindingKey,
      x: Int,
      y: Int,
      depth: Int,
      animationKey: AnimationKey,
      material: M
  ): Sprite[M] =
    Sprite(
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(depth),
      ref = Point.zero,
      flip = Flip.default,
      bindingKey = bindingKey,
      animationKey = animationKey,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      animationActions = Nil,
      material = material
    )

  def apply[M <: Material](
      bindingKey: BindingKey,
      position: Point,
      depth: Depth,
      rotation: Radians,
      scale: Vector2,
      animationKey: AnimationKey,
      ref: Point,
      eventHandler: GlobalEvent => Option[GlobalEvent],
      material: M
  ): Sprite[M] =
    Sprite(
      position = position,
      rotation = rotation,
      scale = scale,
      depth = depth,
      ref = ref,
      flip = Flip.default,
      bindingKey = bindingKey,
      animationKey = animationKey,
      eventHandlerEnabled = true,
      eventHandler = eventHandler,
      animationActions = Nil,
      material = material
    )

  def apply[M <: Material](bindingKey: BindingKey, animationKey: AnimationKey, material: M): Sprite[M] =
    Sprite(
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default,
      bindingKey = bindingKey,
      animationKey = animationKey,
      eventHandlerEnabled = false,
      eventHandler = Function.const(None),
      animationActions = Nil,
      material = material
    )
