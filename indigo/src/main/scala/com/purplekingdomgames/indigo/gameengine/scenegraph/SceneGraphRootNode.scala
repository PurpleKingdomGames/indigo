package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.{GameEvent, ViewEvent}

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

  def fromFragment(sceneUpdateFragment: SceneUpdateFragment): SceneGraphRootNode =
    SceneGraphRootNode(
      SceneGraphLayer(sceneUpdateFragment.gameLayer),
      SceneGraphLayer(sceneUpdateFragment.lightingLayer),
      SceneGraphLayer(sceneUpdateFragment.uiLayer)
    )
}

case class SceneGraphRootNodeFlat(game: SceneGraphLayerFlat, lighting: SceneGraphLayerFlat, ui: SceneGraphLayerFlat) {

//  def applyAnimationMemento(animationStates: AnimationStates)(implicit metrics: IMetrics): SceneGraphRootNodeFlat = {
//
//    metrics.record(ApplyAnimationMementoStartMetric)
//
//    //TODO:!!!
//    /*
//    So what actually happens here, in the perf example, which is a worst case to be fair:
//    is first we call applyAnimationMemento on 10,000 guys.
//    Which is 10,000 object copies, each with 10,000 sub object copies.
//    Then we call runAnimationActions which does - you guessed it - 10,000 object copies.
//    Then we save 10,000 identical animation mementos.
//
//    So actions:
//    DONE: 1. On save, cache saved mementos by key and don't save if you've already done the work
//    2. Merge apply and run into a single copy operation
//    3. Do we need to do a copy at all? Could we extract the animation (and cache by binding key),
//       apply the memento and then only re-apply during conversion to display object?
//     */
//
//    val res = SceneGraphRootNodeFlat(
//      game.applyAnimationMemento(animationStates),
//      lighting.applyAnimationMemento(animationStates),
//      ui.applyAnimationMemento(animationStates)
//    )
//
//    metrics.record(ApplyAnimationMementoEndMetric)
//
//    res
//  }

//  def runAnimationActions(gameTime: GameTime)(implicit metrics: IMetrics): SceneGraphRootNodeFlat = {
//
//    metrics.record(RunAnimationActionsStartMetric)
//
//    val res = SceneGraphRootNodeFlat(
//      game.runAnimationActions(gameTime),
//      lighting.runAnimationActions(gameTime),
//      ui.runAnimationActions(gameTime)
//    )
//
//    metrics.record(RunAnimationActionsEndMetric)
//
//    res
//  }

  def collectViewEvents(gameEvents: List[GameEvent]): List[ViewEvent] =
    game.collectViewEvents(gameEvents) ++
      lighting.collectViewEvents(gameEvents) ++
      ui.collectViewEvents(gameEvents)

}
