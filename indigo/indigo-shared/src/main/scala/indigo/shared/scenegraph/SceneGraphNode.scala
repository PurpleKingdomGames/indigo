package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.animation.AnimationAction._
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.CycleLabel
import indigo.shared.datatypes._
import indigo.shared.materials.StandardMaterial
import indigo.shared.materials.ShaderData
import indigo.shared.datatypes.mutable.CheapMatrix4

import indigo.shared.animation.AnimationAction
import indigo.shared.BoundaryLocator

/**
  * The parent type of anything that can affect the visual representation of the game.
  */
// sealed trait SceneNode extends Product with Serializable {
//   def position: Point
//   def rotation: Radians
//   def scale: Vector2
//   def depth: Depth
//   def ref: Point
//   def flip: Flip
// }
// object SceneNode {
//   def empty: Group = Group.empty
// }

sealed trait SceneNode extends Product with Serializable
object SceneNode {
  def empty: Group = Group.empty
}

sealed trait RenderNode extends SceneNode {
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip
  def ref: Point

  def withDepth(newDepth: Depth): RenderNode
}

/**
  * Can be extended to create custom scene elements.
  *
  * May be used in conjunction with `EventHandler` and `Cloneable`.
  */
trait EntityNode extends RenderNode {
  def bounds: Rectangle
  def toShaderData: ShaderData
}

//------------------
// Utility Nodes
//------------------

final case class Transformer(node: SceneNode, transform: CheapMatrix4) extends SceneNode {
  def addTransform(matrix: CheapMatrix4): Transformer =
    this.copy(transform = transform * matrix)
}

//------------------
// Dependent Nodes
//------------------

sealed trait DependentNode extends SceneNode {
  def position: Point
  def rotation: Radians
  def scale: Vector2
  def depth: Depth
  def flip: Flip

  def withDepth(newDepth: Depth): DependentNode
}

/**
  * A single cloned instance of a cloneblank
  *
  * @param id
  * @param depth
  * @param transform
  */
final case class Clone(id: CloneId, depth: Depth, transform: CloneTransformData) extends DependentNode with BasicSpatialModifiers[Clone] {
  lazy val x: Int                  = transform.position.x
  lazy val y: Int                  = transform.position.y
  lazy val rotation: Radians       = transform.rotation
  lazy val scale: Vector2          = transform.scale
  lazy val flipHorizontal: Boolean = transform.flipHorizontal
  lazy val flipVertical: Boolean   = transform.flipVertical

  def position: Point = Point(transform.position.x, transform.position.y)
  def flip: Flip      = Flip(transform.flipHorizontal, transform.flipVertical)

  def withCloneId(newCloneId: CloneId): Clone =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): Clone =
    this.copy(depth = newDepth)

  def withTransforms(newPosition: Point, newRotation: Radians, newScale: Vector2, flipHorizontal: Boolean, flipVertical: Boolean): Clone =
    this.copy(transform = CloneTransformData(newPosition, newRotation, newScale, flipHorizontal, flipVertical))

  def withPosition(newPosition: Point): Clone =
    this.copy(transform = transform.withPosition(newPosition))

  def withRotation(newRotation: Radians): Clone =
    this.copy(transform = transform.withRotation(newRotation))

  def withScale(newScale: Vector2): Clone =
    this.copy(transform = transform.withScale(newScale))

  def withHorizontalFlip(isFlipped: Boolean): Clone =
    this.copy(transform = transform.withHorizontalFlip(isFlipped))

  def withVerticalFlip(isFlipped: Boolean): Clone =
    this.copy(transform = transform.withVerticalFlip(isFlipped))

  def withFlip(newFlip: Flip): Clone =
    this.copy(
      transform = transform
        .withVerticalFlip(newFlip.vertical)
        .withHorizontalFlip(newFlip.horizontal)
    )
}
object Clone {
  def apply(id: CloneId): Clone =
    Clone(id, Depth(1), CloneTransformData.identity)
}

/**
  * Represents many clones of the same cloneblank, differentiated only by their transform data.
  *
  * @param id
  * @param depth
  * @param transform
  * @param clones
  * @param staticBatchKey
  */
