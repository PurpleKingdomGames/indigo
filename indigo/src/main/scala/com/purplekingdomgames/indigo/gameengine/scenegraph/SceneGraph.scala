package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph.AnimationAction._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._

case class SceneGraphUpdate[ViewEventDataType](rootNode: SceneGraphRootNode, viewEvents: List[ViewEvent[ViewEventDataType]])

case class SceneGraphRootNode(game: SceneGraphGameLayer, lighting: SceneGraphLightingLayer, ui: SceneGraphUiLayer) {
  def addLightingLayer(lighting: SceneGraphLightingLayer): SceneGraphRootNode =
    this.copy(lighting = lighting)

  def addUiLayer(ui: SceneGraphUiLayer): SceneGraphRootNode =
    this.copy(ui = ui)
}

object SceneGraphRootNode {
  def apply(game: SceneGraphGameLayer): SceneGraphRootNode =
    SceneGraphRootNode(game, SceneGraphLightingLayer.empty, SceneGraphUiLayer.empty)
}

case class SceneGraphGameLayer(node: SceneGraphNode)
object SceneGraphGameLayer {
  def empty: SceneGraphGameLayer =
    SceneGraphGameLayer(SceneGraphNode.empty)

  def apply(nodes: SceneGraphNode*): SceneGraphGameLayer =
    SceneGraphGameLayer(
      SceneGraphNodeBranch(nodes.toList)
    )
}

case class SceneGraphLightingLayer(node: SceneGraphNodeBranch, ambientLight: AmbientLight) {

  def withAmbientLight(ambientLight: AmbientLight): SceneGraphLightingLayer = {
    this.copy(
      ambientLight = ambientLight
    )
  }
  def withAmbientLightAmount(amount: Double): SceneGraphLightingLayer = {
    this.copy(
      ambientLight = this.ambientLight.copy(
        amount = amount
      )
    )
  }
  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneGraphLightingLayer = {
    this.copy(
      ambientLight = this.ambientLight.copy(
        tint = Tint(r, g, b)
      )
    )
  }

}
object SceneGraphLightingLayer {
  def empty: SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      SceneGraphNode.empty,
      AmbientLight.none
    )

  def apply(nodes: SceneGraphNodeLeaf*): SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      SceneGraphNodeBranch(nodes.toList),
      AmbientLight.none
    )
}

case class AmbientLight(tint: Tint, amount: Double)
object AmbientLight {
  val none: AmbientLight = AmbientLight(Tint.none, 1)
}

case class SceneGraphUiLayer(node: SceneGraphNode)
object SceneGraphUiLayer {
  def empty: SceneGraphUiLayer =
    SceneGraphUiLayer(SceneGraphNode.empty)

  def apply(nodes: SceneGraphNode*): SceneGraphUiLayer =
    SceneGraphUiLayer(
      SceneGraphNodeBranch(nodes.toList)
    )
}

object SceneGraphNode {
  def empty: SceneGraphNodeBranch = SceneGraphNodeBranch(Nil)
}

sealed trait SceneGraphNode

case class SceneGraphNodeBranch(children: List[SceneGraphNode]) extends SceneGraphNode

object SceneGraphNodeBranch {
  def apply(children: SceneGraphNode*): SceneGraphNodeBranch =
    SceneGraphNodeBranch(children.toList)
}

sealed trait SceneGraphNodeLeaf extends SceneGraphNode {
  val bounds: Rectangle
  val depth: Depth
  val imageAssetRef: String
  val effects: Effects
  val ref: Point
  val crop: Rectangle

  def x: Int = bounds.position.x - ref.x
  def y: Int = bounds.position.y - ref.y

  def withAlpha(a: Double): SceneGraphNodeLeaf
  def withTint(red: Double, green: Double, blue: Double): SceneGraphNodeLeaf
  def flipHorizontal(h: Boolean): SceneGraphNodeLeaf
  def flipVertical(v: Boolean): SceneGraphNodeLeaf
}

