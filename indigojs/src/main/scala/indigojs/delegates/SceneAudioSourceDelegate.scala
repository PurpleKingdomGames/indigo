package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.SceneAudioSource
import indigo.shared.datatypes.BindingKey

@JSExportTopLevel("SceneAudioSource")
final class SceneAudioSourceDelegate(val bindingKey: String, val playbackPattern: PlaybackPatternDelegate, val masterVolume: VolumeDelegate) {
  def toInternal: SceneAudioSource =
    new SceneAudioSource(new BindingKey(bindingKey), playbackPattern.toInternal, masterVolume.toInternal)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneAudioSourceHelper")
@JSExportAll
object SceneAudioSourceDelegate {

  val None: SceneAudioSourceDelegate =
    new SceneAudioSourceDelegate("none", PlaybackPatternDelegate.Silent, VolumeDelegate.Min)

}
