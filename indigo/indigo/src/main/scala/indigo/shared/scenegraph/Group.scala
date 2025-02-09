package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.*
import indigo.shared.events.GlobalEvent

/** Used to group elements to allow them to be manipulated as a collection.
  */
final case class Group(
    children: Batch[SceneNode],
    eventHandlerEnabled: Boolean,
    eventHandler: ((Group, GlobalEvent)) => Option[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    flip: Flip
) extends DependentNode[Group]
    with SpatialModifiers[Group] derives CanEqual:

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withRef(newRef: Point): Group =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Group =
    withRef(Point(x, y))

  def moveTo(pt: Point): Group =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Group =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Group =
    moveTo(newPosition)

  def moveBy(pt: Point): Group =
    moveTo(position + pt)
  def moveBy(x: Int, y: Int): Group =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Group =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Group =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Group =
    rotateTo(newRotation)

  def scaleBy(x: Double, y: Double): Group =
    scaleBy(Vector2(x, y))
  def scaleBy(amount: Vector2): Group =
    this.copy(scale = scale * amount)
  def withScale(newScale: Vector2): Group =
    this.copy(scale = newScale)

  def flipHorizontal(isFlipped: Boolean): Group =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Group =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Group =
    this.copy(flip = newFlip)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Group =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Group =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def addChild(child: SceneNode): Group =
    this.copy(children = children ++ Batch(child))

  def addChildren(additionalChildren: Batch[SceneNode]): Group =
    this.copy(children = children ++ additionalChildren)

  def withEventHandler(f: ((Group, GlobalEvent)) => Option[GlobalEvent]): Group =
    this.copy(eventHandler = f, eventHandlerEnabled = true)
  def onEvent(f: PartialFunction[((Group, GlobalEvent)), GlobalEvent]): Group =
    withEventHandler(f.lift)
  def enableEvents: Group =
    this.copy(eventHandlerEnabled = true)
  def disableEvents: Group =
    this.copy(eventHandlerEnabled = false)

object Group:

  import Batch.*

  def apply(children: SceneNode*): Group =
    Group(
      children.toBatch,
      false,
      Function.const(None),
      Point.zero,
      Radians.zero,
      Vector2.one,
      Point.zero,
      Flip.default
    )

  def apply(children: Batch[SceneNode]): Group =
    Group(
      children,
      false,
      Function.const(None),
      Point.zero,
      Radians.zero,
      Vector2.one,
      Point.zero,
      Flip.default
    )

  def empty: Group =
    apply(Batch.empty)
