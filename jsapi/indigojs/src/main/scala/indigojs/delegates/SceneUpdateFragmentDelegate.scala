package indigojs.delegates

import indigo.shared.scenegraph.SceneUpdateFragment

import indigojs.delegates.clones.CloneBlankDelegate

import scala.scalajs.js.annotation._
import scala.scalajs.js

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneUpdateFragment")
final class SceneUpdateFragmentDelegate(
    _gameLayer: SceneLayerDelegate,
    _lightingLayer: SceneLayerDelegate,
    _distortionLayer: SceneLayerDelegate,
    _uiLayer: SceneLayerDelegate,
    _ambientLight: RGBADelegate,
    _lights: js.Array[LightDelegate],
    _globalEvents: js.Array[GlobalEventDelegate],
    _audio: SceneAudioDelegate,
    _screenEffects: ScreenEffectsDelegate,
    _cloneBlanks: js.Array[CloneBlankDelegate]
) {

  @JSExport
  val gameLayer = _gameLayer
  @JSExport
  val lightingLayer = _lightingLayer
  @JSExport
  val distortionLayer = _distortionLayer
  @JSExport
  val uiLayer = _uiLayer
  @JSExport
  val ambientLight = _ambientLight
  @JSExport
  val lights = _lights
  @JSExport
  val globalEvents = _globalEvents
  @JSExport
  val audio = _audio
  @JSExport
  val screenEffects = _screenEffects
  @JSExport
  val cloneBlanks = _cloneBlanks

  @JSExport
  def addGameLayerNodes(nodes: js.Array[SceneGraphNodeDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.addLayerNodes(nodes),
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

  @JSExport
  def addLightingLayerNodes(nodes: js.Array[SceneGraphNodeDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer.addLayerNodes(nodes),
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def addDistortionLayerNodes(nodes: js.Array[SceneGraphNodeDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer.addLayerNodes(nodes),
      uiLayer,
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer.addLayerNodes(nodes),
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withAmbientLight(light: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      light,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withLights(newLights: js.Array[LightDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      newLights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def addLights(newLights: js.Array[LightDelegate]): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      newLights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def noLights: SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      new js.Array(),
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
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer.withSaturationLevel(amount),
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer.withSaturationLevel(amount),
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withColorOverlay(overlay: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      new ScreenEffectsDelegate(overlay, overlay),
      cloneBlanks
    )

  @JSExport
  def withGameColorOverlay(overlay: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects.withGameColorOverlay(overlay),
      cloneBlanks
    )

  @JSExport
  def withUiColorOverlay(overlay: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects.withUiColorOverlay(overlay),
      cloneBlanks
    )

  @JSExport
  def withTint(tint: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withTint(tint),
      lightingLayer.withTint(tint),
      distortionLayer,
      uiLayer.withTint(tint),
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withGameLayerTint(tint: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer.withTint(tint),
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

  @JSExport
  def withLightingLayerTint(tint: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer.withTint(tint),
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withUiLayerTint(tint: RGBADelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
      gameLayer,
      lightingLayer,
      distortionLayer,
      uiLayer.withTint(tint),
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer.withMagnification(level),
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
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
      distortionLayer,
      uiLayer,
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  @JSExport
  def withDistortionLayerMagnification(level: Int): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
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

  @JSExport
  def withUiLayerMagnification(level: Int): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
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

  @JSExport
  def concat(other: SceneUpdateFragmentDelegate): SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate(
        gameLayer.concat(other.gameLayer),
        lightingLayer.concat(other.lightingLayer),
        distortionLayer.concat(other.distortionLayer),
        uiLayer.concat(other.uiLayer),
        ambientLight.concat(other.ambientLight),
        lights ++ other.lights,
        globalEvents ++ other.globalEvents,
        audio.concat(other.audio),
        screenEffects.concat(other.screenEffects),
        cloneBlanks ++ other.cloneBlanks
    )

  def toInternal: SceneUpdateFragment =
    new SceneUpdateFragment(
      gameLayer.toInternal,
      lightingLayer.toInternal,
      distortionLayer.toInternal,
      uiLayer.toInternal,
      ambientLight.toInternal,
      lights.toList.map(_.toInternal),
      globalEvents.toList.map(_.toInternal),
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
      SceneLayerDelegate.None,
      RGBADelegate.None,
      new js.Array(),
      new js.Array(),
      SceneAudioDelegate.None,
      ScreenEffectsDelegate.None,
      new js.Array()
    )

}
