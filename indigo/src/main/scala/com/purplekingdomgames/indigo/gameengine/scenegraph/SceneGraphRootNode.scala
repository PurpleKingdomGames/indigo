package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}
import com.purplekingdomgames.indigo.util.metrics._

case class SceneGraphRootNode(game: SceneGraphGameLayer, lighting: SceneGraphLightingLayer, ui: SceneGraphUiLayer) {

  private[gameengine] def flatten: SceneGraphRootNodeFlat =
    SceneGraphRootNodeFlat(
      game.flatten,
      lighting.flatten,
      ui.flatten
    )

  def addLightingLayer(lighting: SceneGraphLightingLayer): SceneGraphRootNode =
    this.copy(lighting = lighting)

  def addUiLayer(ui: SceneGraphUiLayer): SceneGraphRootNode =
    this.copy(ui = ui)

}

object SceneGraphRootNode {
  def apply(game: SceneGraphGameLayer): SceneGraphRootNode =
    SceneGraphRootNode(game, SceneGraphLightingLayer.empty, SceneGraphUiLayer.empty)

  def empty: SceneGraphRootNode =
    SceneGraphRootNode(SceneGraphGameLayer.empty, SceneGraphLightingLayer.empty, SceneGraphUiLayer.empty)
}

case class SceneGraphRootNodeFlat(game: SceneGraphGameLayerFlat, lighting: SceneGraphLightingLayerFlat, ui: SceneGraphUiLayerFlat) {

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates)(implicit metrics: IMetrics): SceneGraphRootNodeFlat = {

    metrics.record(ApplyAnimationMementoStartMetric)

    val res = SceneGraphRootNodeFlat(
      game.applyAnimationMemento(animationStates),
      lighting.applyAnimationMemento(animationStates),
      ui.applyAnimationMemento(animationStates)
    )

    metrics.record(ApplyAnimationMementoEndMetric)

    res
  }

  private[gameengine] def runAnimationActions(gameTime: GameTime)(implicit metrics: IMetrics): SceneGraphRootNodeFlat = {

    metrics.record(RunAnimationActionsStartMetric)

    val res = SceneGraphRootNodeFlat(
      game.runAnimationActions(gameTime),
      lighting.runAnimationActions(gameTime),
      ui.runAnimationActions(gameTime)
    )

    metrics.record(RunAnimationActionsEndMetric)

    res
  }

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    game.collectViewEvents(gameEvents) ++
      lighting.collectViewEvents(gameEvents) ++
      ui.collectViewEvents(gameEvents)

}