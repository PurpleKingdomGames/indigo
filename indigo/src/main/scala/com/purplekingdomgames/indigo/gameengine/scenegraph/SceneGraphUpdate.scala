package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.ViewEvent

case class SceneGraphUpdate(rootNode: SceneGraphRootNode, viewEvents: List[ViewEvent])

object SceneGraphUpdate {
  def skip: SceneGraphUpdate = SceneGraphUpdate(SceneGraphRootNode.empty, Nil)

  def apply(rootNode: SceneGraphRootNode): SceneGraphUpdate =
    SceneGraphUpdate(rootNode, Nil)

  def apply(nodes: SceneGraphNode*): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(SceneGraphGameLayer(nodes.toList)), Nil)

  def apply(nodes: List[SceneGraphNode], viewEvents: List[ViewEvent]): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(SceneGraphGameLayer(nodes)), viewEvents)

  def apply(gameLayer: SceneGraphGameLayer, viewEvents: List[ViewEvent]): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(gameLayer), viewEvents)

  def apply(gameLayer: SceneGraphGameLayer, lightingLayer: SceneGraphLightingLayer, uiLayer: SceneGraphUiLayer, viewEvents: List[ViewEvent]): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(gameLayer, lightingLayer, uiLayer), viewEvents)

}

case class SceneUpdate(layers: SceneLayers, viewEvents: List[ViewEvent], audio: SceneAudio) {
  def +(other: SceneUpdate): SceneUpdate =
    SceneUpdate.append(this, other)

  def toSceneGraphUpdate: SceneGraphUpdate =
    SceneGraphUpdate(
      SceneGraphRootNode(
        SceneGraphGameLayer(layers.game),
        SceneGraphLightingLayer(layers.lighting),
        SceneGraphUiLayer(layers.ui)
      ),
      viewEvents
    )
}
object SceneUpdate {
  def empty: SceneUpdate =
    SceneUpdate(SceneLayers.empty, Nil, SceneAudio.Silent)

  def append(a: SceneUpdate, b: SceneUpdate): SceneUpdate =
    SceneUpdate(
      a.layers + b.layers,
      a.viewEvents ++ b.viewEvents,
      SceneAudio.Silent
    )
}

case class SceneLayers(game: List[SceneGraphNode], lighting: List[SceneGraphNode], ui: List[SceneGraphNode]) {
  def +(other: SceneLayers): SceneLayers =
    SceneLayers.append(this, other)
}
object SceneLayers {
  def empty: SceneLayers =
    SceneLayers(Nil, Nil, Nil)

  def append(a: SceneLayers, b: SceneLayers): SceneLayers =
    SceneLayers(a.game ++ b.game, a.lighting ++ b.lighting, a.ui ++ b.ui)
}

case class SceneAudio()
object SceneAudio {
  val Silent: SceneAudio =
    SceneAudio()
}