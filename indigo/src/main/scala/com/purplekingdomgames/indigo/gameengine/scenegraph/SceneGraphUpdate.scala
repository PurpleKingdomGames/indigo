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
