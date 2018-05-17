package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.events.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{AmbientLight, Tint}

case class SceneUpdateFragment(gameLayer: List[SceneGraphNode],
                               lightingLayer: List[SceneGraphNode],
                               uiLayer: List[SceneGraphNode],
                               ambientLight: AmbientLight,
                               viewEvents: List[ViewEvent],
                               audio: SceneAudio) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

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

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    this.copy(
      ambientLight = this.ambientLight.copy(
        amount = amount
      )
    )

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    this.copy(
      ambientLight = this.ambientLight.copy(
        tint = Tint(r, g, b)
      )
    )

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
    SceneUpdateFragment(Nil, Nil, Nil, AmbientLight.Normal, Nil, SceneAudio.None)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.gameLayer ++ b.gameLayer,
      a.lightingLayer ++ b.lightingLayer,
      a.uiLayer ++ b.uiLayer,
      a.ambientLight + b.ambientLight,
      a.viewEvents ++ b.viewEvents,
      SceneAudio.None
    )
}
