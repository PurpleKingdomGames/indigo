package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent

final class SceneGraphRootNode(val game: SceneGraphLayer, val lighting: SceneGraphLayer, val ui: SceneGraphLayer) {

  def flatten: SceneGraphRootNodeFlat =
    new SceneGraphRootNodeFlat(
      game.flatten,
      lighting.flatten,
      ui.flatten
    )

  def addLightingLayer(lightingLayer: SceneGraphLayer): SceneGraphRootNode =
    new SceneGraphRootNode(game, lightingLayer, ui)

  def addUiLayer(uiLayer: SceneGraphLayer): SceneGraphRootNode =
    new SceneGraphRootNode(game, lighting, uiLayer)

}

object SceneGraphRootNode {
  def apply(game: SceneGraphLayer, lighting: SceneGraphLayer, ui: SceneGraphLayer): SceneGraphRootNode =
    new SceneGraphRootNode(game, lighting, ui)

  def empty: SceneGraphRootNode =
    SceneGraphRootNode(SceneGraphLayer.empty, SceneGraphLayer.empty, SceneGraphLayer.empty)

  def fromFragment(sceneUpdateFragment: SceneUpdateFragment): SceneGraphRootNode =
    SceneGraphRootNode(
      new SceneGraphLayer(sceneUpdateFragment.gameLayer),
      new SceneGraphLayer(sceneUpdateFragment.lightingLayer),
      new SceneGraphLayer(sceneUpdateFragment.uiLayer)
    )
}

final class SceneGraphRootNodeFlat(val game: SceneGraphLayerFlat, val lighting: SceneGraphLayerFlat, val ui: SceneGraphLayerFlat) {

  def collectViewEvents(gameEvents: List[GlobalEvent]): List[GlobalEvent] =
    game.collectViewEvents(gameEvents) ++
      lighting.collectViewEvents(gameEvents) ++
      ui.collectViewEvents(gameEvents)

}
