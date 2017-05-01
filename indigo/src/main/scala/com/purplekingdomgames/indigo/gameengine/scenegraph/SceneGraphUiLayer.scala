package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}

case class SceneGraphUiLayer[ViewEventDataType](node: SceneGraphNode[ViewEventDataType]) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphUiLayer[ViewEventDataType] =
    this.copy(node = node.applyAnimationMemento(animationStates))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphUiLayer[ViewEventDataType] =
    this.copy(node = node.runAnimationActions(gameTime))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    node.flatten.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}

object SceneGraphUiLayer {
  def empty[ViewEventDataType]: SceneGraphUiLayer[ViewEventDataType] =
    SceneGraphUiLayer(SceneGraphNode.empty[ViewEventDataType])

  def apply[ViewEventDataType](nodes: SceneGraphNode[ViewEventDataType]*): SceneGraphUiLayer[ViewEventDataType] =
    SceneGraphUiLayer(
      SceneGraphNodeBranch(nodes.toList)
    )
}