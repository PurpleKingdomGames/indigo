package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.animation.AnimationAction._
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.CycleLabel
import indigo.shared.datatypes._
import indigo.shared.IndigoLogger

import indigo.shared.{AnimationsRegister, FontRegister}
import indigo.shared.QuickCache
import indigo.shared.EqualTo
import indigo.shared.EqualTo._

object SceneGraphNode {
  def empty: Group = Group.empty
}

sealed trait SceneGraphNode {
  val depth: Depth
  def x: Int
  def y: Int
  def rotation: Radians
  def scale: Vector2
}

sealed trait SceneGraphNodePrimitive extends SceneGraphNode {
  def bounds: Rectangle
  def withDepth(depth: Depth): SceneGraphNodePrimitive
  def moveTo(pt: Point): SceneGraphNodePrimitive
  def moveTo(x: Int, y: Int): SceneGraphNodePrimitive
  def moveBy(pt: Point): SceneGraphNodePrimitive
  def moveBy(x: Int, y: Int): SceneGraphNodePrimitive
  def rotate(angle: Radians): SceneGraphNodePrimitive
  def rotateBy(angle: Radians): SceneGraphNodePrimitive
  def scaleBy(amount: Vector2): SceneGraphNodePrimitive
  def scaleBy(x: Double, y: Double): SceneGraphNodePrimitive
  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): SceneGraphNodePrimitive
  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): SceneGraphNodePrimitive
}

final class Group(val positionOffset: Point, val rotation: Radians, val scale: Vector2, val depth: Depth, val children: List[SceneGraphNodePrimitive]) extends SceneGraphNodePrimitive {

  lazy val x: Int = positionOffset.x
  lazy val y: Int = positionOffset.y

  def withDepth(newDepth: Depth): Group =
    Group(positionOffset, rotation, scale, newDepth, children)

  def moveTo(pt: Point): Group =
    Group(pt, rotation, scale, depth, children)
  def moveTo(x: Int, y: Int): Group =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Group =
    moveTo(positionOffset + pt)
  def moveBy(x: Int, y: Int): Group =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Group =
    Group(positionOffset, angle, scale, depth, children)
  def rotateBy(angle: Radians): Group =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Group =
    Group(positionOffset, rotation, scale + amount, depth, children)
  def scaleBy(x: Double, y: Double): Group =
    scaleBy(Vector2(x, y))

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): SceneGraphNodePrimitive =
    Group(newPosition, newRotation, newScale, depth, children)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): SceneGraphNodePrimitive =
    Group(positionOffset + positionDiff, rotation + rotationDiff, scale + scaleDiff, depth, children)

  def bounds: Rectangle =
    children match {
      case Nil =>
        Rectangle.zero

      case x :: xs =>
        xs.foldLeft(x.bounds) { (acc, node) =>
          Rectangle.expandToInclude(acc, node.bounds)
        }
    }

  def addChild(child: SceneGraphNodePrimitive): Group =
    Group(positionOffset, rotation, scale, depth, children :+ child)

  def addChildren(additionalChildren: List[SceneGraphNodePrimitive]): Group =
    Group(positionOffset, rotation, scale, depth, children ++ additionalChildren)

}

object Group {

  def apply(positionOffset: Point, rotation: Radians, scale: Vector2, depth: Depth, children: List[SceneGraphNodePrimitive]): Group =
    new Group(positionOffset, rotation, scale, depth, children.toList)

  def apply(position: Point, rotation: Radians, scale: Vector2, depth: Depth, children: SceneGraphNodePrimitive*): Group =
    Group(position, rotation, scale, depth, children.toList)

  def apply(children: SceneGraphNodePrimitive*): Group =
    Group(Point.zero, Radians.zero, Vector2.one, Depth.Base, children.toList)

  def apply(children: List[SceneGraphNodePrimitive]): Group =
    Group(Point.zero, Radians.zero, Vector2.one, Depth.Base, children)

  def empty: Group =
    apply(Nil)
}

final class CloneId(val value: String) extends AnyVal
object CloneId {
  def apply(id: String): CloneId =
    new CloneId(id)

  implicit val equalTo: EqualTo[CloneId] =
    EqualTo.create(_.value === _.value)
}

sealed trait Cloneable

