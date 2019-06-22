package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.animation.AnimationAction._
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.CycleLabel
import indigo.shared.datatypes._
import indigo.shared.IndigoLogger

import indigo.shared.{AnimationsRegister, FontRegister}

object SceneGraphNode {
  def empty: Group = Group.empty
}

sealed trait SceneGraphNode {
  def bounds: Rectangle
  val depth: Depth

  def x: Int
  def y: Int
  def rotation: Radians
  def scale: Vector2

  def withDepth(depth: Depth): SceneGraphNode
  def moveTo(pt: Point): SceneGraphNode
  def moveTo(x: Int, y: Int): SceneGraphNode
  def moveBy(pt: Point): SceneGraphNode
  def moveBy(x: Int, y: Int): SceneGraphNode
  def rotate(angle: Radians): SceneGraphNode
  def rotateBy(angle: Radians): SceneGraphNode
  def scaleBy(amount: Vector2): SceneGraphNode
  def scaleBy(x: Double, y: Double): SceneGraphNode

}

final class Group(val positionOffset: Point, val rotation: Radians, val scale: Vector2, val depth: Depth, val children: List[SceneGraphNode]) extends SceneGraphNode {

  def x: Int = positionOffset.x
  def y: Int = positionOffset.y

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
    Group(positionOffset, rotation, amount, depth, children)
  def scaleBy(x: Double, y: Double): Group =
    scaleBy(Vector2(x, y))

  def bounds: Rectangle =
    children match {
      case Nil =>
        Rectangle.zero

      case x :: xs =>
        xs.foldLeft(x.bounds) { (acc, node) =>
          Rectangle.expandToInclude(acc, node.bounds)
        }
    }

  def addChild(child: SceneGraphNode): Group =
    Group(positionOffset, rotation, scale, depth, children :+ child)

  def addChildren(additionalChildren: List[SceneGraphNode]): Group =
    Group(positionOffset, rotation, scale, depth, children ++ additionalChildren)

}

object Group {

  def apply(positionOffset: Point, rotation: Radians, scale: Vector2, depth: Depth, children: List[SceneGraphNode]): Group =
    new Group(positionOffset, rotation, scale, depth, children.toList)

  def apply(position: Point, rotation: Radians, scale: Vector2, depth: Depth, children: SceneGraphNode*): Group =
    Group(position, rotation, scale, depth, children.toList)

  def apply(children: SceneGraphNode*): Group =
    Group(Point.zero, Radians.zero, Vector2.one, Depth.Base, children.toList)

  def apply(children: List[SceneGraphNode]): Group =
    Group(Point.zero, Radians.zero, Vector2.one, Depth.Base, children)

  def empty: Group =
    apply(Nil)
}

sealed trait Renderable extends SceneGraphNode {
  def effects: Effects

  def withAlpha(a: Double): Renderable
  def withTint(tint: Tint): Renderable
  def withTint(red: Double, green: Double, blue: Double): Renderable

  def flipHorizontal(h: Boolean): Renderable
  def flipVertical(v: Boolean): Renderable

  def eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
  def onEvent(e: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]): Renderable
  def eventHandlerWithBoundsApplied(e: GlobalEvent): Option[GlobalEvent]

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

