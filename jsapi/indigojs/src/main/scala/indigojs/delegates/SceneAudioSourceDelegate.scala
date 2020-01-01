package indigojs.delegates

import scala.scalajs.js.annotation._
import indigo.shared.scenegraph.SceneAudioSource
import indigo.shared.datatypes.BindingKey

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneAudioSource")
final class SceneAudioSourceDelegate(_bindingKey: String, _playbackPattern: PlaybackPatternDelegate, _masterVolume: VolumeDelegate) {

  @JSExport
  val bindingKey = _bindingKey
  @JSExport
  val playbackPattern = _playbackPattern
  @JSExport
  val masterVolume = _masterVolume

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