final class CloneBlank(val id: CloneId, val cloneable: Cloneable)
object CloneBlank {
  def apply(id: CloneId, cloneable: Cloneable): CloneBlank =
    new CloneBlank(id, cloneable)

  def unapply(c: CloneBlank): Option[(CloneId, Cloneable)] =
    Some((c.id, c.cloneable))
}

final class CloneTransformData(val position: Point, val rotation: Radians, val scale: Vector2)
object CloneTransformData {
  def apply(position: Point, rotation: Radians, scale: Vector2): CloneTransformData =
    new CloneTransformData(position, rotation, scale)

  def startAt(position: Point): CloneTransformData =
    new CloneTransformData(position, Radians.zero, Vector2.one)

  val identity: CloneTransformData =
    CloneTransformData(Point.zero, Radians.zero, Vector2.one)
}

final class Clone(val id: CloneId, val depth: Depth, val transform: CloneTransformData) extends SceneGraphNode {
  lazy val x: Int            = transform.position.x
  lazy val y: Int            = transform.position.y
  lazy val rotation: Radians = transform.rotation
  lazy val scale: Vector2    = transform.scale

  def withTransforms(newPosition: Point, newRotation: Radians, newScale: Vector2): Clone =
    new Clone(id, depth, CloneTransformData(newPosition, newRotation, newScale))

  def withPosition(newPosition: Point): Clone =
    new Clone(id, depth, CloneTransformData(newPosition, transform.rotation, transform.scale))

  def withRotation(newRotation: Radians): Clone =
    new Clone(id, depth, CloneTransformData(transform.position, newRotation, transform.scale))

  def withScale(newScale: Vector2): Clone =
    new Clone(id, depth, CloneTransformData(transform.position, transform.rotation, newScale))
}
object Clone {
  def apply(id: CloneId, depth: Depth, transform: CloneTransformData): Clone =
    new Clone(id, depth, transform)
}

final class CloneBatch(val id: CloneId, val depth: Depth, val transform: CloneTransformData, val clones: List[CloneTransformData], val staticBatchId: Option[BindingKey]) extends SceneGraphNode {
  lazy val x: Int            = transform.position.x
  lazy val y: Int            = transform.position.y
  lazy val rotation: Radians = transform.rotation
  lazy val scale: Vector2    = transform.scale
}
object CloneBatch {
  def apply(id: CloneId, depth: Depth, transform: CloneTransformData, clones: List[CloneTransformData], staticBatchId: Option[BindingKey]): CloneBatch =
    new CloneBatch(id, depth, transform, clones, staticBatchId)
}

sealed trait Renderable extends SceneGraphNodePrimitive {
  def effects: Effects
  def ref: Point

  def withAlpha(a: Double): Renderable
  def withTint(tint: Tint): Renderable
  def withTint(red: Double, green: Double, blue: Double): Renderable

  def flipHorizontal(h: Boolean): Renderable
  def flipVertical(v: Boolean): Renderable

  override def withDepth(depth: Depth): Renderable
  override def moveTo(pt: Point): Renderable
  override def moveTo(x: Int, y: Int): Renderable
  override def moveBy(pt: Point): Renderable
  override def moveBy(x: Int, y: Int): Renderable
  override def rotate(angle: Radians): Renderable
  override def rotateBy(angle: Radians): Renderable
  override def scaleBy(amount: Vector2): Renderable
  override def scaleBy(x: Double, y: Double): Renderable

}

sealed trait EventHandling {
  def eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Renderable
}

