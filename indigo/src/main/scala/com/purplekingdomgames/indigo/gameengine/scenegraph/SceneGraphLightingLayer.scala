package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameTime}
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AmbientLight, Tint}

case class SceneGraphLightingLayer(node: SceneGraphNodeBranch, ambientLight: AmbientLight) {

  private[gameengine] def flatten: SceneGraphLightingLayerFlat =
    SceneGraphLightingLayerFlat(node.flatten, ambientLight)

  def withAmbientLight(ambientLight: AmbientLight): SceneGraphLightingLayer = {
    this.copy(
      ambientLight = ambientLight
    )
  }
  def withAmbientLightAmount(amount: Double): SceneGraphLightingLayer = {
    this.copy(
      ambientLight = this.ambientLight.copy(
        amount = amount
      )
    )
  }
  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneGraphLightingLayer = {
    this.copy(
      ambientLight = this.ambientLight.copy(
        tint = Tint(r, g, b)
      )
    )
  }

  def addChild(child: SceneGraphNode): SceneGraphLightingLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphLightingLayer(SceneGraphNodeBranch(l, child), ambientLight)

      case b: SceneGraphNodeBranch =>
        SceneGraphLightingLayer(b.addChild(child), ambientLight)
    }

  def addChildren(children: List[SceneGraphNode]): SceneGraphLightingLayer =
    node match {
      case l: SceneGraphNodeLeaf =>
        SceneGraphLightingLayer(SceneGraphNodeBranch(l :: children), ambientLight)

      case b: SceneGraphNodeBranch =>
        SceneGraphLightingLayer(b.addChildren(children), ambientLight)
    }

}

object SceneGraphLightingLayer {
  def empty: SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      SceneGraphNode.empty,
      AmbientLight.None
    )

  def apply(nodes: SceneGraphNode*): SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      SceneGraphNodeBranch(nodes.toList),
      AmbientLight.None
    )

  def apply(nodes: List[SceneGraphNode]): SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      SceneGraphNodeBranch(nodes),
      AmbientLight.None
    )

  def apply(nodes: List[SceneGraphNode], ambientLight: AmbientLight): SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      SceneGraphNodeBranch(nodes),
      ambientLight
    )
}

case class SceneGraphLightingLayerFlat(nodes: List[SceneGraphNodeLeaf], ambientLight: AmbientLight) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLightingLayerFlat =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  private[gameengine] def runAnimationActions(gameTime: GameTime): SceneGraphLightingLayerFlat =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}
