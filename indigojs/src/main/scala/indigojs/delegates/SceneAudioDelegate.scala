package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.SceneAudio

@JSExportTopLevel("SceneAudio")
final class SceneAudioDelegate(val sourceA: SceneAudioSourceDelegate, val sourceB: SceneAudioSourceDelegate, val sourceC: SceneAudioSourceDelegate) {
  def toInternal: SceneAudio =
    new SceneAudio(
      sourceA.toInternal,
      sourceB.toInternal,
      sourceC.toInternal
    )
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneAudioHelper")
@JSExportAll
object SceneAudioDelegate {

  val None: SceneAudioDelegate =
    new SceneAudioDelegate(SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None)

}
