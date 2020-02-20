package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.RGBA

final class SceneUpdateFragment(
    val gameLayer: SceneLayer,
    val lightingLayer: SceneLayer,
    val uiLayer: SceneLayer,
    val ambientLight: RGBA,
    val globalEvents: List[GlobalEvent],
    val audio: SceneAudio,
    val screenEffects: ScreenEffects,
    val cloneBlanks: List[CloneBlank]
) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addGameLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addGameLayerNodes(nodes.toList)

  def addGameLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer ++ nodes, lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def addLightingLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addLightingLayerNodes(nodes.toList)

  def addLightingLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer ++ nodes, uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def addUiLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addUiLayerNodes(nodes.toList)

  def addUiLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer ++ nodes, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLight(light: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, light, globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight.withAmount(amount), globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, RGBA(r, g, b, 1), globalEvents, audio, screenEffects, cloneBlanks)

  def addGlobalEvents(events: GlobalEvent*): SceneUpdateFragment =
    addGlobalEvents(events.toList)

  def addGlobalEvents(events: List[GlobalEvent]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents ++ events, audio, screenEffects, cloneBlanks)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, sceneAudio, screenEffects, cloneBlanks)

  def addCloneBlanks(blanks: CloneBlank*): SceneUpdateFragment =
    addCloneBlanks(blanks.toList)

  def addCloneBlanks(blanks: List[CloneBlank]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks ++ blanks)

  def withSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withSaturationLevel(amount),
      lightingLayer.withSaturationLevel(amount),
      uiLayer.withSaturationLevel(amount),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withGameLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withSaturationLevel(amount), lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withLightingLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withSaturationLevel(amount), uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withUiLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer.withSaturationLevel(amount), ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withColorOverlay(overlay: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer,
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      ScreenEffects(overlay, overlay),
      cloneBlanks
    )

  def withGameColorOverlay(overlay: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects.withGameColorOverlay(overlay), cloneBlanks)

  def withUiColorOverlay(overlay: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects.withUiColorOverlay(overlay), cloneBlanks)

  def withTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer.withTint(tint), uiLayer.withTint(tint), ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withGameLayerTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withLightingLayerTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withTint(tint), uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withUiLayerTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer.withTint(tint), ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withMagnification(level),
      lightingLayer.withMagnification(level),
      uiLayer.withMagnification(level),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withGameLayerMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withMagnification(level),
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withLightingLayerMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer,
      lightingLayer.withMagnification(level),
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withUiLayerMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer,
      lightingLayer,
      uiLayer.withMagnification(level),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )
}
object SceneUpdateFragment {

  def apply(
      gameLayer: SceneLayer,
      lightingLayer: SceneLayer,
      uiLayer: SceneLayer,
      ambientLight: RGBA,
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      screenEffects: ScreenEffects,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def apply(
      gameLayer: List[SceneGraphNode],
      lightingLayer: List[SceneGraphNode],
      uiLayer: List[SceneGraphNode],
      ambientLight: RGBA,
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      screenEffects: ScreenEffects,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(SceneLayer(gameLayer), SceneLayer(lightingLayer), SceneLayer(uiLayer), ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, Nil, RGBA.None, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.gameLayer |+| b.gameLayer,
      a.lightingLayer |+| b.lightingLayer,
      a.uiLayer |+| b.uiLayer,
      a.ambientLight + b.ambientLight,
      a.globalEvents ++ b.globalEvents,
      a.audio |+| b.audio,
      a.screenEffects |+| b.screenEffects,
      a.cloneBlanks ++ b.cloneBlanks
    )
}
