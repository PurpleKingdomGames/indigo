package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.animation.AnimationAction._
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.CycleLabel
import indigo.shared.datatypes._
import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.animation.AnimationAction
import indigo.shared.BoundaryLocator

object SceneGraphNode {
  def empty: Group = Group.empty
}

sealed trait SceneGraphNode extends Product with Serializable {
  val depth: Depth
  def x: Int
  def y: Int
  def rotation: Radians
  def scale: Vector2
}

sealed trait SceneGraphNodePrimitive extends SceneGraphNode {
  def bounds(locator: BoundaryLocator): Rectangle
  def withDepth(depth: Depth): SceneGraphNodePrimitive
  def moveTo(pt: Point): SceneGraphNodePrimitive
  def moveTo(x: Int, y: Int): SceneGraphNodePrimitive
  def moveBy(pt: Point): SceneGraphNodePrimitive
  def moveBy(x: Int, y: Int): SceneGraphNodePrimitive
}

final case class Group(positionOffset: Point, rotation: Radians, scale: Vector2, depth: Depth, children: List[SceneGraphNodePrimitive]) extends SceneGraphNodePrimitive {

  lazy val x: Int = positionOffset.x
  lazy val y: Int = positionOffset.y

  def withDepth(newDepth: Depth): Group =
    this.copy(depth = newDepth)

  def moveTo(pt: Point): Group =
    this.copy(positionOffset = pt)
  def moveTo(x: Int, y: Int): Group =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Group =
    moveTo(positionOffset + pt)
  def moveBy(x: Int, y: Int): Group =
    moveBy(Point(x, y))

  def bounds(locator: BoundaryLocator): Rectangle =
    children match {
      case Nil =>
        Rectangle.zero

      case x :: xs =>
        xs.foldLeft(x.bounds(locator)) { (acc, node) =>
          Rectangle.expandToInclude(acc, node.bounds(locator))
        }
    }

  def addChild(child: SceneGraphNodePrimitive): Group =
    this.copy(children = children :+ child)

  def addChildren(additionalChildren: List[SceneGraphNodePrimitive]): Group =
    this.copy(children = children ++ additionalChildren)

}

object Group {

  def apply(positionOffset: Point, rotation: Radians, scale: Vector2, depth: Depth, children: SceneGraphNodePrimitive*): Group =
    Group(positionOffset, rotation, scale, depth, children.toList)

  def apply(children: SceneGraphNodePrimitive*): Group =
    Group(Point.zero, Radians.zero, Vector2.one, Depth.Zero, children.toList)

  def apply(children: List[SceneGraphNodePrimitive]): Group =
    Group(Point.zero, Radians.zero, Vector2.one, Depth.Zero, children)

  def empty: Group =
    apply(Nil)
}

final case class CloneId(value: String) extends AnyVal
object CloneId {
  implicit val equalTo: EqualTo[CloneId] =
    EqualTo.create(_.value === _.value)
}

sealed trait Cloneable

final case class CloneBlank(id: CloneId, cloneable: Cloneable) {
  def withCloneId(newCloneId: CloneId): CloneBlank =
    this.copy(id = newCloneId)

  def withCloneable(newCloneable: Cloneable): CloneBlank =
    this.copy(cloneable = newCloneable)
}

final case class CloneTransformData(position: Point, rotation: Radians, scale: Vector2, alpha: Double, flipHorizontal: Boolean, flipVertical: Boolean) {
  def withPosition(newPosition: Point): CloneTransformData =
    this.copy(position = newPosition)

  def withRotation(newRotation: Radians): CloneTransformData =
    this.copy(rotation = newRotation)

  def withScale(newScale: Vector2): CloneTransformData =
    this.copy(scale = newScale)

  def withAlpha(newAlpha: Double): CloneTransformData =
    this.copy(alpha = newAlpha)

  def withHorizontalFlip(isFlipped: Boolean): CloneTransformData =
    this.copy(flipHorizontal = isFlipped)

  def withVerticalFlip(isFlipped: Boolean): CloneTransformData =
    this.copy(flipVertical = isFlipped)
}
object CloneTransformData {
  def startAt(position: Point): CloneTransformData =
    CloneTransformData(position, Radians.zero, Vector2.one, 1f, false, false)

  val identity: CloneTransformData =
    CloneTransformData(Point.zero, Radians.zero, Vector2.one, 1f, false, false)
}

