package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameTime}

case class SceneGraphUiLayer(node: SceneGraphNode) {

  private[gameengine] def flatten: SceneGraphUiLayerFlat =
    SceneGraphUiLayerFlat(node.flatten)

  def addChild(child: SceneGraphNode): SceneGraphUiLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphUiLayer(SceneGraphNodeBranch(l, child))

      case b: SceneGraphNodeBranch =>
        SceneGraphUiLayer(b.addChild(child))
    }

  def addChildren(children: List[SceneGraphNode]): SceneGraphUiLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphUiLayer(SceneGraphNodeBranch(l :: children))

      case b: SceneGraphNodeBranch =>
        SceneGraphUiLayer(b.addChildren(children))
    }

}

object SceneGraphUiLayer {
  def empty: SceneGraphUiLayer =
    SceneGraphUiLayer(SceneGraphNode.empty)

  def apply(nodes: SceneGraphNode*): SceneGraphUiLayer =
    SceneGraphUiLayer(
      SceneGraphNodeBranch(nodes.toList)
    )

  def apply(nodes: List[SceneGraphNode]): SceneGraphUiLayer =
    SceneGraphUiLayer(
      SceneGraphNodeBranch(nodes)
    )
}

case class SceneGraphUiLayerFlat(nodes: List[SceneGraphNodeLeaf]) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphUiLayerFlat =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphUiLayerFlat =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}