final class Graphic(
    val bounds: Rectangle,
    val depth: Depth,
    val rotation: Radians,
    val scale: Vector2,
    val ref: Point,
    val crop: Rectangle,
    val effects: Effects,
    val material: Material
) extends Renderable
    with Cloneable {

  lazy val x: Int = bounds.position.x
  lazy val y: Int = bounds.position.y

  def withMaterial(newMaterial: Material): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects, newMaterial)

  def moveTo(pt: Point): Graphic =
    Graphic(bounds.moveTo(pt), depth, rotation, scale, ref, crop, effects, material)
  def moveTo(x: Int, y: Int): Graphic =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Graphic =
    Graphic(bounds.moveTo(bounds.position + pt), depth, rotation, scale, ref, crop, effects, material)
  def moveBy(x: Int, y: Int): Graphic =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Graphic =
    Graphic(bounds, depth, angle, scale, ref, crop, effects, material)
  def rotateBy(angle: Radians): Graphic =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Graphic =
    Graphic(bounds, depth, rotation, scale * amount, ref, crop, effects, material)
  def scaleBy(x: Double, y: Double): Graphic =
    scaleBy(Vector2(x, y))

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): SceneGraphNodePrimitive =
    Graphic(bounds.moveTo(newPosition), depth, newRotation, newScale, ref, crop, effects, material)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): SceneGraphNodePrimitive =
    Graphic(bounds.moveTo(this.bounds.position + positionDiff), depth, rotation + rotationDiff, scale * scaleDiff, ref, crop, effects, material)

  def withDepth(depthValue: Depth): Graphic =
    Graphic(bounds, depthValue, rotation, scale, ref, crop, effects, material)

  def withAlpha(a: Double): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects.withAlpha(a), material)

  def withTint(tint: Tint): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects.withTint(tint), material)

  def withTint(red: Double, green: Double, blue: Double): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects.withTint(Tint(red, green, blue, 1)), material)

  def withTint(red: Double, green: Double, blue: Double, amount: Double): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects.withTint(Tint(red, green, blue, amount)), material)

  def flipHorizontal(hValue: Boolean): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects.withFlip(Flip(hValue, effects.flip.vertical)), material)

  def flipVertical(vValue: Boolean): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects.withFlip(Flip(effects.flip.horizontal, vValue)), material)

  def withRef(refValue: Point): Graphic =
    Graphic(bounds, depth, rotation, scale, refValue, crop, effects, material)
  def withRef(xValue: Int, yValue: Int): Graphic =
    withRef(Point(xValue, yValue))

  def withCrop(crop: Rectangle): Graphic =
    Graphic(bounds, depth, rotation, scale, ref, crop, effects, material)
  def withCrop(xValue: Int, yValue: Int, widthValue: Int, heightValue: Int): Graphic =
    withCrop(Rectangle(xValue, yValue, widthValue, heightValue))

}

object Graphic {

  def apply(
      bounds: Rectangle,
      depth: Depth,
      rotation: Radians,
      scale: Vector2,
      ref: Point,
      crop: Rectangle,
      effects: Effects,
      material: Material
  ): Graphic =
    new Graphic(
      bounds,
      depth,
      rotation,
      scale,
      ref,
      crop,
      effects,
      material
    )

  def apply(x: Int, y: Int, width: Int, height: Int, depth: Int, material: Material): Graphic =
    Graphic(
      bounds = Rectangle(x, y, width, height),
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
      bounds = bounds,
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      ref = Point.zero,
      crop = bounds,
      effects = Effects.default,
      material = material
    )
}