final case class CloneBatch(id: CloneId, depth: Depth, transform: CloneTransformData, clones: List[CloneTransformData], staticBatchKey: Option[BindingKey])
    extends DependentNode
    with BasicSpatialModifiers[CloneBatch] {
  lazy val x: Int                  = transform.position.x
  lazy val y: Int                  = transform.position.y
  lazy val rotation: Radians       = transform.rotation
  lazy val scale: Vector2          = transform.scale
  lazy val flipHorizontal: Boolean = transform.flipHorizontal
  lazy val flipVertical: Boolean   = transform.flipVertical

  def position: Point = Point(transform.position.x, transform.position.y)
  def flip: Flip      = Flip(transform.flipHorizontal, transform.flipVertical)

  def withCloneId(newCloneId: CloneId): CloneBatch =
    this.copy(id = newCloneId)

  def withDepth(newDepth: Depth): CloneBatch =
    this.copy(depth = newDepth)

  def withTransforms(newPosition: Point, newRotation: Radians, newScale: Vector2, flipHorizontal: Boolean, flipVertical: Boolean): CloneBatch =
    this.copy(transform = CloneTransformData(newPosition, newRotation, newScale, flipHorizontal, flipVertical))

  def withPosition(newPosition: Point): CloneBatch =
    this.copy(transform = transform.withPosition(newPosition))

  def withRotation(newRotation: Radians): CloneBatch =
    this.copy(transform = transform.withRotation(newRotation))

  def withScale(newScale: Vector2): CloneBatch =
    this.copy(transform = transform.withScale(newScale))

  def withHorizontalFlip(isFlipped: Boolean): CloneBatch =
    this.copy(transform = transform.withHorizontalFlip(isFlipped))

  def withVerticalFlip(isFlipped: Boolean): CloneBatch =
    this.copy(transform = transform.withVerticalFlip(isFlipped))

  def withFlip(newFlip: Flip): CloneBatch =
    this.copy(
      transform = transform
        .withVerticalFlip(newFlip.vertical)
        .withHorizontalFlip(newFlip.horizontal)
    )

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

/**
  * Used to group elements to allow them to be manipulated as a collection.
  */
final case class Group(children: List[RenderNode], position: Point, rotation: Radians, scale: Vector2, depth: Depth, ref: Point, flip: Flip) extends CompositeNode with SpatialModifiers[Group] {

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withDepth(newDepth: Depth): Group =
    this.copy(depth = newDepth)

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

  def calculatedBounds(locator: BoundaryLocator): Rectangle = {
    def giveBounds(n: SceneNode): Rectangle =
      n match {
        case n: EntityNode =>
          n.bounds

        case n: CompositeNode =>
          n.calculatedBounds(locator)

        case _ =>
          Rectangle.zero
      }

    children match {
      case Nil =>
        Rectangle.zero

      case x :: xs =>
        xs.foldLeft(giveBounds(x)) { (acc, node) =>
          Rectangle.expandToInclude(acc, giveBounds(node))
        }
    }
  }

  def addChild(child: RenderNode): Group =
    this.copy(children = children ++ List(child))

  def addChildren(additionalChildren: List[RenderNode]): Group =
    this.copy(children = children ++ additionalChildren)

  def toMatrix: CheapMatrix4 =
    CheapMatrix4.identity
      .scale(
        if (flip.horizontal) -1.0 else 1.0,
        if (flip.vertical) -1.0 else 1.0,
        1.0d
      )
      .translate(
        -ref.x.toDouble,
        -ref.y.toDouble,
        0.0d
      )
      .scale(scale.x, scale.y, 1.0d)
      .rotate(rotation.value)
      .translate(
        position.x.toDouble,
        position.y.toDouble,
        0.0d
      )

  def toTransformers: List[Transformer] =
    toTransformers(CheapMatrix4.identity)
  def toTransformers(parentTransform: CheapMatrix4): List[Transformer] = {
    val mat = toMatrix * parentTransform // to avoid re-evaluation
    children.map { n =>
      Transformer(n.withDepth(n.depth + depth), mat)
    }
  }
}

object Group {

  def apply(children: RenderNode*): Group =
    Group(children.toList, Point.zero, Radians.zero, Vector2.one, Depth.Zero, Point.zero, Flip.default)

  def apply(children: List[RenderNode]): Group =
    Group(children, Point.zero, Radians.zero, Vector2.one, Depth.Zero, Point.zero, Flip.default)

  def empty: Group =
    apply(Nil)
}

//------------------
// Composite Nodes
//------------------

sealed trait CompositeNode extends RenderNode {
  def calculatedBounds(locator: BoundaryLocator): Rectangle
}

/**
  * Sprites are used to represented key-frame animated screen elements.
  */
final case class Sprite(
    bindingKey: BindingKey,
    material: StandardMaterial,
    animationKey: AnimationKey,
    animationActions: List[AnimationAction],
    eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends CompositeNode
    with EventHandler
    with Cloneable
    with SpatialModifiers[Sprite] {

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def calculatedBounds(locator: BoundaryLocator): Rectangle =
    locator.findBounds(this)

  def withDepth(newDepth: Depth): Sprite =
    this.copy(depth = newDepth)

  def withMaterial(newMaterial: StandardMaterial): Sprite =
    this.copy(material = newMaterial)

  def modifyMaterial(alter: StandardMaterial => StandardMaterial): Sprite =
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
  def apply(bindingKey: BindingKey, x: Int, y: Int, depth: Int, animationKey: AnimationKey, material: StandardMaterial): Sprite =
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
      material: StandardMaterial
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

  def apply(bindingKey: BindingKey, animationKey: AnimationKey, material: StandardMaterial): Sprite =
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

/**
  * Used to draw text onto the screen.
  */
final case class Text(
    text: String,
    alignment: TextAlignment,
    fontKey: FontKey,
    material: StandardMaterial,
    eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent],
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends CompositeNode
    with EventHandler
    with SpatialModifiers[Text] {

  def calculatedBounds(locator: BoundaryLocator): Rectangle =
    locator.findBounds(this)

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial(newMaterial: StandardMaterial): Text =
    this.copy(material = newMaterial)

  def modifyMaterial(alter: StandardMaterial => StandardMaterial): Text =
    this.copy(material = alter(material))

  def moveTo(pt: Point): Text =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Text =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Text =
    moveTo(newPosition)

  def moveBy(pt: Point): Text =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Text =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Text =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Text =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Text =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Text =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Text =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Text =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Text =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Text =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): Text =
    this.copy(depth = newDepth)

  def withRef(newRef: Point): Text =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Text =
    withRef(Point(x, y))

  def flipHorizontal(isFlipped: Boolean): Text =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Text =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Text =
    this.copy(flip = newFlip)

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

}

object Text {

  def apply(text: String, x: Int, y: Int, depth: Int, fontKey: FontKey, material: StandardMaterial): Text =
    Text(
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(depth),
      ref = Point.zero,
      flip = Flip.default,
      text = text,
      alignment = TextAlignment.Left,
      fontKey = fontKey,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil,
      material = material
    )

  def apply(text: String, fontKey: FontKey, material: StandardMaterial): Text =
    Text(
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth(1),
      ref = Point.zero,
      flip = Flip.default,
      text = text,
      alignment = TextAlignment.Left,
      fontKey = fontKey,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil,
      material = material
    )

}
