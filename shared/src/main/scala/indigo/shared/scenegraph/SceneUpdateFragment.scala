package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.Tint

final class SceneUpdateFragment(
    val gameLayer: SceneLayer,
    val lightingLayer: SceneLayer,
    val uiLayer: SceneLayer,
    val ambientLight: Tint,
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

  def withAmbientLight(light: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, light, globalEvents, audio, cloneBlanks)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight.withAmount(amount), globalEvents, audio, cloneBlanks)

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, Tint(r, g, b, 1), globalEvents, audio, cloneBlanks)

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

  def withSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withSaturationLevel(amount),
      lightingLayer.withSaturationLevel(amount),
      uiLayer.withSaturationLevel(amount),
      ambientLight,
      globalEvents,
      audio,
      cloneBlanks
    )

  def withGameLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withSaturationLevel(amount), lightingLayer, uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def withLightingLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withSaturationLevel(amount), uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def withUiLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer.withSaturationLevel(amount), ambientLight, globalEvents, audio, cloneBlanks)

  def withColorOverlay(overlay: Tint): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withColorOverlay(overlay),
      lightingLayer.withColorOverlay(overlay),
      uiLayer.withColorOverlay(overlay),
      ambientLight,
      globalEvents,
      audio,
      cloneBlanks
    )

  def withGameLayerColorOverlay(overlay: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withColorOverlay(overlay), lightingLayer, uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def withLightingLayerColorOverlay(overlay: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withColorOverlay(overlay), uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def withUiLayerColorOverlay(overlay: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer.withColorOverlay(overlay), ambientLight, globalEvents, audio, cloneBlanks)

  def withTint(tint: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer.withTint(tint), uiLayer.withTint(tint), ambientLight, globalEvents, audio, cloneBlanks)

  def withGameLayerTint(tint: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer, uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def withLightingLayerTint(tint: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withTint(tint), uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def withUiLayerTint(tint: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer.withTint(tint), ambientLight, globalEvents, audio, cloneBlanks)

}
object SceneUpdateFragment {

  def apply(
      gameLayer: SceneLayer,
      lightingLayer: SceneLayer,
      uiLayer: SceneLayer,
      ambientLight: Tint,
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, cloneBlanks)

  def apply(
      gameLayer: List[SceneGraphNode],
      lightingLayer: List[SceneGraphNode],
      uiLayer: List[SceneGraphNode],
      ambientLight: Tint,
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(SceneLayer(gameLayer), SceneLayer(lightingLayer), SceneLayer(uiLayer), ambientLight, globalEvents, audio, cloneBlanks)

  def apply(): SceneUpdateFragment =
    empty

  def empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, Nil, Tint.None, Nil, SceneAudio.None, Nil)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.gameLayer |+| b.gameLayer,
      a.lightingLayer |+| b.lightingLayer,
      a.uiLayer |+| b.uiLayer,
      a.ambientLight + b.ambientLight,
      a.globalEvents ++ b.globalEvents,
      a.audio |+| b.audio,
      a.cloneBlanks ++ b.cloneBlanks
    )
}

final class SceneLayer(val nodes: List[SceneGraphNode], val colorOverlay: Tint, val tint: Tint, val saturation: Double) {

  def |+|(other: SceneLayer): SceneLayer = {
    val newSaturation: Double =
      (saturation, other.saturation) match {
        case (1d, b) => b
        case (a, 1d) => a
        case (a, b)  => Math.min(a, b)
      }

    SceneLayer(nodes ++ other.nodes, colorOverlay + other.colorOverlay, tint + other.tint, newSaturation)
  }

  def ++(moreNodes: List[SceneGraphNode]): SceneLayer =
    SceneLayer(nodes ++ moreNodes, colorOverlay, tint, saturation)

  def withColorOverlay(overlay: Tint): SceneLayer =
    SceneLayer(nodes, overlay, tint, saturation)

  def withTint(newTint: Tint): SceneLayer =
    SceneLayer(nodes, colorOverlay, newTint, saturation)

  def withSaturationLevel(amount: Double): SceneLayer =
    SceneLayer(nodes, colorOverlay, tint, amount)
}

object SceneLayer {

  def apply(nodes: List[SceneGraphNode]): SceneLayer =
    new SceneLayer(nodes, Tint.Zero, Tint.None, 1.0d)

  def apply(nodes: List[SceneGraphNode], colorOverlay: Tint, tint: Tint, saturation: Double): SceneLayer =
    new SceneLayer(nodes, colorOverlay, tint, saturation)

  def None: SceneLayer =
    SceneLayer(Nil, Tint.Zero, Tint.None, 1.0d)

}
