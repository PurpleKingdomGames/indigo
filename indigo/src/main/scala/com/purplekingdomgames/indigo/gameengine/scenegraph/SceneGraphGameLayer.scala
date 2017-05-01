package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}

case class SceneGraphGameLayer[ViewEventDataType](node: SceneGraphNode[ViewEventDataType]) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphGameLayer[ViewEventDataType] =
    this.copy(node = node.applyAnimationMemento(animationStates))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphGameLayer[ViewEventDataType] =
    this.copy(node = node.runAnimationActions(gameTime))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    node.flatten.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}

object SceneGraphGameLayer {

  def empty[ViewEventDataType]: SceneGraphGameLayer[ViewEventDataType] =
    SceneGraphGameLayer[ViewEventDataType](SceneGraphNode.empty[ViewEventDataType])

  def apply[ViewEventDataType](nodes: SceneGraphNode[ViewEventDataType]*): SceneGraphGameLayer[ViewEventDataType] =
    SceneGraphGameLayer(
      SceneGraphNodeBranch[ViewEventDataType](nodes.toList)
    )

}