final class Sprite(
    val bindingKey: BindingKey,
    val bounds: Rectangle,
    val depth: Depth,
    val rotation: Radians,
    val scale: Vector2,
    val animationsKey: AnimationKey,
    val ref: Point,
    val effects: Effects,
    val eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
) extends Renderable
    with EventHandling
    with Cloneable {

  lazy val x: Int = bounds.position.x
  lazy val y: Int = bounds.position.y

  def withDepth(newDepth: Depth): Sprite =
    Sprite(bindingKey, bounds, newDepth, rotation, scale, animationsKey, ref, effects, eventHandler)

  def moveTo(pt: Point): Sprite =
    Sprite(bindingKey, bounds.moveTo(pt), depth, rotation, scale, animationsKey, ref, effects, eventHandler)
  def moveTo(x: Int, y: Int): Sprite =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Sprite =
    Sprite(bindingKey, bounds.moveTo(this.bounds.position + pt), depth, rotation, scale, animationsKey, ref, effects, eventHandler)
  def moveBy(x: Int, y: Int): Sprite =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Sprite =
    Sprite(bindingKey, bounds, depth, angle, scale, animationsKey, ref, effects, eventHandler)
  def rotateBy(angle: Radians): Sprite =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale * amount, animationsKey, ref, effects, eventHandler)
  def scaleBy(x: Double, y: Double): Sprite =
    scaleBy(Vector2(x, y))

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): SceneGraphNodePrimitive =
    Sprite(bindingKey, bounds.moveTo(newPosition), depth, newRotation, newScale, animationsKey, ref, effects, eventHandler)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): SceneGraphNodePrimitive =
    Sprite(bindingKey, bounds.moveTo(this.bounds.position + positionDiff), depth, rotation + rotationDiff, scale * scaleDiff, animationsKey, ref, effects, eventHandler)

  def withBindingKey(newBindingKey: BindingKey): Sprite =
    Sprite(newBindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects, eventHandler)

  def withAlpha(a: Double): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withAlpha(a), eventHandler)

  def withTint(tint: Tint): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withTint(tint), eventHandler)

  def withTint(red: Double, green: Double, blue: Double): Sprite =
    withTint(Tint(red, green, blue, 1))

  def withTint(red: Double, green: Double, blue: Double, amount: Double): Sprite =
    withTint(Tint(red, green, blue, amount))

  def flipHorizontal(h: Boolean): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withFlip(Flip(horizontal = h, vertical = effects.flip.vertical)), eventHandler)

  def flipVertical(v: Boolean): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withFlip(Flip(horizontal = effects.flip.horizontal, vertical = v)), eventHandler)

  def withRef(newRef: Point): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, newRef, effects, eventHandler)
  def withRef(x: Int, y: Int): Sprite =
    withRef(Point(x, y))

  def withAnimationKey(newAnimationKey: AnimationKey): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, newAnimationKey, ref, effects, eventHandler)

  def play(): Sprite = {
    AnimationsRegister.addAction(bindingKey, animationsKey, Play)
    this
  }

  def changeCycle(label: CycleLabel): Sprite = {
    AnimationsRegister.addAction(bindingKey, animationsKey, ChangeCycle(label))
    this
  }

  def jumpToFirstFrame(): Sprite = {
    AnimationsRegister.addAction(bindingKey, animationsKey, JumpToFirstFrame)
    this
  }

  def jumpToLastFrame(): Sprite = {
    AnimationsRegister.addAction(bindingKey, animationsKey, JumpToLastFrame)
    this
  }

  def jumpToFrame(number: Int): Sprite = {
    AnimationsRegister.addAction(bindingKey, animationsKey, JumpToFrame(number))
    this
  }

  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects, e)

}

object Sprite {
  def apply(bindingKey: BindingKey, x: Int, y: Int, width: Int, height: Int, depth: Int, animationsKey: AnimationKey): Sprite =
    Sprite(
      bindingKey = bindingKey,
      bounds = Rectangle(x, y, width, height),
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      animationsKey = animationsKey,
      ref = Point.zero,
      effects = Effects.default,
      eventHandler = (_: (Rectangle, GlobalEvent)) => Nil
    )

  def apply(
      bindingKey: BindingKey,
      bounds: Rectangle,
      depth: Depth,
      rotation: Radians,
      scale: Vector2,
      animationsKey: AnimationKey,
      ref: Point,
      effects: Effects,
      eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
  ): Sprite =
    new Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects, eventHandler)

}