final case class Clone(id: CloneId, depth: Depth, transform: CloneTransformData) extends SceneGraphNode {
  lazy val x: Int                  = transform.position.x
  lazy val y: Int                  = transform.position.y
  lazy val rotation: Radians       = transform.rotation
  lazy val scale: Vector2          = transform.scale
  lazy val alpha: Double           = transform.alpha
  lazy val flipHorizontal: Boolean = transform.flipHorizontal
  lazy val flipVertical: Boolean   = transform.flipVertical

  def withCloneId(newCloneId: CloneId): Clone =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): Clone =
    this.copy(depth = newDepth)

  def withTransforms(newPosition: Point, newRotation: Radians, newScale: Vector2, alpha: Double, flipHorizontal: Boolean, flipVertical: Boolean): Clone =
    this.copy(transform = CloneTransformData(newPosition, newRotation, newScale, alpha, flipHorizontal, flipVertical))

  def withPosition(newPosition: Point): Clone =
    this.copy(transform = transform.withPosition(newPosition))

  def withRotation(newRotation: Radians): Clone =
    this.copy(transform = transform.withRotation(newRotation))

  def withScale(newScale: Vector2): Clone =
    this.copy(transform = transform.withScale(newScale))

  def withAlpha(newAlpha: Double): Clone =
    this.copy(transform = transform.withAlpha(newAlpha))

  def withHorizontalFlip(isFlipped: Boolean): Clone =
    this.copy(transform = transform.withHorizontalFlip(isFlipped))

  def withVerticalFlip(isFlipped: Boolean): Clone =
    this.copy(transform = transform.withVerticalFlip(isFlipped))
}

final case class CloneBatch(id: CloneId, depth: Depth, transform: CloneTransformData, clones: List[CloneTransformData], staticBatchKey: Option[BindingKey]) extends SceneGraphNode {
  lazy val x: Int                  = transform.position.x
  lazy val y: Int                  = transform.position.y
  lazy val rotation: Radians       = transform.rotation
  lazy val scale: Vector2          = transform.scale
  lazy val alpha: Double           = transform.alpha
  lazy val flipHorizontal: Boolean = transform.flipHorizontal
  lazy val flipVertical: Boolean   = transform.flipVertical

  def withCloneId(newCloneId: CloneId): CloneBatch =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): CloneBatch =
    this.copy(depth = newDepth)

  def withTransforms(newPosition: Point, newRotation: Radians, newScale: Vector2, alpha: Double, flipHorizontal: Boolean, flipVertical: Boolean): CloneBatch =
    this.copy(transform = CloneTransformData(newPosition, newRotation, newScale, alpha, flipHorizontal, flipVertical))

  def withPosition(newPosition: Point): CloneBatch =
    this.copy(transform = transform.withPosition(newPosition))

  def withRotation(newRotation: Radians): CloneBatch =
    this.copy(transform = transform.withRotation(newRotation))

  def withScale(newScale: Vector2): CloneBatch =
    this.copy(transform = transform.withScale(newScale))

  def withAlpha(newAlpha: Double): CloneBatch =
    this.copy(transform = transform.withAlpha(newAlpha))

  def withHorizontalFlip(isFlipped: Boolean): CloneBatch =
    this.copy(transform = transform.withHorizontalFlip(isFlipped))

  def withVerticalFlip(isFlipped: Boolean): CloneBatch =
    this.copy(transform = transform.withVerticalFlip(isFlipped))

  def withClones(newClones: List[CloneTransformData]): CloneBatch =
    this.copy(clones = newClones)

  def addClones(additionalClones: List[CloneTransformData]): CloneBatch =
    this.copy(clones = clones ++ additionalClones)

  def withMaybeStaticBatchKey(maybeKey: Option[BindingKey]): CloneBatch =
    this.copy(staticBatchKey = maybeKey)

  def withStaticBatchKey(key: BindingKey): CloneBatch =
    withMaybeStaticBatchKey(Option(key))

  def clearStaticBatchKey: CloneBatch =
    withMaybeStaticBatchKey(None)
}

sealed trait Renderable extends SceneGraphNodePrimitive {
  def effects: Effects
  def ref: Point

  def withEffects(newEffects: Effects): Renderable
  def withTint(tint: RGBA): Renderable
  def withTint(red: Double, green: Double, blue: Double): Renderable
  def withOverlay(newOverlay: Overlay): Renderable
  def withBorder(newBorder: Border): Renderable
  def withGlow(newGlow: Glow): Renderable
  def withAlpha(a: Double): Renderable
  def flipHorizontal(h: Boolean): Renderable
  def flipVertical(v: Boolean): Renderable

  override def withDepth(depth: Depth): Renderable
  override def moveTo(pt: Point): Renderable
  override def moveTo(x: Int, y: Int): Renderable
  override def moveBy(pt: Point): Renderable
  override def moveBy(x: Int, y: Int): Renderable
  def rotate(angle: Radians): Renderable
  def rotateBy(angle: Radians): Renderable
  def scaleBy(amount: Vector2): Renderable
  def scaleBy(x: Double, y: Double): Renderable
  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): SceneGraphNodePrimitive
  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): SceneGraphNodePrimitive

}

