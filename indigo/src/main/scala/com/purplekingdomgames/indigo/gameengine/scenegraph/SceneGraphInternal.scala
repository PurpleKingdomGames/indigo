package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}

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

  private def convertLeafNode[VEDT](leaf: SceneGraphNodeLeaf[VEDT]): SceneGraphNodeLeafInternal[VEDT] =
    leaf match {
      case Graphic(bounds, depth, imageAssetRef, ref, crop, effects, eventHandler) =>
        GraphicInternal[VEDT](bounds, depth, imageAssetRef, ref, crop, effects, eventHandler)

      case t @ Text(text, alignment, position, depth, fontInfo, effects, eventHandler) =>
        TextInternal[VEDT](text, t.lines, t.bounds, alignment, position, depth, fontInfo, effects, eventHandler)

      case Sprite(bindingKey, bounds, depth, imageAssetRef, animations, ref, effects, eventHandler) =>
        SpriteInternal[VEDT](bindingKey, bounds, depth, imageAssetRef, convertAnimationsToInternal(animations), ref, effects, eventHandler)

    }

  private def convertChildren[VEDT](children: List[SceneGraphNode]): List[SceneGraphNodeInternal[VEDT]] =
    children.map(convertChild[VEDT])

  private def convertChild[VEDT](sceneGraphNode: SceneGraphNode): SceneGraphNodeInternal[VEDT] =
    sceneGraphNode match {
      case SceneGraphNodeBranch(children) =>
        SceneGraphNodeBranchInternal[VEDT](
          convertChildren(children)
        )

      case l: SceneGraphNodeLeaf[VEDT] =>
        convertLeafNode[VEDT](l)
    }

  def fromPublicFacing(sceneGraphNode: SceneGraphRootNode[_]): SceneGraphRootNodeInternal[sceneGraphNode.VEDT] =
    SceneGraphRootNodeInternal[sceneGraphNode.VEDT](
      game = SceneGraphGameLayerInternal[sceneGraphNode.VEDT](
        convertChild[sceneGraphNode.VEDT](sceneGraphNode.game.node)
      ),
      lighting = SceneGraphLightingLayerInternal[sceneGraphNode.VEDT](
        convertChild[sceneGraphNode.VEDT](sceneGraphNode.lighting.node),
        sceneGraphNode.lighting.ambientLight
      ),
      ui = SceneGraphUiLayerInternal[sceneGraphNode.VEDT](
        convertChild[sceneGraphNode.VEDT](sceneGraphNode.ui.node)
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
case class SceneGraphRootNodeInternal[ViewEventDataType](game: SceneGraphGameLayerInternal[ViewEventDataType], lighting: SceneGraphLightingLayerInternal[ViewEventDataType], ui: SceneGraphUiLayerInternal[ViewEventDataType]) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphRootNodeInternal[ViewEventDataType] =
    SceneGraphRootNodeInternal(
      game.applyAnimationMemento(animationStates),
      lighting.applyAnimationMemento(animationStates),
      ui.applyAnimationMemento(animationStates)
    )

  def runAnimationActions(gameTime: GameTime): SceneGraphRootNodeInternal[ViewEventDataType] =
    SceneGraphRootNodeInternal(
      game.runAnimationActions(gameTime),
      lighting.runAnimationActions(gameTime),
      ui.runAnimationActions(gameTime)
    )

  def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    game.collectViewEvents(gameEvents) ++
      lighting.collectViewEvents(gameEvents) ++
      ui.collectViewEvents(gameEvents)

}

case class SceneGraphGameLayerInternal[ViewEventDataType](node: SceneGraphNodeInternal[ViewEventDataType]) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphGameLayerInternal[ViewEventDataType] =
    this.copy(node = node.applyAnimationMemento(animationStates))

  def runAnimationActions(gameTime: GameTime): SceneGraphGameLayerInternal[ViewEventDataType] =
    this.copy(node = node.runAnimationActions(gameTime))

  def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    node.flatten.flatMap(n => gameEvents.map(e => n.eventHandler(e))).collect { case Some(s) => s}

}
case class SceneGraphLightingLayerInternal[ViewEventDataType](node: SceneGraphNodeInternal[ViewEventDataType], ambientLight: AmbientLight) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLightingLayerInternal[ViewEventDataType] =
    this.copy(node = node.applyAnimationMemento(animationStates))

  def runAnimationActions(gameTime: GameTime): SceneGraphLightingLayerInternal[ViewEventDataType] =
    this.copy(node = node.runAnimationActions(gameTime))

  def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] = Nil

}
case class SceneGraphUiLayerInternal[ViewEventDataType](node: SceneGraphNodeInternal[ViewEventDataType]) {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphUiLayerInternal[ViewEventDataType] =
    this.copy(node = node.applyAnimationMemento(animationStates))

  def runAnimationActions(gameTime: GameTime): SceneGraphUiLayerInternal[ViewEventDataType] =
    this.copy(node = node.runAnimationActions(gameTime))

  def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] = Nil

}

