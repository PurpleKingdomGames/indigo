package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.AnimationAction._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._

case class SceneGraphUpdate[ViewEventDataType](rootNode: SceneGraphRootNode[ViewEventDataType], viewEvents: List[ViewEvent[ViewEventDataType]])

object SceneGraphNode {
  def empty[ViewEventDataType]: SceneGraphNodeBranch[ViewEventDataType] = SceneGraphNodeBranch(Nil)
}

sealed trait SceneGraphNode[ViewEventDataType] {

  private[gameengine] def flatten: List[SceneGraphNodeLeaf[ViewEventDataType]] = {
    def rec(acc: List[SceneGraphNodeLeaf[ViewEventDataType]]): List[SceneGraphNodeLeaf[ViewEventDataType]] = {
      this match {
        case l: SceneGraphNodeLeaf[ViewEventDataType] => l :: acc
        case b: SceneGraphNodeBranch[ViewEventDataType] =>
          b.children.flatMap(n => n.flatten) ++ acc
      }
    }

    rec(Nil)
  }

}

case class SceneGraphNodeBranch[ViewEventDataType](children: List[SceneGraphNode[ViewEventDataType]]) extends SceneGraphNode[ViewEventDataType]

object SceneGraphNodeBranch {
  def apply[ViewEventDataType](children: SceneGraphNode[ViewEventDataType]*): SceneGraphNodeBranch[ViewEventDataType] =
    SceneGraphNodeBranch(children.toList)
}

sealed trait SceneGraphNodeLeaf[ViewEventDataType] extends SceneGraphNode[ViewEventDataType] {
  val bounds: Rectangle
  val depth: Depth
  val imageAssetRef: String
  val effects: Effects
  val ref: Point
  val crop: Rectangle
  val eventHandler: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]

  private[gameengine] def frameHash: String

  def x: Int = bounds.position.x - ref.x
  def y: Int = bounds.position.y - ref.y

  def moveTo(pt: Point): SceneGraphNodeLeaf[ViewEventDataType]
  def moveTo(x: Int, y: Int): SceneGraphNodeLeaf[ViewEventDataType]

  def moveBy(pt: Point): SceneGraphNodeLeaf[ViewEventDataType]
  def moveBy(x: Int, y: Int): SceneGraphNodeLeaf[ViewEventDataType]

  def withDepth(depth: Int): SceneGraphNodeLeaf[ViewEventDataType]
  def withAlpha(a: Double): SceneGraphNodeLeaf[ViewEventDataType]
  def withTint(red: Double, green: Double, blue: Double): SceneGraphNodeLeaf[ViewEventDataType]
  def flipHorizontal(h: Boolean): SceneGraphNodeLeaf[ViewEventDataType]
  def flipVertical(v: Boolean): SceneGraphNodeLeaf[ViewEventDataType]

  def onEvent(e: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]): SceneGraphNodeLeaf[ViewEventDataType]

  private[gameengine] val eventHandlerWithBoundsApplied: GameEvent => Option[ViewEvent[ViewEventDataType]]

  private[gameengine] def saveAnimationMemento: Option[AnimationMemento]

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeLeaf[ViewEventDataType]

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphNodeLeaf[ViewEventDataType]
}

