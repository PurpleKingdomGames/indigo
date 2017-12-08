package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AmbientLight, Tint}

case class SceneGraphLightingLayer[ViewEventDataType](node: SceneGraphNodeBranch[ViewEventDataType], ambientLight: AmbientLight) {

  private[gameengine] def flatten: SceneGraphLightingLayerFlat[ViewEventDataType] =
    SceneGraphLightingLayerFlat[ViewEventDataType](node.flatten, ambientLight)

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

  def addChild(child: SceneGraphNode[ViewEventDataType]): SceneGraphLightingLayer[ViewEventDataType] =
    node match {
      case l: SceneGraphNodeLeaf[ViewEventDataType] =>
        SceneGraphLightingLayer[ViewEventDataType](SceneGraphNodeBranch[ViewEventDataType](l, child), ambientLight)

      case b: SceneGraphNodeBranch[ViewEventDataType] =>
        SceneGraphLightingLayer[ViewEventDataType](b.addChild(child), ambientLight)
    }

  def addChildren(children: List[SceneGraphNode[ViewEventDataType]]): SceneGraphLightingLayer[ViewEventDataType] =
    node match {
      case l: SceneGraphNodeLeaf[ViewEventDataType] =>
        SceneGraphLightingLayer[ViewEventDataType](SceneGraphNodeBranch[ViewEventDataType](l :: children), ambientLight)

      case b: SceneGraphNodeBranch[ViewEventDataType] =>
        SceneGraphLightingLayer[ViewEventDataType](b.addChildren(children), ambientLight)
    }

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

case class SceneGraphLightingLayerFlat[ViewEventDataType](nodes: List[SceneGraphNodeLeaf[ViewEventDataType]], ambientLight: AmbientLight) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLightingLayerFlat[ViewEventDataType] =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphLightingLayerFlat[ViewEventDataType] =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}
