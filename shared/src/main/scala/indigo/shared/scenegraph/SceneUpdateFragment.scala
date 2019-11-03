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

  def withAmbientLight(light: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, light, globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight.withAmount(amount), globalEvents, audio, screenEffects, cloneBlanks)

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, Tint(r, g, b, 1), globalEvents, audio, screenEffects, cloneBlanks)

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

  def withColorOverlay(overlay: Tint): SceneUpdateFragment =
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

  def withGameColorOverlay(overlay: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects.withGameColorOverlay(overlay), cloneBlanks)

  def withUiColorOverlay(overlay: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects.withUiColorOverlay(overlay), cloneBlanks)

  def withTint(tint: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer.withTint(tint), uiLayer.withTint(tint), ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withGameLayerTint(tint: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.withTint(tint), lightingLayer, uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withLightingLayerTint(tint: Tint): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer, lightingLayer.withTint(tint), uiLayer, ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def withUiLayerTint(tint: Tint): SceneUpdateFragment =
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
      ambientLight: Tint,
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
      ambientLight: Tint,
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      screenEffects: ScreenEffects,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    new SceneUpdateFragment(SceneLayer(gameLayer), SceneLayer(lightingLayer), SceneLayer(uiLayer), ambientLight, globalEvents, audio, screenEffects, cloneBlanks)

  def apply(): SceneUpdateFragment =
    empty

  def empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, Nil, Tint.None, Nil, SceneAudio.None, ScreenEffects.None, Nil)

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

final class SceneLayer(val nodes: List[SceneGraphNode], val tint: Tint, val saturation: Double, val magnification: Option[Int]) {

  def |+|(other: SceneLayer): SceneLayer = {
    val newSaturation: Double =
      (saturation, other.saturation) match {
        case (1d, b) => b
        case (a, 1d) => a
        case (a, b)  => Math.min(a, b)
      }

    SceneLayer(nodes ++ other.nodes, tint + other.tint, newSaturation, magnification.orElse(other.magnification))
  }

  def ++(moreNodes: List[SceneGraphNode]): SceneLayer =
    SceneLayer(nodes ++ moreNodes, tint, saturation, magnification)

  def withTint(newTint: Tint): SceneLayer =
    SceneLayer(nodes, newTint, saturation, magnification)

  def withSaturationLevel(amount: Double): SceneLayer =
    SceneLayer(nodes, tint, amount, magnification)

  def withMagnification(level: Int): SceneLayer =
    SceneLayer(nodes, tint, saturation, SceneLayer.sanitiseMagnification(level))
}

object SceneLayer {

  def apply(nodes: List[SceneGraphNode]): SceneLayer =
    new SceneLayer(nodes, Tint.None, 1.0d, Option.empty[Int])

  def apply(nodes: List[SceneGraphNode], tint: Tint, saturation: Double, magnification: Option[Int]): SceneLayer =
    new SceneLayer(nodes, tint, saturation, magnification.flatMap(sanitiseMagnification))

  def None: SceneLayer =
    SceneLayer(Nil, Tint.None, 1.0d, Option.empty[Int])

  def sanitiseMagnification(level: Int): Option[Int] =
    Option(Math.max(1, Math.min(256, level)))

}

final class ScreenEffects(val gameColorOverlay: Tint, val uiColorOverlay: Tint) {

  def |+|(other: ScreenEffects): ScreenEffects =
    ScreenEffects(gameColorOverlay + other.gameColorOverlay, uiColorOverlay + other.uiColorOverlay)

  def withGameColorOverlay(overlay: Tint): ScreenEffects =
    ScreenEffects(overlay, uiColorOverlay)

  def withUiColorOverlay(overlay: Tint): ScreenEffects =
    ScreenEffects(gameColorOverlay, overlay)

}

object ScreenEffects {

  def apply(gameColorOverlay: Tint, uiColorOverlay: Tint): ScreenEffects =
    new ScreenEffects(gameColorOverlay, uiColorOverlay)

  def None: ScreenEffects =
    ScreenEffects(Tint.Zero, Tint.Zero)

}
