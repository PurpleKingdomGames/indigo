package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.AmbientLight

case class SceneUpdateFragment(gameLayer: List[SceneGraphNode], lightingLayer: List[SceneGraphNode], uiLayer: List[SceneGraphNode], ambientLight: AmbientLight, viewEvents: List[ViewEvent], audio: SceneAudio) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def toSceneGraphUpdate: SceneGraphUpdate =
    SceneGraphUpdate(
      SceneGraphRootNode(
        SceneGraphGameLayer(gameLayer),
        SceneGraphLightingLayer(lightingLayer, ambientLight),
        SceneGraphUiLayer(uiLayer)
      ),
      viewEvents
    )

  def addGameLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    this.copy(gameLayer = gameLayer ++ nodes.toList)

  def addGameLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(gameLayer = gameLayer ++ nodes)

  def addLightingLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    this.copy(lightingLayer = lightingLayer ++ nodes.toList)

  def addLightingLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(lightingLayer = lightingLayer ++ nodes)

  def addUiLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    this.copy(uiLayer = uiLayer ++ nodes.toList)

  def addUiLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(uiLayer = uiLayer ++ nodes)

  def withAmbientLight(light: AmbientLight): SceneUpdateFragment =
    this.copy(ambientLight = light)

  def addViewEvents(events: ViewEvent*): SceneUpdateFragment =
    this.copy(viewEvents = viewEvents ++ events.toList)

  def addViewEvents(events: List[ViewEvent]): SceneUpdateFragment =
    this.copy(viewEvents = viewEvents ++ events)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    this.copy(audio = sceneAudio)
}
object SceneUpdateFragment {

  def apply(): SceneUpdateFragment =
    empty

  def empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, Nil, AmbientLight.None, Nil, SceneAudio.Silent)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.gameLayer ++ b.gameLayer,
      a.lightingLayer ++ b.lightingLayer,
      a.uiLayer ++ b.uiLayer,
      a.ambientLight + b.ambientLight,
      a.viewEvents ++ b.viewEvents,
      SceneAudio.Silent
    )
}

case class SceneAudio()
object SceneAudio {
  val Silent: SceneAudio =
    SceneAudio()
}
