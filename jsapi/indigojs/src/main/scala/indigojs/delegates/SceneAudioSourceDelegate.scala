package indigojs.delegates

import scala.scalajs.js.annotation._
import scala.scalajs.js
import indigo.shared.scenegraph.SceneAudioSource
import indigo.shared.datatypes.BindingKey

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("SceneAudioSource")
final class SceneAudioSourceDelegate(_bindingKey: String, _playbackPattern: PlaybackPatternDelegate, _masterVolume: js.UndefOr[VolumeDelegate]) {

  @JSExport
  val bindingKey = _bindingKey
  @JSExport
  val playbackPattern = _playbackPattern
  @JSExport
  val masterVolume = _masterVolume.toOption match {
      case Some(m) => m
      case None => VolumeDelegate.Max
  }

  @JSExport
  def concat(other: SceneAudioSourceDelegate): SceneAudioSourceDelegate =
    (this, other) match {
      case (SceneAudioSourceDelegate.None, y) =>
        y

      case (x, SceneAudioSourceDelegate.None) =>
        x

      case (_, y) =>
        y
    }


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