sealed trait EventHandling {
  def eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Renderable
}

final case class Graphic(
    position: Point,
    depth: Depth,
    rotation: Radians,
    scale: Vector2,
    ref: Point,
    crop: Rectangle,
    effects: Effects,
    material: Material
) extends Renderable
    with Cloneable {

  def bounds(locator: BoundaryLocator): Rectangle =
    Rectangle(position, crop.size)

  lazy val lazyBounds: Rectangle =
    Rectangle(position, crop.size)

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial(newMaterial: Material): Graphic =
    this.copy(material = newMaterial)

  def moveTo(pt: Point): Graphic =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Graphic =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Graphic =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Graphic =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Graphic =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Graphic =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Graphic =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Graphic =
    scaleBy(Vector2(x, y))

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Graphic =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Graphic =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): Graphic =
    this.copy(depth = newDepth)

  def withAlpha(newAlpha: Double): Graphic =
    this.copy(effects = effects.withAlpha(newAlpha))

  def withTint(newTint: RGBA): Graphic =
    this.copy(effects = effects.withTint(newTint))

  def withTint(red: Double, green: Double, blue: Double): Graphic =
    this.copy(effects = effects.withTint(RGBA(red, green, blue, 1)))

  def withTint(red: Double, green: Double, blue: Double, amount: Double): Graphic =
    this.copy(effects = effects.withTint(RGBA(red, green, blue, amount)))

  def withOverlay(newOverlay: Overlay): Graphic =
    this.copy(effects = effects.withOverlay(newOverlay))

  def withBorder(newBorder: Border): Graphic =
    this.copy(effects = effects.withBorder(newBorder))

  def withGlow(newGlow: Glow): Graphic =
    this.copy(effects = effects.withGlow(newGlow))

  def flipHorizontal(isFlipped: Boolean): Graphic =
    this.copy(effects = effects.withHorizontalFlip(isFlipped))

  def flipVertical(isFlipped: Boolean): Graphic =
    this.copy(effects = effects.withVerticalFlip(isFlipped))

  def withRef(newRef: Point): Graphic =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Graphic =
    withRef(Point(x, y))

  def withCrop(newCrop: Rectangle): Graphic =
    this.copy(crop = newCrop)
  def withCrop(x: Int, y: Int, width: Int, height: Int): Graphic =
    withCrop(Rectangle(x, y, width, height))

  def withEffects(newEffects: Effects): Graphic =
    this.copy(effects = newEffects)
}

object Graphic {

  def apply(x: Int, y: Int, width: Int, height: Int, depth: Int, material: Material): Graphic =
    Graphic(
      position = Point(x, y),
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      crop = Rectangle(0, 0, width, height),
      effects = Effects.default,
      material = material
    )

  def apply(bounds: Rectangle, depth: Int, material: Material): Graphic =
    Graphic(
      position = bounds.position,
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      crop = bounds,
      effects = Effects.default,
      material = material
    )
}

final case class Sprite(
    bindingKey: BindingKey,
    position: Point,
    depth: Depth,
    rotation: Radians,
    scale: Vector2,
    animationKey: AnimationKey,
    ref: Point,
    effects: Effects,
    eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
    animationActions: List[AnimationAction]
) extends Renderable
    with EventHandling
    with Cloneable {

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def bounds(locator: BoundaryLocator): Rectangle =
    locator.findBounds(this)

  def withDepth(newDepth: Depth): Sprite =
    this.copy(depth = newDepth)

  def moveTo(pt: Point): Sprite =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Sprite =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Sprite =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Sprite =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Sprite =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Sprite =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Sprite =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Sprite =
    scaleBy(Vector2(x, y))

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Sprite =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Sprite =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withBindingKey(newBindingKey: BindingKey): Sprite =
    this.copy(bindingKey = newBindingKey)

  def withAlpha(newAlpha: Double): Sprite =
    this.copy(effects = effects.withAlpha(newAlpha))

  def withTint(newTint: RGBA): Sprite =
    this.copy(effects = effects.withTint(newTint))

  def withTint(red: Double, green: Double, blue: Double): Sprite =
    withTint(RGBA(red, green, blue, 1))

  def withTint(red: Double, green: Double, blue: Double, amount: Double): Sprite =
    withTint(RGBA(red, green, blue, amount))

  def withOverlay(newOverlay: Overlay): Sprite =
    this.copy(effects = effects.withOverlay(newOverlay))

  def withBorder(newBorder: Border): Sprite =
    this.copy(effects = effects.withBorder(newBorder))

  def withGlow(newGlow: Glow): Sprite =
    this.copy(effects = effects.withGlow(newGlow))

  def flipHorizontal(isFlipped: Boolean): Sprite =
    this.copy(effects = effects.withHorizontalFlip(isFlipped))

  def flipVertical(isFlipped: Boolean): Sprite =
    this.copy(effects = effects.withVerticalFlip(isFlipped))

  def withRef(newRef: Point): Sprite =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Sprite =
    withRef(Point(x, y))

  def withAnimationKey(newAnimationKey: AnimationKey): Sprite =
    this.copy(animationKey = newAnimationKey)

  def play(): Sprite =
    this.copy(animationActions = animationActions :+ Play)

  def changeCycle(label: CycleLabel): Sprite =
    this.copy(animationActions = animationActions :+ ChangeCycle(label))

  def jumpToFirstFrame(): Sprite =
    this.copy(animationActions = animationActions :+ JumpToFirstFrame)

  def jumpToLastFrame(): Sprite =
    this.copy(animationActions = animationActions :+ JumpToLastFrame)

  def jumpToFrame(number: Int): Sprite =
    this.copy(animationActions = animationActions :+ JumpToFrame(number))

  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Sprite =
    this.copy(eventHandler = e)

  def withEffects(newEffects: Effects): Sprite =
    this.copy(effects = newEffects)

}

