package indigo.shared.scenegraph

final class SceneGraphRootNode(val game: SceneGraphLayer, val lighting: SceneGraphLayer, val ui: SceneGraphLayer) {

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
