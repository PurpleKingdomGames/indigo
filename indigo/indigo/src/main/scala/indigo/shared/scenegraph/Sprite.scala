package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.animation.AnimationAction
import indigo.shared.animation.AnimationAction._
import indigo.shared.animation.AnimationKey
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData
import indigo.shared.BoundaryLocator
import indigo.shared.animation.CycleLabel
import indigo.shared.datatypes._

/** Sprites are used to represented key-frame animated screen elements.
  */
final case class Sprite(
    bindingKey: BindingKey,
    material: Material,
    animationKey: AnimationKey,
    animationActions: List[AnimationAction],
    eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends DependentNode
    with EventHandler
    with Cloneable
    with SpatialModifiers[Sprite] derives CanEqual {

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
    locator.spriteBounds(this)

  def withDepth(newDepth: Depth): Sprite =
    this.copy(depth = newDepth)

  def withMaterial(newMaterial: Material): Sprite =
    this.copy(material = newMaterial)

  def modifyMaterial(alter: Material => Material): Sprite =
    this.copy(material = alter(material))

  def moveTo(pt: Point): Sprite =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Sprite =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Sprite =
    moveTo(newPosition)

  def moveBy(pt: Point): Sprite =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Sprite =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Sprite =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Sprite =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Sprite =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Sprite =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Sprite =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Sprite =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Sprite =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Sprite =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withBindingKey(newBindingKey: BindingKey): Sprite =
    this.copy(bindingKey = newBindingKey)

  def flipHorizontal(isFlipped: Boolean): Sprite =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Sprite =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Sprite =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Sprite =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Sprite =
    withRef(Point(x, y))

  def withAnimationKey(newAnimationKey: AnimationKey): Sprite =
    this.copy(animationKey = newAnimationKey)

  def play(): Sprite =
    this.copy(animationActions = animationActions ++ List(Play))

  def changeCycle(label: CycleLabel): Sprite =
    this.copy(animationActions = animationActions ++ List(ChangeCycle(label)))

  def jumpToFirstFrame(): Sprite =
    this.copy(animationActions = animationActions ++ List(JumpToFirstFrame))

  def jumpToLastFrame(): Sprite =
    this.copy(animationActions = animationActions ++ List(JumpToLastFrame))

  def jumpToFrame(number: Int): Sprite =
    this.copy(animationActions = animationActions ++ List(JumpToFrame(number)))

  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Sprite =
    this.copy(eventHandler = e)

}

object Sprite {
  def apply(
      bindingKey: BindingKey,
      x: Int,
      y: Int,
      depth: Int,
      animationKey: AnimationKey,
      material: Material
  ): Sprite =
    Sprite(
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(depth),
      ref = Point.zero,
      flip = Flip.default,
      bindingKey = bindingKey,
      animationKey = animationKey,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil,
      animationActions = Nil,
      material = material
    )

  def apply(
      bindingKey: BindingKey,
      position: Point,
      depth: Depth,
      rotation: Radians,
      scale: Vector2,
      animationKey: AnimationKey,
      ref: Point,
      eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
      material: Material
  ): Sprite =
    Sprite(
      position = position,
      rotation = rotation,
      scale = scale,
      depth = depth,
      ref = ref,
      flip = Flip.default,
      bindingKey = bindingKey,
      animationKey = animationKey,
      eventHandler = eventHandler,
      animationActions = Nil,
      material = material
    )

  def apply(bindingKey: BindingKey, animationKey: AnimationKey, material: Material): Sprite =
    Sprite(
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(1),
      ref = Point.zero,
      flip = Flip.default,
      bindingKey = bindingKey,
      animationKey = animationKey,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil,
      animationActions = Nil,
      material = material
    )
}
