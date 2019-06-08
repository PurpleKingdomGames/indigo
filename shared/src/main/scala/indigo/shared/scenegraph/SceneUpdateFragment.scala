package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.{AmbientLight, Tint}

final case class SceneUpdateFragment(
    gameLayer: List[SceneGraphNode],
    lightingLayer: List[SceneGraphNode],
    uiLayer: List[SceneGraphNode],
    ambientLight: AmbientLight,
    globalEvents: List[GlobalEvent],
    audio: SceneAudio
) {
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

  def addGlobalEvents(events: GlobalEvent*): SceneUpdateFragment =
    this.copy(globalEvents = globalEvents ++ events.toList)

  def addGlobalEvents(events: List[GlobalEvent]): SceneUpdateFragment =
    this.copy(globalEvents = globalEvents ++ events)

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
      a.globalEvents ++ b.globalEvents,
      a.audio |+| b.audio
    )
}
