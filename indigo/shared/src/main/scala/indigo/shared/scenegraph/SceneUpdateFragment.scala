package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.RGBA

final class SceneUpdateFragment(
    val gameLayer: SceneLayer,
    val lightingLayer: SceneLayer,
    val distortionLayer: SceneLayer,
    val uiLayer: SceneLayer,
    val ambientLight: RGBA,
    val lights: List[Light],
    val globalEvents: List[GlobalEvent],
    val audio: SceneAudio,
    val screenEffects: ScreenEffects,
    val cloneBlanks: List[CloneBlank],
) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addGameLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addGameLayerNodes(nodes.toList)

  def addGameLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer ++ nodes, lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def addLightingLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addLightingLayerNodes(nodes.toList)

  def addLightingLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer ++ nodes, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def addDistortionLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addDistortionLayerNodes(nodes.toList)

  def addDistortionLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer ++ nodes, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def addUiLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addUiLayerNodes(nodes.toList)

  def addUiLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer ++ nodes, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLight(light: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, light, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight.withAmount(amount), lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, RGBA(r, g, b, 1), lights, globalEvents, audio, screenEffects, cloneBlanks)

  def noLights: SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, Nil, globalEvents, audio, screenEffects, cloneBlanks)

  def withLights(newLights: Light*): SceneUpdateFragment =
    withLights(newLights.toList)

  def withLights(newLights: List[Light]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, newLights, globalEvents, audio, screenEffects, cloneBlanks)

  def addLights(newLights: Light*): SceneUpdateFragment =
    addLights(newLights.toList)

  def addLights(newLights: List[Light]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, lights ++ newLights.toList, globalEvents, audio, screenEffects, cloneBlanks)

  def addGlobalEvents(events: GlobalEvent*): SceneUpdateFragment =
    addGlobalEvents(events.toList)

  def addGlobalEvents(events: List[GlobalEvent]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents ++ events, audio, screenEffects, cloneBlanks)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, sceneAudio, screenEffects, cloneBlanks)

  def addCloneBlanks(blanks: CloneBlank*): SceneUpdateFragment =
    addCloneBlanks(blanks.toList)

  def addCloneBlanks(blanks: List[CloneBlank]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks ++ blanks)

  def withSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withSaturationLevel(amount),
      lightingLayer.withSaturationLevel(amount),
      distortionLayer,
      uiLayer.withSaturationLevel(amount),
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withGameLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withSaturationLevel(amount), lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withLightingLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withSaturationLevel(amount), distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withUiLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer.withSaturationLevel(amount), ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withColorOverlay(overlay: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      ScreenEffects(overlay, overlay),
      cloneBlanks
    )

  def withGameColorOverlay(overlay: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects.withGameColorOverlay(overlay), cloneBlanks)

  def withUiColorOverlay(overlay: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects.withUiColorOverlay(overlay), cloneBlanks)

  def withTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer.withTint(tint), distortionLayer, uiLayer.withTint(tint), ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withGameLayerTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withLightingLayerTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withTint(tint), distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withUiLayerTint(tint: RGBA): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer.withTint(tint), ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def withMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withMagnification(level),
      lightingLayer.withMagnification(level),
      distortionLayer.withMagnification(level),
      uiLayer.withMagnification(level),
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withGameLayerMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer.withMagnification(level),
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withLightingLayerMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer,
      lightingLayer.withMagnification(level),
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withDistortionLayerMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer,
      lightingLayer,
      distortionLayer.withMagnification(level),
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def withUiLayerMagnification(level: Int): SceneUpdateFragment =
    SceneUpdateFragment(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer.withMagnification(level),
      ambientLight,
      lights,
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
      distortionLayer: SceneLayer,
      uiLayer: SceneLayer,
      ambientLight: RGBA,
      lights: List[Light],
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      screenEffects: ScreenEffects,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(gameLayer, lightingLayer, distortionLayer, uiLayer, ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def apply(
      gameLayer: List[SceneGraphNode],
      lightingLayer: List[SceneGraphNode],
      distortionLayer: List[SceneGraphNode],
      uiLayer: List[SceneGraphNode],
      ambientLight: RGBA,
      lights: List[Light],
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      screenEffects: ScreenEffects,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(SceneLayer(gameLayer), SceneLayer(lightingLayer), SceneLayer(distortionLayer), SceneLayer(uiLayer), ambientLight, lights, globalEvents, audio, screenEffects, cloneBlanks)

  def apply(gameLayer: SceneGraphNode*): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.toList, Nil, Nil, Nil, RGBA.None, Nil, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, Nil, Nil, RGBA.None, Nil, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.gameLayer |+| b.gameLayer,
      a.lightingLayer |+| b.lightingLayer,
      a.distortionLayer |+| b.distortionLayer,
      a.uiLayer |+| b.uiLayer,
      a.ambientLight + b.ambientLight,
      a.lights ++ b.lights,
      a.globalEvents ++ b.globalEvents,
      a.audio |+| b.audio,
      a.screenEffects |+| b.screenEffects,
      a.cloneBlanks ++ b.cloneBlanks
    )
}
