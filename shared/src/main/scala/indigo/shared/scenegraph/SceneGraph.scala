package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.animation.AnimationAction._
import indigo.shared.animation.AnimationKey
import indigo.shared.animation.CycleLabel
import indigo.shared.datatypes._
import indigo.shared.IndigoLogger

import indigo.shared.{AnimationsRegister, FontRegister}

object SceneGraphNode {
  def empty: Group = Group(Point.zero, Depth.Base, Nil)
}

sealed trait SceneGraphNode {
  def bounds: Rectangle
  val depth: Depth

  def withDepth(depth: Depth): SceneGraphNode
  def moveTo(pt: Point): SceneGraphNode
  def moveTo(x: Int, y: Int): SceneGraphNode
  def moveBy(pt: Point): SceneGraphNode
  def moveBy(x: Int, y: Int): SceneGraphNode

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def flatten: List[Renderable] = {
    def rec(acc: List[Renderable]): List[Renderable] =
      this match {
        case l: Renderable =>
          l :: acc

        case b: Group =>
          b.children
            .map(c => c.withDepth(c.depth + b.depth).moveBy(b.positionOffset))
            .flatMap(n => n.flatten) ++ acc
      }

    rec(Nil)
  }
}

final class Group(val positionOffset: Point, val depth: Depth, val children: List[SceneGraphNode]) extends SceneGraphNode {

  def withDepth(newDepth: Depth): Group =
    Group(positionOffset, newDepth, children)

  def moveTo(pt: Point): Group =
    Group(pt, depth, children)
  def moveTo(x: Int, y: Int): Group =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Group =
    moveTo(positionOffset + pt)
  def moveBy(x: Int, y: Int): Group =
    moveBy(Point(x, y))

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
    Group(positionOffset, depth, children :+ child)

  def addChildren(additionalChildren: List[SceneGraphNode]): Group =
    Group(positionOffset, depth, children ++ additionalChildren)

}

object Group {

  def apply(positionOffset: Point, depth: Depth, children: List[SceneGraphNode]): Group =
    new Group(positionOffset, depth, children.toList)

  def apply(position: Point, depth: Depth, children: SceneGraphNode*): Group =
    Group(position, depth, children.toList)

  def apply(children: SceneGraphNode*): Group =
    Group(Point.zero, Depth.Base, children.toList)

  def apply(children: List[SceneGraphNode]): Group =
    Group(Point.zero, Depth.Base, children)
}

sealed trait Renderable extends SceneGraphNode {
  def bounds: Rectangle
  def effects: Effects
  def eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]

  def x: Int
  def y: Int
  def rotation: Radians
  def scale: Vector2

  def moveTo(pt: Point): Renderable
  def moveTo(x: Int, y: Int): Renderable

  def moveBy(pt: Point): Renderable
  def moveBy(x: Int, y: Int): Renderable

  def rotate(angle: Radians): Renderable
  def rotateBy(angle: Radians): Renderable

  def scaleBy(x: Double, y: Double): Renderable

  def withDepth(depth: Depth): Renderable
  def withAlpha(a: Double): Renderable
  def withTint(tint: Tint): Renderable
  def withTint(red: Double, green: Double, blue: Double): Renderable
  def flipHorizontal(h: Boolean): Renderable
  def flipVertical(v: Boolean): Renderable

  def onEvent(e: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]): Renderable

  //TODO: Review this.
  def eventHandlerWithBoundsApplied(e: GlobalEvent): Option[GlobalEvent]

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

  def scaleBy(x: Double, y: Double): Renderable =
    Graphic(bounds, depth, rotation, Vector2(x, y), imageAssetRef, ref, crop, effects, eventHandler)

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

final case class Sprite(
    bindingKey: BindingKey,
    bounds: Rectangle,
    depth: Depth,
    rotation: Radians,
    scale: Vector2,
    animationsKey: AnimationKey,
    ref: Point,
    effects: Effects,
    eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
) extends Renderable {

  def x: Int = bounds.position.x - ref.x
  def y: Int = bounds.position.y - ref.y

  def withDepth(depth: Depth): Sprite =
    this.copy(depth = depth)

  def moveTo(pt: Point): Sprite =
    this.copy(bounds = bounds.moveTo(pt))
  def moveTo(x: Int, y: Int): Sprite =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Sprite =
    this.copy(
      bounds = bounds.moveTo(this.bounds.position + pt)
    )
  def moveBy(x: Int, y: Int): Sprite =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Renderable = ???
  def rotateBy(angle: Radians): Renderable =
    rotate(rotation + angle)

  def scaleBy(x: Double, y: Double): Renderable =
    this.copy(scale = Vector2(x, y))

  def withBindingKey(bindingKey: BindingKey): Sprite =
    this.copy(bindingKey = bindingKey)

  def withAlpha(a: Double): Sprite =
    this.copy(effects = effects.withAlpha(a))

  def withTint(tint: Tint): Sprite =
    this.copy(effects = effects.withTint(tint))

  def withTint(red: Double, green: Double, blue: Double): Sprite =
    this.copy(effects = effects.withTint(Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Sprite =
    this.copy(effects = effects.withFlip(Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Sprite =
    this.copy(effects = effects.withFlip(Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withRef(ref: Point): Sprite =
    this.copy(ref = ref)
  def withRef(x: Int, y: Int): Sprite =
    this.copy(ref = Point(x, y))

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
    this.copy(eventHandler = e)

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
    eventHandler: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]
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
          .map(line => TextLine(line, Text.calculateBoundsOfLine(line, fontInfo)))
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
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Text =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Text =
    this.copy(
      position = this.position + pt
    )
  def moveBy(x: Int, y: Int): Text =
    moveBy(Point(x, y))

  def rotate(angle: Radians): Renderable = ???
  def rotateBy(angle: Radians): Renderable =
    rotate(rotation + angle)

  def scaleBy(x: Double, y: Double): Renderable =
    this.copy(scale = Vector2(x, y))

  def withDepth(depth: Depth): Text =
    this.copy(depth = depth)

  def withAlpha(a: Double): Text =
    this.copy(effects = effects.withAlpha(a))

  def withTint(tint: Tint): Text =
    this.copy(effects = effects.withTint(tint))

  def withTint(red: Double, green: Double, blue: Double): Text =
    this.copy(effects = effects.withTint(Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Text =
    this.copy(effects = effects.withFlip(Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Text =
    this.copy(effects = effects.withFlip(Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withAlignment(alignment: TextAlignment): Text =
    this.copy(alignment = alignment)

  def alignLeft: Text   = copy(alignment = TextAlignment.Left)
  def alignCenter: Text = copy(alignment = TextAlignment.Center)
  def alignRight: Text  = copy(alignment = TextAlignment.Right)

  def withText(text: String): Text =
    this.copy(text = text)

  def withFontKey(fontKey: FontKey): Text =
    this.copy(fontKey = fontKey)

  def onEvent(e: ((Rectangle, GlobalEvent)) => Option[GlobalEvent]): Text =
    this.copy(eventHandler = e)

  def alignedBounds: Rectangle =
    (alignment, bounds.moveTo(position)) match {
      case (TextAlignment.Left, b)   => b
      case (TextAlignment.Center, b) => b.moveTo(Point(b.x - (b.width / 2), b.y))
      case (TextAlignment.Right, b)  => b.moveTo(Point(b.x - b.width, b.y))
    }

  def eventHandlerWithBoundsApplied(e: GlobalEvent): Option[GlobalEvent] =
    eventHandler((alignedBounds, e))

}

final case class TextLine(text: String, lineBounds: Rectangle)

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
}
