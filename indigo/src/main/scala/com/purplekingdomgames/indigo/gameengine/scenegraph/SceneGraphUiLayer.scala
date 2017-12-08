package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}

case class SceneGraphUiLayer[ViewEventDataType](node: SceneGraphNode[ViewEventDataType]) {

  private[gameengine] def flatten: SceneGraphUiLayerFlat[ViewEventDataType] =
    SceneGraphUiLayerFlat[ViewEventDataType](node.flatten)

  def addChild(child: SceneGraphNode[ViewEventDataType]): SceneGraphUiLayer[ViewEventDataType] =
    node match {
      case l: SceneGraphNodeLeaf[ViewEventDataType] =>
        SceneGraphUiLayer[ViewEventDataType](SceneGraphNodeBranch[ViewEventDataType](l, child))

      case b: SceneGraphNodeBranch[ViewEventDataType] =>
        SceneGraphUiLayer[ViewEventDataType](b.addChild(child))
    }

  def addChildren(children: List[SceneGraphNode[ViewEventDataType]]): SceneGraphUiLayer[ViewEventDataType] =
    node match {
      case l: SceneGraphNodeLeaf[ViewEventDataType] =>
        SceneGraphUiLayer[ViewEventDataType](SceneGraphNodeBranch[ViewEventDataType](l :: children))

      case b: SceneGraphNodeBranch[ViewEventDataType] =>
        SceneGraphUiLayer[ViewEventDataType](b.addChildren(children))
    }

}

object SceneGraphUiLayer {
  def empty[ViewEventDataType]: SceneGraphUiLayer[ViewEventDataType] =
    SceneGraphUiLayer(SceneGraphNode.empty[ViewEventDataType])

  def apply[ViewEventDataType](nodes: SceneGraphNode[ViewEventDataType]*): SceneGraphUiLayer[ViewEventDataType] =
    SceneGraphUiLayer(
      SceneGraphNodeBranch(nodes.toList)
    )
}

case class SceneGraphUiLayerFlat[ViewEventDataType](nodes: List[SceneGraphNodeLeaf[ViewEventDataType]]) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphUiLayerFlat[ViewEventDataType] =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphUiLayerFlat[ViewEventDataType] =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}