case class Graphic[ViewEventDataType](bounds: Rectangle, depth: Depth, imageAssetRef: String, ref: Point, crop: Rectangle, effects: Effects, eventHandler: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]) extends SceneGraphNodeLeaf[ViewEventDataType] {

  private[gameengine] def frameHash: String = crop.hash + "_" + imageAssetRef

  def moveTo(pt: Point): Graphic[ViewEventDataType] =
    this.copy(bounds = bounds.copy(position = pt))
  def moveTo(x: Int, y: Int): Graphic[ViewEventDataType] =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Graphic[ViewEventDataType] =
    this.copy(bounds =
      bounds.copy(
        position = this.bounds.position + pt
      )
    )
  def moveBy(x: Int, y: Int): Graphic[ViewEventDataType] =
    moveBy(Point(x, y))

  def withDepth(depth: Int): Graphic[ViewEventDataType] =
    this.copy(depth = Depth(depth))

  def withAlpha(a: Double): Graphic[ViewEventDataType] =
    this.copy(effects = effects.copy(alpha = a))

  def withTint(red: Double, green: Double, blue: Double): Graphic[ViewEventDataType] =
    this.copy(effects = effects.copy(tint = Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Graphic[ViewEventDataType] =
    this.copy(effects = effects.copy(flip = Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Graphic[ViewEventDataType] =
    this.copy(effects = effects.copy(flip = Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withRef(ref: Point): Graphic[ViewEventDataType] =
    this.copy(ref = ref)
  def withRef(x: Int, y: Int): Graphic[ViewEventDataType] =
    this.copy(ref = Point(x, y))

  def withCrop(crop: Rectangle): Graphic[ViewEventDataType] =
    this.copy(crop = crop)
  def withCrop(x: Int, y: Int, width: Int, height: Int): Graphic[ViewEventDataType] =
    this.copy(crop = Rectangle(x, y, width, height))

  def onEvent(e: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]): Graphic[ViewEventDataType] =
    this.copy(eventHandler = e)

  private[gameengine] val eventHandlerWithBoundsApplied: GameEvent => Option[ViewEvent[ViewEventDataType]] =
    (e: GameEvent) => eventHandler(bounds, e)

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): Graphic[ViewEventDataType] = this

  private[gameengine] def saveAnimationMemento: Option[AnimationMemento] = None

  private[gameengine] def runAnimationActions(gameTime: GameTime): Graphic[ViewEventDataType] = this

}

object Graphic {
  def apply[ViewEventDataType](x: Int, y: Int, width: Int, height: Int, depth: Int, imageAssetRef: String): Graphic[ViewEventDataType] =
    Graphic(
      bounds = Rectangle(x, y, width, height),
      depth = depth,
      imageAssetRef = imageAssetRef,
      ref = Point.zero,
      crop = Rectangle(0, 0, width, height),
      effects = Effects.default,
      eventHandler = (_:(Rectangle, GameEvent)) => None
    )

  def apply[ViewEventDataType](bounds: Rectangle, depth: Int, imageAssetRef: String): Graphic[ViewEventDataType] =
    Graphic(
      bounds = bounds,
      depth = depth,
      imageAssetRef = imageAssetRef,
      ref = Point.zero,
      crop = bounds,
      effects = Effects.default,
      eventHandler = (_:(Rectangle, GameEvent)) => None
    )
}

case class Sprite[ViewEventDataType](bindingKey: BindingKey, bounds: Rectangle, depth: Depth, imageAssetRef: String, animations: Animations, ref: Point, effects: Effects, eventHandler: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]) extends SceneGraphNodeLeaf[ViewEventDataType] {

  private[gameengine] def frameHash: String = animations.currentFrame.bounds.hash + "_" + imageAssetRef

  def withDepth(depth: Int): Sprite[ViewEventDataType] =
    this.copy(depth = Depth(depth))

  val crop: Rectangle = bounds

  def moveTo(pt: Point): Sprite[ViewEventDataType] =
    this.copy(bounds = bounds.copy(position = pt))
  def moveTo(x: Int, y: Int): Sprite[ViewEventDataType] =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Sprite[ViewEventDataType] =
    this.copy(bounds =
      bounds.copy(
        position = this.bounds.position + pt
      )
    )
  def moveBy(x: Int, y: Int): Sprite[ViewEventDataType] =
    moveBy(Point(x, y))

  def withBindingKey(keyValue: String): Sprite[ViewEventDataType] =
    this.copy(bindingKey = BindingKey(keyValue))
  def withBindingKey(bindingKey: BindingKey): Sprite[ViewEventDataType] =
    this.copy(bindingKey = bindingKey)

  def withAlpha(a: Double): Sprite[ViewEventDataType] =
    this.copy(effects = effects.copy(alpha = a))

  def withTint(red: Double, green: Double, blue: Double): Sprite[ViewEventDataType] =
    this.copy(effects = effects.copy(tint = Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Sprite[ViewEventDataType] =
    this.copy(effects = effects.copy(flip = Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Sprite[ViewEventDataType] =
    this.copy(effects = effects.copy(flip = Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withRef(ref: Point): Sprite[ViewEventDataType] =
    this.copy(ref = ref)
  def withRef(x: Int, y: Int): Sprite[ViewEventDataType] =
    this.copy(ref = Point(x, y))

  def play(): Sprite[ViewEventDataType] =
    this.copy(animations = animations.addAction(Play))

  def changeCycle(label: String): Sprite[ViewEventDataType] =
    this.copy(animations = animations.addAction(ChangeCycle(label)))

  def jumpToFirstFrame(): Sprite[ViewEventDataType] =
    this.copy(animations = animations.addAction(JumpToFirstFrame))

  def jumpToLastFrame(): Sprite[ViewEventDataType] =
    this.copy(animations = animations.addAction(JumpToLastFrame))

  def jumpToFrame(number: Int): Sprite[ViewEventDataType] =
    this.copy(animations = animations.addAction(JumpToFrame(number)))

  def onEvent(e: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]): Sprite[ViewEventDataType] =
    this.copy(eventHandler = e)

  private[gameengine] val eventHandlerWithBoundsApplied: GameEvent => Option[ViewEvent[ViewEventDataType]] =
    (e: GameEvent) => eventHandler(bounds, e)

  private[gameengine] def saveAnimationMemento: Option[AnimationMemento] = Option(animations.saveMemento(bindingKey))

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): Sprite[ViewEventDataType] =
    animationStates.withBindingKey(bindingKey) match {
      case Some(memento) => this.copy(animations = animations.applyMemento(memento))
      case None => this
    }

  private[gameengine] def runAnimationActions(gameTime: GameTime): Sprite[ViewEventDataType] = this.copy(animations = animations.runActions(gameTime))

}

object Sprite {
  def apply[ViewEventDataType](bindingKey: BindingKey, x: Int, y: Int, width: Int, height: Int, depth: Int, imageAssetRef: String, animations: Animations): Sprite[ViewEventDataType] =
    Sprite(
      bindingKey = bindingKey,
      bounds = Rectangle(x, y, width, height),
      depth = depth,
      imageAssetRef = imageAssetRef,
      animations = animations,
      ref = Point.zero,
      effects = Effects.default,
      eventHandler = (_:(Rectangle, GameEvent)) => None
    )
}

case class Text[ViewEventDataType](text: String, alignment: TextAlignment, position: Point, depth: Depth, fontInfo: FontInfo, effects: Effects, eventHandler: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]) extends SceneGraphNodeLeaf[ViewEventDataType] {

  private[gameengine] def frameHash: String = "" // Not used - look up done another way.

  // Handled a different way
  val ref: Point = Point(0, 0)

  val lines: List[TextLine] =
    text
      .split('\n').toList
      .map(_.replace("\n", ""))
      .map(line => TextLine(line, Text.calculateBoundsOfLine(line, fontInfo)))

  val bounds: Rectangle =
    lines.map(_.lineBounds).fold(Rectangle.zero) {
      (acc, next) => acc.copy(size = Point(Math.max(acc.width, next.width), acc.height + next.height))
    }

  val crop: Rectangle = bounds
  val imageAssetRef: String = fontInfo.fontSpriteSheet.imageAssetRef

  def moveTo(pt: Point): Text[ViewEventDataType] =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Text[ViewEventDataType] =
    moveTo(Point(x, y))

  def moveBy(pt: Point): Text[ViewEventDataType] =
    this.copy(
      position = this.position + pt
    )
  def moveBy(x: Int, y: Int): Text[ViewEventDataType] =
    moveBy(Point(x, y))

  def withDepth(depth: Int): Text[ViewEventDataType] =
    this.copy(depth = Depth(depth))

  def withAlpha(a: Double): Text[ViewEventDataType] =
    this.copy(effects = effects.copy(alpha = a))

  def withTint(red: Double, green: Double, blue: Double): Text[ViewEventDataType] =
    this.copy(effects = effects.copy(tint = Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Text[ViewEventDataType] =
    this.copy(effects = effects.copy(flip = Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Text[ViewEventDataType] =
    this.copy(effects = effects.copy(flip = Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withAlignment(alignment: TextAlignment): Text[ViewEventDataType] =
    this.copy(alignment = alignment)

  def alignLeft: Text[ViewEventDataType] = copy(alignment = AlignLeft)
  def alignCenter: Text[ViewEventDataType] = copy(alignment = AlignCenter)
  def alignRight: Text[ViewEventDataType] = copy(alignment = AlignRight)

  def withText(text: String): Text[ViewEventDataType] =
    this.copy(text = text)

  def withFontInfo(fontInfo: FontInfo): Text[ViewEventDataType] =
    this.copy(fontInfo = fontInfo)

  def onEvent(e: ((Rectangle, GameEvent)) => Option[ViewEvent[ViewEventDataType]]): Text[ViewEventDataType] =
    this.copy(eventHandler = e)

  private val realBound: Rectangle =
    (alignment, bounds.copy(position = position)) match {
      case (AlignLeft, b) => b
      case (AlignCenter, b) => b.copy(position = Point(b.x - (b.width / 2), b.y))
      case (AlignRight, b) => b.copy(position = Point(b.x - b.width, b.y))
    }

  private[gameengine] val eventHandlerWithBoundsApplied: GameEvent => Option[ViewEvent[ViewEventDataType]] =
    (e: GameEvent) => eventHandler(realBound, e)

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): Text[ViewEventDataType] = this

  private[gameengine] def saveAnimationMemento: Option[AnimationMemento] = None

  private[gameengine] def runAnimationActions(gameTime: GameTime): Text[ViewEventDataType] = this

}

case class TextLine(text: String, lineBounds: Rectangle)

object Text {

  def calculateBoundsOfLine(lineText: String, fontInfo: FontInfo): Rectangle = {
    lineText.toList
      .map(c => fontInfo.findByCharacter(c).bounds)
      .fold(Rectangle.zero)((acc, curr) => Rectangle(0, 0, acc.width + curr.width, Math.max(acc.height, curr.height)))
  }

  def apply[ViewEventDataType](text: String, x: Int, y: Int, depth: Int, fontInfo: FontInfo): Text[ViewEventDataType] =
    Text(
      text = text,
      alignment = AlignLeft,
      position = Point(x, y),
      depth = depth,
      fontInfo = fontInfo,
      effects = Effects.default,
      eventHandler = (_:(Rectangle, GameEvent)) => None
    )
}
