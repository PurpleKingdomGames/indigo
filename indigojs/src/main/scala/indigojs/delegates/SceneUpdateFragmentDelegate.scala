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

  def toInternal: SceneUpdateFragment =
    ??? // TODO

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
