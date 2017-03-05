package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameTime}

object SceneGraphInternal {

  def fromPublicFacing(sceneGraphNode: SceneGraphNode): SceneGraphNodeInternal = {
    ???
  }

}

sealed trait SceneGraphNodeInternal {

  def flatten(acc: List[SceneGraphNodeLeafInternal]): List[SceneGraphNodeLeafInternal] = {
    this match {
      case l: SceneGraphNodeLeafInternal => l :: acc
      case b: SceneGraphNodeBranchInternal =>
        b.children.flatMap(n => n.flatten(Nil)) ++ acc
    }
  }

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal

}


// Types of SceneGraphNode
case class SceneGraphNodeBranchInternal(children: List[SceneGraphNodeInternal]) extends SceneGraphNodeInternal {
  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal =
    this.copy(children.map(_.applyAnimationMemento(animationStates)))
}
sealed trait SceneGraphNodeLeafInternal extends SceneGraphNodeInternal {
  val bounds: Rectangle
  val depth: Depth
  val imageAssetRef: String
  val effects: Effects
  val ref: Point
  val crop: Rectangle

  def x: Int = bounds.position.x - ref.x
  def y: Int = bounds.position.y - ref.y

  def saveAnimationMemento: Option[AnimationMemento]

}

// Concrete leaf types
case class GraphicInternal(bounds: Rectangle, depth: Depth, imageAssetRef: String, ref: Point, crop: Rectangle, effects: Effects) extends SceneGraphNodeLeafInternal {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal = this

  def saveAnimationMemento: Option[AnimationMemento] = None
}

case class SpriteInternal(bindingKey: BindingKey, bounds: Rectangle, depth: Depth, imageAssetRef: String, animations: AnimationsInternal, ref: Point, effects: Effects) extends SceneGraphNodeLeafInternal {
  val crop: Rectangle = bounds

  def saveAnimationMemento: Option[AnimationMemento] = Option(animations.saveMemento(bindingKey))

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal =
    animationStates.withBindingKey(bindingKey) match {
      case Some(memento) => this.copy(animations = animations.applyMemento(memento))
      case None => this
    }

  def runActions(gameTime: GameTime): SpriteInternal = this.copy(animations = animations.runActions(gameTime))
}

case class TextInternal(text: String, alignment: TextAlignment, position: Point, depth: Depth, fontInfo: FontInfo, effects: Effects) extends SceneGraphNodeLeafInternal {

  // Handled a different way
  val ref: Point = Point(0, 0)
  val bounds: Rectangle = Rectangle(position, Point(text.length * fontInfo.charSize.x, fontInfo.charSize.y))
  val crop: Rectangle = bounds
  val imageAssetRef: String = fontInfo.fontSpriteSheet.imageAssetRef

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal = this

  def saveAnimationMemento: Option[AnimationMemento] = None
}
