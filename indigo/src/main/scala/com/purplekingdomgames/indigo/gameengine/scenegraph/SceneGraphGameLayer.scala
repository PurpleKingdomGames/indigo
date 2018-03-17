package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameTime}

case class SceneGraphGameLayer(node: SceneGraphNode) {

  private[gameengine] def flatten: SceneGraphGameLayerFlat =
    SceneGraphGameLayerFlat(node.flatten)

  def addChild(child: SceneGraphNode): SceneGraphGameLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphGameLayer(SceneGraphNodeBranch(l, child))

      case b: SceneGraphNodeBranch =>
        SceneGraphGameLayer(b.addChild(child))
    }

  def addChildren(children: List[SceneGraphNode]): SceneGraphGameLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphGameLayer(SceneGraphNodeBranch(l :: children))

      case b: SceneGraphNodeBranch =>
        SceneGraphGameLayer(b.addChildren(children))
    }

}

object SceneGraphGameLayer {

  def empty: SceneGraphGameLayer =
    SceneGraphGameLayer(SceneGraphNode.empty)

  def apply(nodes: SceneGraphNode*): SceneGraphGameLayer =
    SceneGraphGameLayer(
      SceneGraphNodeBranch(nodes.toList)
    )

  def apply(nodes: List[SceneGraphNode]): SceneGraphGameLayer =
    SceneGraphGameLayer(
      SceneGraphNodeBranch(nodes)
    )

}

case class SceneGraphGameLayerFlat(nodes: List[SceneGraphNodeLeaf]) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphGameLayerFlat =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphGameLayerFlat =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}