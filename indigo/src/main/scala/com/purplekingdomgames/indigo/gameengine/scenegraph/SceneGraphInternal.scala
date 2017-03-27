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

      case t @ Text(text, alignment, position, depth, fontInfo, effects) =>
        TextInternal(text, t.lines, t.bounds, alignment, position, depth, fontInfo, effects)

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

  def fromPublicFacing(sceneGraphNode: SceneGraphRootNode): SceneGraphRootNodeInternal =
    SceneGraphRootNodeInternal(
      game = SceneGraphGameLayerInternal(
        convertChild(sceneGraphNode.game.node)
      ),
      lighting = SceneGraphLightingLayerInternal(
        convertChild(sceneGraphNode.lighting.node),
        sceneGraphNode.lighting.ambientLight
      ),
      ui = SceneGraphUiLayerInternal(
        convertChild(sceneGraphNode.ui.node)
      )
    )

}

/*
Rather than have an unknown number of layers of dubious value, for now... and I may regret this
later... lets just have some fixed layers with specific jobs. I can justify this on the grounds
that the engine has a specific purpose, it's not general.
Game - Simple diffuse colour layer
Lighting - More involved, at minimum it's another diffuse layer that's multiplied onto the game layer
Post processing screen effects can then be applied here to the combined game and lighting layers.
UI - Simple diffuse, but always lives above the other two.
 */
case class SceneGraphRootNodeInternal(game: SceneGraphGameLayerInternal, lighting: SceneGraphLightingLayerInternal, ui: SceneGraphUiLayerInternal) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphRootNodeInternal =
    SceneGraphRootNodeInternal(
      game.applyAnimationMemento(animationStates),
      lighting.applyAnimationMemento(animationStates),
      ui.applyAnimationMemento(animationStates)
    )

  def runAnimationActions(gameTime: GameTime): SceneGraphRootNodeInternal =
    SceneGraphRootNodeInternal(
      game.runAnimationActions(gameTime),
      lighting.runAnimationActions(gameTime),
      ui.runAnimationActions(gameTime)
    )

}

case class SceneGraphGameLayerInternal(node: SceneGraphNodeInternal) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphGameLayerInternal =
    this.copy(node = node.applyAnimationMemento(animationStates))

  def runAnimationActions(gameTime: GameTime): SceneGraphGameLayerInternal =
    this.copy(node = node.runAnimationActions(gameTime))

}
case class SceneGraphLightingLayerInternal(node: SceneGraphNodeInternal, ambientLight: AmbientLight) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLightingLayerInternal =
    this.copy(node = node.applyAnimationMemento(animationStates))

  def runAnimationActions(gameTime: GameTime): SceneGraphLightingLayerInternal =
    this.copy(node = node.runAnimationActions(gameTime))

}
case class SceneGraphUiLayerInternal(node: SceneGraphNodeInternal) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphUiLayerInternal =
    this.copy(node = node.applyAnimationMemento(animationStates))

  def runAnimationActions(gameTime: GameTime): SceneGraphUiLayerInternal =
    this.copy(node = node.runAnimationActions(gameTime))

}

sealed trait SceneGraphNodeInternal {

  def flatten: List[SceneGraphNodeLeafInternal] = {
    def rec(acc: List[SceneGraphNodeLeafInternal]): List[SceneGraphNodeLeafInternal] = {
      this match {
        case l: SceneGraphNodeLeafInternal => l :: acc
        case b: SceneGraphNodeBranchInternal =>
          b.children.flatMap(n => n.flatten) ++ acc
      }
    }

    rec(Nil)
  }

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal

}


// Types of SceneGraphNode
case class SceneGraphNodeBranchInternal(children: List[SceneGraphNodeInternal]) extends SceneGraphNodeInternal {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal =
    this.copy(children.map(_.applyAnimationMemento(animationStates)))

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal =
    this.copy(children.map(_.runAnimationActions(gameTime)))

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

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal = this
}

case class SpriteInternal(bindingKey: BindingKey, bounds: Rectangle, depth: Depth, imageAssetRef: String, animations: AnimationsInternal, ref: Point, effects: Effects) extends SceneGraphNodeLeafInternal {
  val crop: Rectangle = bounds

  def saveAnimationMemento: Option[AnimationMemento] = Option(animations.saveMemento(bindingKey))

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal =
    animationStates.withBindingKey(bindingKey) match {
      case Some(memento) => this.copy(animations = animations.applyMemento(memento))
      case None => this
    }

  def runAnimationActions(gameTime: GameTime): SpriteInternal = this.copy(animations = animations.runActions(gameTime))
}

case class TextInternal(text: String, lines: List[TextLine], bounds: Rectangle, alignment: TextAlignment, position: Point, depth: Depth, fontInfo: FontInfo, effects: Effects) extends SceneGraphNodeLeafInternal {

  // Handled a different way
  val ref: Point = Point(0, 0)
  val crop: Rectangle = bounds
  val imageAssetRef: String = fontInfo.fontSpriteSheet.imageAssetRef

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal = this

  def saveAnimationMemento: Option[AnimationMemento] = None

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal = this
}
