package indigo.shared.scenegraph

import indigo.shared.scenegraph.syntax.BasicSpatial
import indigo.shared.scenegraph.syntax.Spatial

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
    with Cloneable derives CanEqual {

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
    locator.spriteBounds(this).map(rect => BoundaryLocator.findBounds(this, rect.position, rect.size, ref))

  def withDepth(newDepth: Depth): Sprite =
    this.copy(depth = newDepth)

  def withMaterial(newMaterial: Material): Sprite =
    this.copy(material = newMaterial)

  def modifyMaterial(alter: Material => Material): Sprite =
    this.copy(material = alter(material))

  def withBindingKey(newBindingKey: BindingKey): Sprite =
    this.copy(bindingKey = newBindingKey)

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

  given BasicSpatial[Sprite] with
    extension (sprite: Sprite)
      def position: Point =
        sprite.position
      def rotation: Radians =
        sprite.rotation
      def scale: Vector2 =
        sprite.scale
      def depth: Depth =
        sprite.depth
      def ref: Point =
        sprite.ref
      def flip: Flip =
        sprite.flip

      def withPosition(newPosition: Point): Sprite =
        sprite.copy(position = newPosition)
      def withRotation(newRotation: Radians): Sprite =
        sprite.copy(rotation = newRotation)
      def withScale(newScale: Vector2): Sprite =
        sprite.copy(scale = newScale)
      def withRef(newRef: Point): Sprite =
        sprite.copy(ref = newRef)
      def withRef(x: Int, y: Int): Sprite =
        withRef(Point(x, y))
      def withDepth(newDepth: Depth): Sprite =
        sprite.copy(depth = newDepth)
      def withFlip(newFlip: Flip): Sprite =
        sprite.copy(flip = newFlip)

  given Spatial[Sprite] = Spatial.default[Sprite]
}
