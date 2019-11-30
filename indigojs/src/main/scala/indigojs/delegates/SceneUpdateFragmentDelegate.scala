package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.SceneUpdateFragment
// import scala.scalajs.js

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneUpdateFragment")
final class SceneUpdateFragmentDelegate(
    // val gameLayer: SceneLayer,
    // val lightingLayer: SceneLayer,
    // val uiLayer: SceneLayer,
    // val ambientLight: Tint,
    // val globalEvents: List[GlobalEvent],
    // val audio: SceneAudio,
    // val screenEffects: ScreenEffects,
    // val cloneBlanks: List[CloneBlank]
) {

  def toInternal: SceneUpdateFragment =
    SceneUpdateFragment.empty

}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneUpdateFragmentOps")
object SceneUpdateFragmentDelegate {

  @JSExport
  val empty: SceneUpdateFragmentDelegate =
    new SceneUpdateFragmentDelegate()

}
