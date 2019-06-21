package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.AmbientLight

final class SceneUpdateFragment(
    val gameLayer: List[SceneGraphNode],
    val lightingLayer: List[SceneGraphNode],
    val uiLayer: List[SceneGraphNode],
    val ambientLight: AmbientLight,
    val globalEvents: List[GlobalEvent],
    val audio: SceneAudio
) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addGameLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addGameLayerNodes(nodes.toList)

  def addGameLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer ++ nodes, lightingLayer, uiLayer, ambientLight, globalEvents, audio)

  def addLightingLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addLightingLayerNodes(nodes.toList)

  def addLightingLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer ++ nodes, uiLayer, ambientLight, globalEvents, audio)

  def addUiLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addUiLayerNodes(nodes.toList)

  def addUiLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer ++ nodes, ambientLight, globalEvents, audio)

  def withAmbientLight(light: AmbientLight): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, light, globalEvents, audio)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight.withAmount(amount), globalEvents, audio)

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight.withTint(r, g, b), globalEvents, audio)

  def addGlobalEvents(events: GlobalEvent*): SceneUpdateFragment =
    addGlobalEvents(events.toList)

  def addGlobalEvents(events: List[GlobalEvent]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents ++ events, audio)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, sceneAudio)

}
object SceneUpdateFragment {

  def apply(
      gameLayer: List[SceneGraphNode],
      lightingLayer: List[SceneGraphNode],
      uiLayer: List[SceneGraphNode],
      ambientLight: AmbientLight,
      globalEvents: List[GlobalEvent],
      audio: SceneAudio
  ): SceneUpdateFragment =
    new SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio)

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