final class Text(
    val text: String,
    val alignment: TextAlignment,
    val position: Point,
    val depth: Depth,
    val rotation: Radians,
    val scale: Vector2,
    val fontKey: FontKey,
    val effects: Effects,
    val eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
) extends Renderable
    with EventHandling {

  val ref: Point = Point.zero

  lazy val lines: List[TextLine] =
    FontRegister
      .findByFontKey(fontKey)
      .map { fontInfo =>
        text
          .split('\n')
          .toList
          .map(_.replace("\n", ""))
          .map(line => new TextLine(line, Text.calculateBoundsOfLine(line, fontInfo)))
      }
      .getOrElse {
        IndigoLogger.errorOnce(s"Cannot build Text lines, missing Font with key: ${fontKey.toString()}")
        Nil
      }

  lazy val unalignedBounds: Rectangle =
    lines
      .map(_.lineBounds)
      .fold(Rectangle.zero) { (acc, next) =>
        acc.resize(Point(Math.max(acc.width, next.width), acc.height + next.height))
      }
      .moveTo(position)

  lazy val bounds: Rectangle =
    (alignment, unalignedBounds) match {
      case (TextAlignment.Left, b)   => b
      case (TextAlignment.Center, b) => b.moveTo(Point(b.x - (b.width / 2), b.y))
      case (TextAlignment.Right, b)  => b.moveTo(Point(b.x - b.width, b.y))
    }

  lazy val x: Int = bounds.position.x
  lazy val y: Int = bounds.position.y

  def moveTo(pt: Point): Text =
    Text(text, alignment, pt, depth, rotation, scale, fontKey, effects, eventHandler)
  def moveTo(x: Int, y: Int): Text =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Text =
    Text(text, alignment, position + pt, depth, rotation, scale, fontKey, effects, eventHandler)
  def moveBy(x: Int, y: Int): Text =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Text =
    Text(text, alignment, position, depth, angle, scale, fontKey, effects, eventHandler)
  def rotateBy(angle: Radians): Text =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Text =
    Text(text, alignment, position, depth, rotation, scale * amount, fontKey, effects, eventHandler)
  def scaleBy(x: Double, y: Double): Text =
    scaleBy(Vector2(x, y))

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): SceneGraphNodePrimitive =
    Text(text, alignment, newPosition, depth, newRotation, newScale, fontKey, effects, eventHandler)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): SceneGraphNodePrimitive =
    Text(text, alignment, position + positionDiff, depth, rotation + rotationDiff, scale * scaleDiff, fontKey, effects, eventHandler)

  def withDepth(newDepth: Depth): Text =
    Text(text, alignment, position, newDepth, rotation, scale, fontKey, effects, eventHandler)

  def withAlpha(a: Double): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withAlpha(a), eventHandler)

  def withTint(tint: Tint): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withTint(tint), eventHandler)

  def withTint(red: Double, green: Double, blue: Double): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withTint(Tint(red, green, blue, 1)), eventHandler)

  def withTint(red: Double, green: Double, blue: Double, amount: Double): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withTint(Tint(red, green, blue, amount)), eventHandler)

  def flipHorizontal(h: Boolean): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withFlip(Flip(horizontal = h, vertical = effects.flip.vertical)), eventHandler)

  def flipVertical(v: Boolean): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withFlip(Flip(horizontal = effects.flip.horizontal, vertical = v)), eventHandler)

  def withAlignment(newAlignment: TextAlignment): Text =
    Text(text, newAlignment, position, depth, rotation, scale, fontKey, effects, eventHandler)

  def alignLeft: Text =
    Text(text, TextAlignment.Left, position, depth, rotation, scale, fontKey, effects, eventHandler)
  def alignCenter: Text =
    Text(text, TextAlignment.Center, position, depth, rotation, scale, fontKey, effects, eventHandler)
  def alignRight: Text =
    Text(text, TextAlignment.Right, position, depth, rotation, scale, fontKey, effects, eventHandler)

  def withText(newText: String): Text =
    Text(newText, alignment, position, depth, rotation, scale, fontKey, effects, eventHandler)

  def withFontKey(newFontKey: FontKey): Text =
    Text(text, alignment, position, depth, rotation, scale, newFontKey, effects, eventHandler)

  def onEvent(e: ((Rectangle, GlobalEvent)) => List[GlobalEvent]): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects, e)

}

final class TextLine(val text: String, val lineBounds: Rectangle) {
  def hash: String = text + lineBounds.hash
}

object Text {

  implicit val lineBoundsCache: QuickCache[Rectangle] = QuickCache.empty

  def calculateBoundsOfLine(lineText: String, fontInfo: FontInfo): Rectangle =
    QuickCache("line-bounds-" + fontInfo.fontKey.key + "-" + lineText) {
      lineText.toList
        .map(c => fontInfo.findByCharacter(c).bounds)
        .fold(Rectangle.zero)((acc, curr) => Rectangle(0, 0, acc.width + curr.width, Math.max(acc.height, curr.height)))
    }

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

  def apply(
      text: String,
      alignment: TextAlignment,
      position: Point,
      depth: Depth,
      rotation: Radians,
      scale: Vector2,
      fontKey: FontKey,
      effects: Effects,
      eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
  ) =
    new Text(text, alignment, position, depth, rotation, scale, fontKey, effects, eventHandler)

}
