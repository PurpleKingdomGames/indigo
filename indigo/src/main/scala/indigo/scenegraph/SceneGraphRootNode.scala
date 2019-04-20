package indigo.scenegraph

import indigo.shared.events.GlobalEvent

final case class SceneGraphRootNode(game: SceneGraphLayer, lighting: SceneGraphLayer, ui: SceneGraphLayer) {

  def flatten: SceneGraphRootNodeFlat =
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

final case class SceneGraphRootNodeFlat(game: SceneGraphLayerFlat, lighting: SceneGraphLayerFlat, ui: SceneGraphLayerFlat) {

  def collectViewEvents(gameEvents: List[GlobalEvent]): List[GlobalEvent] =
    game.collectViewEvents(gameEvents) ++
      lighting.collectViewEvents(gameEvents) ++
      ui.collectViewEvents(gameEvents)

}
