package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.ViewEvent

case class SceneGraphUpdate(rootNode: SceneGraphRootNode, viewEvents: List[ViewEvent], sceneAudio: SceneAudio)

object SceneGraphUpdate {
  def skip: SceneGraphUpdate = SceneGraphUpdate(SceneGraphRootNode.empty, Nil, SceneAudio.None)

  def apply(rootNode: SceneGraphRootNode): SceneGraphUpdate =
    SceneGraphUpdate(rootNode, Nil, SceneAudio.None)

  def apply(nodes: SceneGraphNode*): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(SceneGraphGameLayer(nodes.toList)), Nil, SceneAudio.None)

  def apply(nodes: List[SceneGraphNode], viewEvents: List[ViewEvent]): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(SceneGraphGameLayer(nodes)), viewEvents, SceneAudio.None)

  def apply(gameLayer: SceneGraphGameLayer, viewEvents: List[ViewEvent]): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(gameLayer), viewEvents, SceneAudio.None)

  def apply(gameLayer: SceneGraphGameLayer, lightingLayer: SceneGraphLightingLayer, uiLayer: SceneGraphUiLayer, viewEvents: List[ViewEvent], sceneAudio: SceneAudio): SceneGraphUpdate =
    SceneGraphUpdate(SceneGraphRootNode(gameLayer, lightingLayer, uiLayer), viewEvents, sceneAudio)

}