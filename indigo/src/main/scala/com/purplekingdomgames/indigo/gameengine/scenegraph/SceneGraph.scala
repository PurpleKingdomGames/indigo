package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.AnimationStates

import scala.language.implicitConversions
import scala.util.Random

object SceneGraphNode {
  def empty: SceneGraphNode = SceneGraphNodeBranch(Nil)
}
sealed trait SceneGraphNode {

  def flatten(acc: List[SceneGraphNodeLeaf]): List[SceneGraphNodeLeaf] = {
    this match {
      case l: SceneGraphNodeLeaf => l :: acc
      case b: SceneGraphNodeBranch =>
        b.children.flatMap(n => n.flatten(Nil)) ++ acc
    }
  }

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNode

}

// Types of SceneGraphNode
case class SceneGraphNodeBranch(children: List[SceneGraphNode]) extends SceneGraphNode {
  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNode =
    this.copy(children.map(_.applyAnimationMemento(animationStates)))
}
sealed trait SceneGraphNodeLeaf extends SceneGraphNode {
  val bounds: Rectangle
  val depth: Depth
  val imageAssetRef: String
  val effects: Effects
  val ref: Point
  val crop: Option[Rectangle]

  def withAlpha(a: Double): SceneGraphNodeLeaf
  def withTint(red: Double, green: Double, blue: Double): SceneGraphNodeLeaf
  def flipHorizontal(h: Boolean): SceneGraphNodeLeaf
  def flipVertical(v: Boolean): SceneGraphNodeLeaf
}

// Concrete leaf types
case class Graphic(bounds: Rectangle, depth: Depth, imageAssetRef: String, ref: Point, crop: Option[Rectangle], effects: Effects) extends SceneGraphNodeLeaf {

  val x: Int = bounds.position.x - ref.x
  val y: Int = bounds.position.y - ref.y

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

  def withCrop(crop: Rectangle): Graphic =
    this.copy(crop = Option(crop))

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNode = this
}

case class BindingKey(value: String)
object BindingKey {
  private val random: Random = new Random

  def generate: BindingKey = BindingKey(random.alphanumeric.take(16).mkString)
}

case class Sprite(bindingKey: BindingKey, bounds: Rectangle, depth: Depth, imageAssetRef: String, animations: Animations, ref: Point, effects: Effects) extends SceneGraphNodeLeaf {

  val crop: Option[Rectangle] = None

  val x: Int = bounds.position.x - ref.x
  val y: Int = bounds.position.y - ref.y

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

  def saveAnimationMemento: AnimationMemento = animations.saveMemento(bindingKey)

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNode =
    animationStates.withBindingKey(bindingKey) match {
      case Some(memento) => this.copy(animations = animations.applyMemento(memento))
      case None => this
    }
  
//  def nextFrame: Sprite = this.copy(animations = animations.nextFrame)

}

case class FontInfo(charSize: Point, fontSpriteSheet: FontSpriteSheet, fontChar: FontChar, fontChars: List[FontChar]) {
  private val nonEmtpyChars: List[FontChar] = fontChar +: fontChars

  def addChar(fontChar: FontChar) = FontInfo(charSize, fontSpriteSheet, fontChar, nonEmtpyChars)

  def findByCharacter(character: String): FontChar = nonEmtpyChars.find(p => p.character == character).getOrElse(FontChar("?", Point(0, 0)))
}
case class FontSpriteSheet(imageAssetRef: String, size: Point)
case class FontChar(character: String, offset: Point)

sealed trait TextAlignment
case object AlignLeft extends TextAlignment
case object AlignCenter extends TextAlignment
case object AlignRight extends TextAlignment

case class Text(text: String, alignment: TextAlignment, position: Point, depth: Depth, fontInfo: FontInfo, effects: Effects) extends SceneGraphNodeLeaf {

  // Handled a different way
  val ref: Point = Point(0, 0)
  val crop: Option[Rectangle] = None

  val bounds: Rectangle = Rectangle(position, Point(text.length * fontInfo.charSize.x, fontInfo.charSize.y))
  val imageAssetRef: String = fontInfo.fontSpriteSheet.imageAssetRef

  def withAlpha(a: Double): Text =
    this.copy(effects = effects.copy(alpha = a))

  def withTint(red: Double, green: Double, blue: Double): Text =
    this.copy(effects = effects.copy(tint = Tint(red, green, blue)))

  def flipHorizontal(h: Boolean): Text =
    this.copy(effects = effects.copy(flip = Flip(horizontal = h, vertical = effects.flip.vertical)))

  def flipVertical(v: Boolean): Text =
    this.copy(effects = effects.copy(flip = Flip(horizontal = effects.flip.horizontal, vertical = v)))

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNode = this
}

// Graphical effects
object Effects {
  val default = Effects(
    alpha = 1.0,
    tint = Tint(
      r = 1,
      g = 1,
      b = 1
    ),
    flip = Flip(
      horizontal = false,
      vertical = false
    )
  )
}
case class Effects(alpha: Double, tint: Tint, flip: Flip)
case class Tint(r: Double, g: Double, b: Double)
case class Flip(horizontal: Boolean, vertical: Boolean)
