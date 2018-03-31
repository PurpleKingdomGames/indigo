package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameTime}

case class SceneGraphLayer(node: SceneGraphNode) {

  private[gameengine] def flatten: SceneGraphLayerFlat =
    SceneGraphLayerFlat(node.flatten)

  def addChild(child: SceneGraphNode): SceneGraphLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphLayer(SceneGraphNodeBranch(l, child))

      case b: SceneGraphNodeBranch =>
        SceneGraphLayer(b.addChild(child))
    }

  def addChildren(children: List[SceneGraphNode]): SceneGraphLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphLayer(SceneGraphNodeBranch(l :: children))

      case b: SceneGraphNodeBranch =>
        SceneGraphLayer(b.addChildren(children))
    }

}

object SceneGraphLayer {

  def empty: SceneGraphLayer =
    SceneGraphLayer(SceneGraphNode.empty)

  def apply(nodes: SceneGraphNode*): SceneGraphLayer =
    SceneGraphLayer(
      SceneGraphNodeBranch(nodes.toList)
    )

  def apply(nodes: List[SceneGraphNode]): SceneGraphLayer =
    SceneGraphLayer(
      SceneGraphNodeBranch(nodes)
    )

}

case class SceneGraphLayerFlat(nodes: List[SceneGraphNodeLeaf]) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLayerFlat =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphLayerFlat =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}