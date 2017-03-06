package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameTime}

object SceneGraphInternal {

  private def convertCycleToInternal(cycle: Cycle): CycleInternal =
    CycleInternal(
      label = cycle.label,
      nonEmtpyFrames = cycle.frame :: cycle.frames,
      playheadPosition = 0,
      lastFrameAdvance = 0
    )

  private def convertAnimationsToInternal(animations: Animations): AnimationsInternal = {
    AnimationsInternal(
      spriteSheetSize = animations.spriteSheetSize,
      currentCycleLabel = animations.currentCycleLabel,
      nonEmtpyCycles = (Map(animations.cycle.label -> animations.cycle) ++ animations.cycles).map(p => p._1 -> convertCycleToInternal(p._2)),
      actions = animations.actions
    )
  }

  private def convertLeafNode(leaf: SceneGraphNodeLeaf): SceneGraphNodeLeafInternal =
    leaf match {
      case Graphic(bounds, depth, imageAssetRef, ref, crop, effects) =>
        GraphicInternal(bounds, depth, imageAssetRef, ref, crop, effects)

      case Text(text, alignment, position, depth, fontInfo, effects) =>
        TextInternal(text, alignment, position, depth, fontInfo, effects)

      case Sprite(bindingKey, bounds, depth, imageAssetRef, animations, ref, effects) =>
        SpriteInternal(bindingKey, bounds, depth, imageAssetRef, convertAnimationsToInternal(animations), ref, effects)

    }

  private def convertChildren(children: List[SceneGraphNode]): List[SceneGraphNodeInternal] =
    children.map(convertChild)

  private def convertChild(sceneGraphNode: SceneGraphNode): SceneGraphNodeInternal =
    sceneGraphNode match {
      case SceneGraphNodeBranch(children) =>
        SceneGraphNodeBranchInternal(
          convertChildren(children)
        )

      case l: SceneGraphNodeLeaf =>
        convertLeafNode(l)
    }

  def fromPublicFacing(sceneGraphNode: SceneGraphNode): SceneGraphNodeInternal =
    convertChild(sceneGraphNode)

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
