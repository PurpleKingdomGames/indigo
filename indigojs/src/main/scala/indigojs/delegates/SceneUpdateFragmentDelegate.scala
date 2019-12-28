package indigojs.delegates

import indigo.shared.scenegraph.SceneUpdateFragment

import scala.scalajs.js.annotation._
import scala.scalajs.js

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneUpdateFragment")
final class SceneUpdateFragmentDelegate(
    val gameLayer: SceneLayerDelegate,
    val lightingLayer: SceneLayerDelegate,
    val uiLayer: SceneLayerDelegate,
    val ambientLight: TintDelegate,
    val globalEvents: js.Array[GlobalEventDelegate],
    val audio: SceneAudioDelegate,
    val screenEffects: ScreenEffectsDelegate,
    val cloneBlanks: js.Array[CloneBlankDelegate]
) {

  @JSExport
  def addGameLayerNodes(nodes: js.Array[SceneGraphNodeDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.addLayerNodes(nodes),
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def addLightingLayerNodes(nodes: js.Array[SceneGraphNodeDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer.addLayerNodes(nodes),
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def addUiLayerNodes(nodes: js.Array[SceneGraphNodeDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer.addLayerNodes(nodes),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withAmbientLight(light: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer,
      light,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def addGlobalEvents(events: js.Array[GlobalEventDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents ++ events,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withAudio(sceneAudio: SceneAudioDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      sceneAudio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def addCloneBlanks(blanks: js.Array[CloneBlankDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks ++ blanks
    )

  @JSExport
  def withSaturationLevel(amount: Double): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withSaturationLevel(amount),
      lightingLayer.withSaturationLevel(amount),
      uiLayer.withSaturationLevel(amount),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withGameLayerSaturationLevel(amount: Double): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withSaturationLevel(amount),
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withLightingLayerSaturationLevel(amount: Double): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer.withSaturationLevel(amount),
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withUiLayerSaturationLevel(amount: Double): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer.withSaturationLevel(amount),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withColorOverlay(overlay: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      new ScreenEffectsDelegate(overlay, overlay),
      cloneBlanks
    )

  @JSExport
  def withGameColorOverlay(overlay: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects.withGameColorOverlay(overlay),
      cloneBlanks
    )

  @JSExport
  def withUiColorOverlay(overlay: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects.withUiColorOverlay(overlay),
      cloneBlanks
    )

  @JSExport
  def withTint(tint: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withTint(tint),
      lightingLayer.withTint(tint),
      uiLayer.withTint(tint),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withGameLayerTint(tint: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withTint(tint),
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withLightingLayerTint(tint: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer.withTint(tint),
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withUiLayerTint(tint: TintDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer.withTint(tint),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withMagnification(level: Int): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withMagnification(level),
      lightingLayer.withMagnification(level),
      uiLayer.withMagnification(level),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withGameLayerMagnification(level: Int): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withMagnification(level),
      lightingLayer,
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withLightingLayerMagnification(level: Int): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer.withMagnification(level),
      uiLayer,
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withUiLayerMagnification(level: Int): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      uiLayer.withMagnification(level),
      ambientLight,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def toInternal: SceneUpdateFragment =
    new SceneUpdateFragment(
      gameLayer.toInternal,
      lightingLayer.toInternal,
      uiLayer.toInternal,
      ambientLight.toInternal,
      globalEvents.toList,
      audio.toInternal,
      screenEffects.toInternal,
      cloneBlanks.map(_.toInternal).toList
    )

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneUpdateFragmentHelper")
object SceneUpdateFragmentDelegate {

  @JSExport
  val empty: SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      SceneLayerDelegate.None,
      SceneLayerDelegate.None,
      SceneLayerDelegate.None,
      TintDelegate.None,
      new js.Array(),
      SceneAudioDelegate.None,
      ScreenEffectsDelegate.None,
      new js.Array()
    )

}
