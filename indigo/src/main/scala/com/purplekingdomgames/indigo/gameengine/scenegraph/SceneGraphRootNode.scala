package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.{AnimationStates, GameEvent, GameTime, ViewEvent}
import com.purplekingdomgames.indigo.util.metrics._

case class SceneGraphRootNode[ViewEventDataType](game: SceneGraphGameLayer[ViewEventDataType], lighting: SceneGraphLightingLayer[ViewEventDataType], ui: SceneGraphUiLayer[ViewEventDataType]) {

  def addLightingLayer(lighting: SceneGraphLightingLayer[ViewEventDataType]): SceneGraphRootNode[ViewEventDataType] =
    this.copy(lighting = lighting)

  def addUiLayer(ui: SceneGraphUiLayer[ViewEventDataType]): SceneGraphRootNode[ViewEventDataType] =
    this.copy(ui = ui)

  private[gameengine] def applyAnimationMemento(animationStates: AnimationStates)(implicit metrics: IMetrics): SceneGraphRootNode[ViewEventDataType] = {

    metrics.record(ApplyAnimationMementoStartMetric)

    val res = SceneGraphRootNode[ViewEventDataType](
      game.applyAnimationMemento(animationStates),
      lighting.applyAnimationMemento(animationStates),
      ui.applyAnimationMemento(animationStates)
    )

    metrics.record(ApplyAnimationMementoEndMetric)

    res
  }

  private[gameengine] def runAnimationActions(gameTime: GameTime)(implicit metrics: IMetrics): SceneGraphRootNode[ViewEventDataType] = {

    metrics.record(RunAnimationActionsStartMetric)

    val res = SceneGraphRootNode[ViewEventDataType](
      game.runAnimationActions(gameTime),
      lighting.runAnimationActions(gameTime),
      ui.runAnimationActions(gameTime)
    )

    metrics.record(RunAnimationActionsEndMetric)

    res
  }

  private[gameengine] def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent[ViewEventDataType]] =
    game.collectViewEvents(gameEvents) ++
      lighting.collectViewEvents(gameEvents) ++
      ui.collectViewEvents(gameEvents)

}

object SceneGraphRootNode {
  def apply[ViewEventDataType](game: SceneGraphGameLayer[ViewEventDataType]): SceneGraphRootNode[ViewEventDataType] =
    SceneGraphRootNode(game, SceneGraphLightingLayer.empty, SceneGraphUiLayer.empty)
}