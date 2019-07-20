package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.AmbientLight

final class SceneUpdateFragment(
    val gameLayer: List[SceneGraphNode],
    val lightingLayer: List[SceneGraphNode],
    val uiLayer: List[SceneGraphNode],
    val ambientLight: AmbientLight,
    val globalEvents: List[GlobalEvent],
    val audio: SceneAudio,
    val cloneBlanks: List[CloneBlank]
) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addGameLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addGameLayerNodes(nodes.toList)

  def addGameLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer ++ nodes, lightingLayer, uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def addLightingLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addLightingLayerNodes(nodes.toList)

  def addLightingLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer ++ nodes, uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def addUiLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addUiLayerNodes(nodes.toList)

  def addUiLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer ++ nodes, ambientLight, globalEvents, audio, cloneBlanks)

  def withAmbientLight(light: AmbientLight): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, light, globalEvents, audio, cloneBlanks)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight.withAmount(amount), globalEvents, audio, cloneBlanks)

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight.withTint(r, g, b), globalEvents, audio, cloneBlanks)

  def addGlobalEvents(events: GlobalEvent*): SceneUpdateFragment =
    addGlobalEvents(events.toList)

  def addGlobalEvents(events: List[GlobalEvent]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents ++ events, audio, cloneBlanks)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, sceneAudio, cloneBlanks)

  def addCloneBlanks(blanks: CloneBlank*): SceneUpdateFragment =
    addCloneBlanks(blanks.toList)

  def addCloneBlanks(blanks: List[CloneBlank]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, cloneBlanks ++ blanks)

}
object SceneUpdateFragment {

  def apply(
      gameLayer: List[SceneGraphNode],
      lightingLayer: List[SceneGraphNode],
      uiLayer: List[SceneGraphNode],
      ambientLight: AmbientLight,
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def apply(): SceneUpdateFragment =
    empty

  def empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, Nil, AmbientLight.Normal, Nil, SceneAudio.None, Nil)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.gameLayer ++ b.gameLayer,
      a.lightingLayer ++ b.lightingLayer,
      a.uiLayer ++ b.uiLayer,
      a.ambientLight + b.ambientLight,
      a.globalEvents ++ b.globalEvents,
      a.audio |+| b.audio,
      a.cloneBlanks ++ b.cloneBlanks
    )
}