object Sprite {
  def apply(bindingKey: BindingKey, x: Int, y: Int, depth: Int, animationKey: AnimationKey): Sprite =
    Sprite(
      bindingKey = bindingKey,
      position = Point(x, y),
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      animationKey = animationKey,
      ref = Point.zero,
      effects = Effects.default,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil
    )

  def apply(
      bindingKey: BindingKey,
      position: Point,
      depth: Depth,
      rotation: Radians,
      scale: Vector2,
      animationKey: AnimationKey,
      ref: Point,
      effects: Effects,
      eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
  ): Sprite =
    Sprite(bindingKey, position, depth, rotation, scale, animationKey, ref, effects, eventHandler, Nil)

}

final case class Text(
    text: String,
    alignment: TextAlignment,
    position: Point,
    depth: Depth,
    rotation: Radians,
    scale: Vector2,
    fontKey: FontKey,
    effects: Effects,
    eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
) extends Renderable
    with EventHandling {

  val ref: Point = Point.zero

  def bounds(locator: BoundaryLocator): Rectangle =
    locator.findBounds(this)

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def moveTo(pt: Point): Text =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Text =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Text =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Text =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Text =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Text =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Text =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Text =
    scaleBy(Vector2(x, y))

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Text =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Text =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): Text =
    this.copy(depth = newDepth)

  def withAlpha(newAlpha: Double): Text =
    this.copy(effects = effects.withAlpha(newAlpha))

  def withTint(newTint: RGBA): Text =
    this.copy(effects = effects.withTint(newTint))

  def withTint(red: Double, green: Double, blue: Double): Text =
    this.copy(effects = effects.withTint(RGBA(red, green, blue, 1)))

  def withTint(red: Double, green: Double, blue: Double, amount: Double): Text =
    this.copy(effects = effects.withTint(RGBA(red, green, blue, amount)))

  def withOverlay(newOverlay: Overlay): Text =
    this.copy(effects = effects.withOverlay(newOverlay))

  def withBorder(newBorder: Border): Text =
    this.copy(effects = effects.withBorder(newBorder))

  def withGlow(newGlow: Glow): Text =
    this.copy(effects = effects.withGlow(newGlow))

  def flipHorizontal(isFlipped: Boolean): Text =
    this.copy(effects = effects.withHorizontalFlip(isFlipped))

  def flipVertical(isFlipped: Boolean): Text =
    this.copy(effects = effects.withVerticalFlip(isFlipped))

  def withAlignment(newAlignment: TextAlignment): Text =
    this.copy(alignment = newAlignment)

  def alignLeft: Text =
    this.copy(alignment = TextAlignment.Left)
  def alignCenter: Text =
    this.copy(alignment = TextAlignment.Center)
  def alignRight: Text =
    this.copy(alignment = TextAlignment.Right)

  def withText(newText: String): Text =
    this.copy(text = newText)

  def withFontKey(newFontKey: FontKey): Text =
    this.copy(fontKey = newFontKey)

  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Text =
    this.copy(eventHandler = e)

  def withEffects(newEffects: Effects): Text =
    this.copy(effects = newEffects)

}

final case class TextLine(text: String, lineBounds: Rectangle) {
  def moveTo(x: Int, y: Int): TextLine =
    moveTo(Point(x, y))
  def moveTo(newPosition: Point): TextLine =
    this.copy(lineBounds = lineBounds.moveTo(newPosition))

  def hash: String = text + lineBounds.hash
}

object Text {

  def apply(text: String, x: Int, y: Int, depth: Int, fontKey: FontKey): Text =
    Text(
      text = text,
      alignment = TextAlignment.Left,
      position = Point(x, y),
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      fontKey = fontKey,
      effects = Effects.default,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil
    )

}
