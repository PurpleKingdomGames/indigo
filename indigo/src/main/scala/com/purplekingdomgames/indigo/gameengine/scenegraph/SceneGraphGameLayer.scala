package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}

case class SceneGraphGameLayer[ViewEventDataType](node: SceneGraphNode[ViewEventDataType]) {

  private[gameengine] def flatten: SceneGraphGameLayerFlat[ViewEventDataType] =
    SceneGraphGameLayerFlat[ViewEventDataType](node.flatten)

}

object SceneGraphGameLayer {

  def empty[ViewEventDataType]: SceneGraphGameLayer[ViewEventDataType] =
    SceneGraphGameLayer[ViewEventDataType](SceneGraphNode.empty[ViewEventDataType])

  def apply[ViewEventDataType](nodes: SceneGraphNode[ViewEventDataType]*): SceneGraphGameLayer[ViewEventDataType] =
    SceneGraphGameLayer(
      SceneGraphNodeBranch[ViewEventDataType](nodes.toList)
    )

  def apply[ViewEventDataType](nodes: List[SceneGraphNode[ViewEventDataType]]): SceneGraphGameLayer[ViewEventDataType] =
    SceneGraphGameLayer(
      SceneGraphNodeBranch[ViewEventDataType](nodes)
    )

}

case class SceneGraphGameLayerFlat[ViewEventDataType](nodes: List[SceneGraphNodeLeaf[ViewEventDataType]]) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphGameLayerFlat[ViewEventDataType] =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphGameLayerFlat[ViewEventDataType] =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}