sealed trait SceneGraphNodeInternal[ViewEventDataType] {

  def flatten: List[SceneGraphNodeLeafInternal[ViewEventDataType]] = {
    def rec(acc: List[SceneGraphNodeLeafInternal[ViewEventDataType]]): List[SceneGraphNodeLeafInternal[ViewEventDataType]] = {
      this match {
        case l: SceneGraphNodeLeafInternal[ViewEventDataType] => l :: acc
        case b: SceneGraphNodeBranchInternal[ViewEventDataType] =>
          b.children.flatMap(n => n.flatten) ++ acc
      }
    }

    rec(Nil)
  }

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal[ViewEventDataType]

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal[ViewEventDataType]

}


// Types of SceneGraphNode
case class SceneGraphNodeBranchInternal[ViewEventDataType](children: List[SceneGraphNodeInternal[ViewEventDataType]]) extends SceneGraphNodeInternal[ViewEventDataType] {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal[ViewEventDataType] =
    this.copy(children.map(_.applyAnimationMemento(animationStates)))

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal[ViewEventDataType] =
    this.copy(children.map(_.runAnimationActions(gameTime)))

}
sealed trait SceneGraphNodeLeafInternal[ViewEventDataType] extends SceneGraphNodeInternal[ViewEventDataType] {
  val bounds: Rectangle
  val depth: Depth
  val imageAssetRef: String
  val effects: Effects
  val ref: Point
  val crop: Rectangle
  val eventHandler: GameEvent => Option[ViewEvent[ViewEventDataType]]

  def x: Int = bounds.position.x - ref.x
  def y: Int = bounds.position.y - ref.y

  def saveAnimationMemento: Option[AnimationMemento]
}

// Concrete leaf types
case class GraphicInternal[ViewEventDataType](bounds: Rectangle, depth: Depth, imageAssetRef: String, ref: Point, crop: Rectangle, effects: Effects, eventHandler: GameEvent => Option[ViewEvent[ViewEventDataType]]) extends SceneGraphNodeLeafInternal[ViewEventDataType] {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal[ViewEventDataType] = this

  def saveAnimationMemento: Option[AnimationMemento] = None

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal[ViewEventDataType] = this
}

case class SpriteInternal[ViewEventDataType](bindingKey: BindingKey, bounds: Rectangle, depth: Depth, imageAssetRef: String, animations: AnimationsInternal, ref: Point, effects: Effects, eventHandler: GameEvent => Option[ViewEvent[ViewEventDataType]]) extends SceneGraphNodeLeafInternal[ViewEventDataType] {
  val crop: Rectangle = bounds

  def saveAnimationMemento: Option[AnimationMemento] = Option(animations.saveMemento(bindingKey))

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal[ViewEventDataType] =
    animationStates.withBindingKey(bindingKey) match {
      case Some(memento) => this.copy(animations = animations.applyMemento(memento))
      case None => this
    }

  def runAnimationActions(gameTime: GameTime): SpriteInternal[ViewEventDataType] = this.copy(animations = animations.runActions(gameTime))
}

case class TextInternal[ViewEventDataType](text: String, lines: List[TextLine], bounds: Rectangle, alignment: TextAlignment, position: Point, depth: Depth, fontInfo: FontInfo, effects: Effects, eventHandler: GameEvent => Option[ViewEvent[ViewEventDataType]]) extends SceneGraphNodeLeafInternal[ViewEventDataType] {

  // Handled a different way
  val ref: Point = Point(0, 0)
  val crop: Rectangle = bounds
  val imageAssetRef: String = fontInfo.fontSpriteSheet.imageAssetRef

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphNodeInternal[ViewEventDataType] = this

  def saveAnimationMemento: Option[AnimationMemento] = None

  def runAnimationActions(gameTime: GameTime): SceneGraphNodeInternal[ViewEventDataType] = this
}
