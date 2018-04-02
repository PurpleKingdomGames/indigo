package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.assets.AnimationStates

case class SceneGraphLayer(nodes: List[SceneGraphNode]) extends AnyVal {

  def flatten: SceneGraphLayerFlat =
    SceneGraphLayerFlat(nodes.flatMap(_.flatten))

}


case class SceneGraphLayerFlat(nodes: List[Renderable]) extends AnyVal {

  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLayerFlat =
    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))

  def runAnimationActions(gameTime: GameTime): SceneGraphLayerFlat =
    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s}

}