case class Graphic(bounds: Rectangle, depth: Depth, imageAssetRef: String, ref: Point, crop: Rectangle, effects: Effects) extends SceneGraphNodeLeaf {

  def withAlpha(a: Double): Graphic =
    this.copy(effects = effects.copy(alpha = a))

  def withTint(red: Double, green: Double, blue: Double): Graphic =
    this.copy(effects = effects.copy(tint = Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Graphic =
    this.copy(effects = effects.copy(flip = Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Graphic =
    this.copy(effects = effects.copy(flip = Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withRef(ref: Point): Graphic =
    this.copy(ref = ref)
  def withRef(x: Int, y: Int): Graphic =
    this.copy(ref = Point(x, y))

  def withCrop(crop: Rectangle): Graphic =
    this.copy(crop = crop)
  def withCrop(x: Int, y: Int, width: Int, height: Int): Graphic =
    this.copy(crop = Rectangle(x, y, width, height))

}

object Graphic {
  def apply(x: Int, y: Int, width: Int, height: Int, depth: Int, imageAssetRef: String): Graphic =
    Graphic(
      bounds = Rectangle(x, y, width, height),
      depth = depth,
      imageAssetRef = imageAssetRef,
      ref = Point.zero,
      crop = Rectangle(x, y, width, height),
      effects = Effects.default
    )
}

case class Sprite(bindingKey: BindingKey, bounds: Rectangle, depth: Depth, imageAssetRef: String, animations: Animations, ref: Point, effects: Effects) extends SceneGraphNodeLeaf {

  val crop: Rectangle = bounds

  def moveTo(x: Int, y: Int): Sprite =
    this.copy(bounds = bounds.copy(position = Point(x, y)))

  def moveBy(x: Int, y: Int): Sprite =
    this.copy(bounds = bounds.copy(
      position =
        bounds.position.copy(
          x = bounds.position.x + x,
          y = bounds.position.y + y
        )
      )
    )

  def withBindingKey(keyValue: String): Sprite =
    this.copy(bindingKey = BindingKey(keyValue))
  def withBindingKey(bindingKey: BindingKey): Sprite =
    this.copy(bindingKey = bindingKey)

  def withAlpha(a: Double): Sprite =
    this.copy(effects = effects.copy(alpha = a))

  def withTint(red: Double, green: Double, blue: Double): Sprite =
    this.copy(effects = effects.copy(tint = Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Sprite =
    this.copy(effects = effects.copy(flip = Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Sprite =
    this.copy(effects = effects.copy(flip = Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withRef(ref: Point): Sprite =
    this.copy(ref = ref)
  def withRef(x: Int, y: Int): Sprite =
    this.copy(ref = Point(x, y))

  def play(): Sprite =
    this.copy(animations = animations.addAction(Play))

  def changeCycle(label: String): Sprite =
    this.copy(animations = animations.addAction(ChangeCycle(label)))

  def jumpToFirstFrame(): Sprite =
    this.copy(animations = animations.addAction(JumpToFirstFrame))

  def jumpToLastFrame(): Sprite =
    this.copy(animations = animations.addAction(JumpToLastFrame))

  def jumpToFrame(number: Int): Sprite =
    this.copy(animations = animations.addAction(JumpToFrame(number)))

}

object Sprite {
  def apply(bindingKey: BindingKey, x: Int, y: Int, width: Int, height: Int, depth: Int, imageAssetRef: String, animations: Animations): Sprite =
    Sprite(
      bindingKey = bindingKey,
      bounds = Rectangle(x, y, width, height),
      depth = depth,
      imageAssetRef = imageAssetRef,
      animations = animations,
      ref = Point.zero,
      effects = Effects.default
    )
}

case class Text(text: String, alignment: TextAlignment, position: Point, depth: Depth, fontInfo: FontInfo, effects: Effects) extends SceneGraphNodeLeaf {

  // Handled a different way
  val ref: Point = Point(0, 0)

  val bounds: Rectangle = {
    text.toList
      .map(c => fontInfo.findByCharacter(c).bounds)
      .fold(Rectangle(0, 0, 0, 0))((acc, curr) => Rectangle(0, 0, acc.width + curr.width, Math.max(acc.height, curr.height)))
  }

  val crop: Rectangle = bounds
  val imageAssetRef: String = fontInfo.fontSpriteSheet.imageAssetRef

  def withAlpha(a: Double): Text =
    this.copy(effects = effects.copy(alpha = a))

  def withTint(red: Double, green: Double, blue: Double): Text =
    this.copy(effects = effects.copy(tint = Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Text =
    this.copy(effects = effects.copy(flip = Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Text =
    this.copy(effects = effects.copy(flip = Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def withAlignment(alignment: TextAlignment): Text =
    this.copy(alignment = alignment)

  def withText(text: String): Text =
    this.copy(text = text)

  def withFontInfo(fontInfo: FontInfo): Text =
    this.copy(fontInfo = fontInfo)

}

object Text {
  def apply(text: String, x: Int, y: Int, depth: Int, fontInfo: FontInfo): Text =
    Text(
      text = text,
      alignment = AlignLeft,
      position = Point(x, y),
      depth = depth,
      fontInfo = fontInfo,
      effects = Effects.default
    )
}
