package com.purplekingdomgames.indigo.gameengine

import scala.language.implicitConversions

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

}

// Types of SceneGraphNode
case class SceneGraphNodeBranch(children: List[SceneGraphNode]) extends SceneGraphNode
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

// Data types
case class Point(x: Int, y: Int)
case class Rectangle(position: Point, size: Point)
case class Depth(zIndex: Int)

object Point {
  val identity: Point = Point(0, 0)
  implicit def tuple2ToPoint(t: (Int, Int)): Point = Point(t._1, t._2)
}

object Depth {
  implicit def intToDepth(i: Int): Depth = Depth(i)
}

object Rectangle {
  def apply(x: Int, y: Int, width: Int, height: Int): Rectangle = Rectangle(Point(x, y), Point(width, height))
  implicit def tuple4ToRectangle(t: (Int, Int, Int, Int)): Rectangle = Rectangle(t._1, t._2, t._3, t._4)
}

// Frames
case class Animations(spriteSheetSize: Point, cycle: Cycle, cycles: List[Cycle]) {
  private val nonEmtpyCycles: List[Cycle] = cycle +: cycles

  def currentCycle: Cycle =
    nonEmtpyCycles.find(_.current).getOrElse(nonEmtpyCycles.head)

  def currentCycleName: String = currentCycle.label

  def currentFrame: Frame = currentCycle.currentFrame

  def addCycle(cycle: Cycle) = Animations(spriteSheetSize, cycle, nonEmtpyCycles)

  def nextFrame: Animations = {
    this.copy(cycle = currentCycle.nextFrame(), cycles = nonEmtpyCycles.filterNot(_.current))
  }

}

case class Cycle(label: String, playheadPosition: Int, frame: Frame, frames: List[Frame], current: Boolean) {
  private val nonEmtpyFrames: List[Frame] = frame +: frames

  def currentFrame: Frame =
    nonEmtpyFrames.find(_.current).getOrElse(nonEmtpyFrames.head)

  def addFrame(frame: Frame) = Cycle(label, playheadPosition, frame, nonEmtpyFrames, current)

  def nextFrame(): Cycle = this.copy(playheadPosition = playheadPosition + 1 % nonEmtpyFrames.length)

}

case class Frame(bounds: Rectangle, current: Boolean)

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

}

case class Sprite(bounds: Rectangle, depth: Depth, imageAssetRef: String, animations: Animations, ref: Point, effects: Effects) extends SceneGraphNodeLeaf {

  val crop: Option[Rectangle] = None

  val x: Int = bounds.position.x - ref.x
  val y: Int = bounds.position.y - ref.y

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

  def nextFrame: Sprite = this.copy(animations = animations.nextFrame)

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