final class Graphic(
    val bounds: Rectangle,
    val depth: Depth,
    val rotation: Radians,
    val scale: Vector2,
    val imageAssetRef: String,
    val ref: Point,
    val crop: Rectangle,
    val effects: Effects,
    val eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
) extends Renderable {

  def x: Int = bounds.position.x - ref.x
  def y: Int = bounds.position.y - ref.y

  def moveTo(pt: Point): Graphic =
    Graphic(bounds.moveTo(pt), depth, rotation, scale, imageAssetRef, ref, crop, effects, eventHandler)
  def moveTo(x: Int, y: Int): Graphic =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Graphic =
    Graphic(bounds.moveTo(bounds.position + pt), depth, rotation, scale, imageAssetRef, ref, crop, effects, eventHandler)
  def moveBy(x: Int, y: Int): Graphic =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Renderable =
    Graphic(bounds, depth, angle, scale, imageAssetRef, ref, crop, effects, eventHandler)
  def rotateBy(angle: Radians): Renderable =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Renderable =
    Graphic(bounds, depth, rotation, amount, imageAssetRef, ref, crop, effects, eventHandler)
  def scaleBy(x: Double, y: Double): Renderable =
    scaleBy(Vector2(x, y))

  def withDepth(depthValue: Depth): Graphic =
    Graphic(bounds, depthValue, rotation, scale, imageAssetRef, ref, crop, effects, eventHandler)

  def withAlpha(a: Double): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, ref, crop, effects.withAlpha(a), eventHandler)

  def withTint(tint: Tint): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, ref, crop, effects.withTint(tint), eventHandler)

  def withTint(red: Double, green: Double, blue: Double): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, ref, crop, effects.withTint(Tint(red, green, blue)), eventHandler)

  def flipHorizontal(hValue: Boolean): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, ref, crop, effects.withFlip(Flip(hValue, effects.flip.vertical)), eventHandler)

  def flipVertical(vValue: Boolean): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, ref, crop, effects.withFlip(Flip(effects.flip.horizontal, vValue)), eventHandler)

  def withRef(refValue: Point): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, refValue, crop, effects, eventHandler)
  def withRef(xValue: Int, yValue: Int): Graphic =
    withRef(Point(xValue, yValue))

  def withCrop(crop: Rectangle): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, ref, crop, effects, eventHandler)
  def withCrop(xValue: Int, yValue: Int, widthValue: Int, heightValue: Int): Graphic =
    withCrop(Rectangle(xValue, yValue, widthValue, heightValue))

  def onEvent(eventHandlerValue: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]): Graphic =
    Graphic(bounds, depth, rotation, scale, imageAssetRef, ref, crop, effects, eventHandlerValue)

  def eventHandlerWithBoundsApplied(e: GlobalEvent): Option[GlobalEvent] =
    eventHandler((bounds, e))

}

object Graphic {

  def apply(
      bounds: Rectangle,
      depth: Depth,
      rotation: Radians,
      scale: Vector2,
      imageAssetRef: String,
      ref: Point,
      crop: Rectangle,
      effects: Effects,
      eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
  ): Graphic =
    new Graphic(
      bounds,
      depth,
      rotation,
      scale,
      imageAssetRef,
      ref,
      crop,
      effects,
      eventHandler
    )

  def apply(x: Int, y: Int, width: Int, height: Int, depth: Int, imageAssetRef: String): Graphic =
    Graphic(
      bounds = Rectangle(x, y, width, height),
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      imageAssetRef = imageAssetRef,
      ref = Point.zero,
      crop = Rectangle(0, 0, width, height),
      effects = Effects.default,
      eventHandler = (_: (Rectangle, GlobalEvent)) => None
    )

  def apply(bounds: Rectangle, depth: Int, imageAssetRef: String): Graphic =
    Graphic(
      bounds = bounds,
      depth = Depth(depth),
      rotation = Radians.zero,
      scale = Vector2.one,
      imageAssetRef = imageAssetRef,
      ref = Point.zero,
      crop = bounds,
      effects = Effects.default,
      eventHandler = (_: (Rectangle, GlobalEvent)) => None
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
    val eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
) extends Renderable {

  def x: Int = bounds.position.x - ref.x
  def y: Int = bounds.position.y - ref.y

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

  def rotate(angle: Radians): Renderable =
    Sprite(bindingKey, bounds, depth, angle, scale, animationsKey, ref, effects, eventHandler)
  def rotateBy(angle: Radians): Renderable =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Renderable =
    Sprite(bindingKey, bounds, depth, rotation, amount, animationsKey, ref, effects, eventHandler)
  def scaleBy(x: Double, y: Double): Renderable =
    scaleBy(Vector2(x, y))

  def withBindingKey(newBindingKey: BindingKey): Sprite =
    Sprite(newBindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects, eventHandler)

  def withAlpha(a: Double): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withAlpha(a), eventHandler)

