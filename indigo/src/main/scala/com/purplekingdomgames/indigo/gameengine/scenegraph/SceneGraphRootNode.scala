package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, ViewEvent}
import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameTime}
import com.purplekingdomgames.indigo.util.metrics._

case class SceneGraphRootNode(game: SceneGraphLayer, lighting: SceneGraphLayer, ui: SceneGraphLayer) {

  private[gameengine] def flatten: SceneGraphRootNodeFlat =
    SceneGraphRootNodeFlat(
      game.flatten,
      lighting.flatten,
      ui.flatten
    )

  def addLightingLayer(lighting: SceneGraphLayer): SceneGraphRootNode =
    this.copy(lighting = lighting)

  def addUiLayer(ui: SceneGraphLayer): SceneGraphRootNode =
    this.copy(ui = ui)

}

object SceneGraphRootNode {
  def apply(game: SceneGraphLayer): SceneGraphRootNode =
    SceneGraphRootNode(game, SceneGraphLayer(Nil), SceneGraphLayer(Nil))

  def empty: SceneGraphRootNode =
    SceneGraphRootNode(SceneGraphLayer(Nil), SceneGraphLayer(Nil), SceneGraphLayer(Nil))

  def fromFragment(sceneUpdateFragment: SceneUpdateFragment): SceneGraphRootNode = {
    SceneGraphRootNode(
      SceneGraphLayer(sceneUpdateFragment.gameLayer),
      SceneGraphLayer(sceneUpdateFragment.lightingLayer),
      SceneGraphLayer(sceneUpdateFragment.uiLayer)
    )
  }
}

case class SceneGraphRootNodeFlat(game: SceneGraphLayerFlat, lighting: SceneGraphLayerFlat, ui: SceneGraphLayerFlat) {

  def applyAnimationMemento(animationStates: AnimationStates)(implicit metrics: IMetrics): SceneGraphRootNodeFlat = {

    metrics.record(ApplyAnimationMementoStartMetric)

    val res = SceneGraphRootNodeFlat(
      game.applyAnimationMemento(animationStates),
      lighting.applyAnimationMemento(animationStates),
      ui.applyAnimationMemento(animationStates)
    )

    metrics.record(ApplyAnimationMementoEndMetric)

    res
  }

  def runAnimationActions(gameTime: GameTime)(implicit metrics: IMetrics): SceneGraphRootNodeFlat = {

    metrics.record(RunAnimationActionsStartMetric)

    val res = SceneGraphRootNodeFlat(
      game.runAnimationActions(gameTime),
      lighting.runAnimationActions(gameTime),
      ui.runAnimationActions(gameTime)
    )

    metrics.record(RunAnimationActionsEndMetric)

    res
  }

  def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    game.collectViewEvents(gameEvents) ++
      lighting.collectViewEvents(gameEvents) ++
      ui.collectViewEvents(gameEvents)

}