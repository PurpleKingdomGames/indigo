package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AmbientLight, Tint}

case class SceneGraphLightingLayer[ViewEventDataType](node: SceneGraphNodeBranch[ViewEventDataType], ambientLight: AmbientLight) {

  def withAmbientLight(ambientLight: AmbientLight): SceneGraphLightingLayer[ViewEventDataType] = {
    this.copy(
      ambientLight = ambientLight
    )
  }
  def withAmbientLightAmount(amount: Double): SceneGraphLightingLayer[ViewEventDataType] = {
    this.copy(
      ambientLight = this.ambientLight.copy(
        amount = amount
      )
    )
  }
  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneGraphLightingLayer[ViewEventDataType] = {
    this.copy(
      ambientLight = this.ambientLight.copy(
        tint = Tint(r, g, b)
      )
    )
  }

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLightingLayer[ViewEventDataType] =
    this.copy(node = node.applyAnimationMemento(animationStates))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphLightingLayer[ViewEventDataType] =
    this.copy(node = node.runAnimationActions(gameTime))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    node.flatten.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}

object SceneGraphLightingLayer {
  def empty[ViewEventDataType]: SceneGraphLightingLayer[ViewEventDataType] =
    SceneGraphLightingLayer(
      SceneGraphNode.empty[ViewEventDataType],
      AmbientLight.none
    )

  def apply[ViewEventDataType](nodes: SceneGraphNodeLeaf[ViewEventDataType]*): SceneGraphLightingLayer[ViewEventDataType] =
    SceneGraphLightingLayer(
      SceneGraphNodeBranch[ViewEventDataType](nodes.toList),
      AmbientLight.none
    )
}