  def withTint(tint: Tint): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withTint(tint), eventHandler)

  def withTint(red: Double, green: Double, blue: Double): Sprite =
    withTint(Tint(red, green, blue))

  def flipHorizontal(h: Boolean): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withFlip(Flip(horizontal = h, vertical = effects.flip.vertical)), eventHandler)

  def flipVertical(v: Boolean): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects.withFlip(Flip(horizontal = effects.flip.horizontal, vertical = v)), eventHandler)

  def withRef(newRef: Point): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, newRef, effects, eventHandler)
  def withRef(x: Int, y: Int): Sprite =
    withRef(Point(x, y))

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

  def onEvent(e: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]): Sprite =
    Sprite(bindingKey, bounds, depth, rotation, scale, animationsKey, ref, effects, e)

  def eventHandlerWithBoundsApplied(e: GlobalEvent): Option[GlobalEvent] =
    eventHandler((bounds, e))

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
      eventHandler = (_: (Rectangle, GlobalEvent)) => None
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
      eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
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
    val eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
) extends Renderable {

  def x: Int = bounds.position.x
  def y: Int = bounds.position.y

  def lines: List[TextLine] =
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
        IndigoLogger.errorOnce(s"Cannot build Text lines, missing Font with key: $fontKey")
        Nil
      }

  def bounds: Rectangle =
    lines.map(_.lineBounds).fold(Rectangle.zero) { (acc, next) =>
      acc.resize(Point(Math.max(acc.width, next.width), acc.height + next.height))
    }

  def moveTo(pt: Point): Text =
    Text(text, alignment, pt, depth, rotation, scale, fontKey, effects, eventHandler)
  def moveTo(x: Int, y: Int): Text =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Text =
    Text(text, alignment, position + pt, depth, rotation, scale, fontKey, effects, eventHandler)
  def moveBy(x: Int, y: Int): Text =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Renderable =
    Text(text, alignment, position, depth, angle, scale, fontKey, effects, eventHandler)
  def rotateBy(angle: Radians): Renderable =
    rotate(rotation + angle)

  def scaleBy(amount: Vector2): Renderable =
    Text(text, alignment, position, depth, rotation, amount, fontKey, effects, eventHandler)
  def scaleBy(x: Double, y: Double): Renderable =
    scaleBy(Vector2(x, y))

  def withDepth(newDepth: Depth): Text =
    Text(text, alignment, position, newDepth, rotation, scale, fontKey, effects, eventHandler)

  def withAlpha(a: Double): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withAlpha(a), eventHandler)

  def withTint(tint: Tint): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withTint(tint), eventHandler)

  def withTint(red: Double, green: Double, blue: Double): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects.withTint(Tint(red, green, blue)), eventHandler)

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

  def onEvent(e: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]): Text =
    Text(text, alignment, position, depth, rotation, scale, fontKey, effects, e)

  def alignedBounds: Rectangle =
    (alignment, bounds.moveTo(position)) match {
      case (TextAlignment.Left, b)   => b
      case (TextAlignment.Center, b) => b.moveTo(Point(b.x - (b.width / 2), b.y))
      case (TextAlignment.Right, b)  => b.moveTo(Point(b.x - b.width, b.y))
    }

  def eventHandlerWithBoundsApplied(e: GlobalEvent): Option[GlobalEvent] =
    eventHandler((alignedBounds, e))

}

final class TextLine(val text: String, val lineBounds: Rectangle)

object Text {

  def calculateBoundsOfLine(lineText: String, fontInfo: FontInfo): Rectangle =
    lineText.toList
      .map(c => fontInfo.findByCharacter(c).bounds)
      .fold(Rectangle.zero)((acc, curr) => Rectangle(0, 0, acc.width + curr.width, Math.max(acc.height, curr.height)))

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
      eventHandler = (_: (Rectangle, GlobalEvent)) => None
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
      eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
  ) =
    new Text(text, alignment, position, depth, rotation, scale, fontKey, effects, eventHandler)

}
