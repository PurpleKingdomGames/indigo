package indigo.shared.scenegraph

import indigo.shared.datatypes.BindingKey
import indigo.shared.audio.Volume

final case class SceneAudioSource(bindingKey: BindingKey, playbackPattern: PlaybackPattern, masterVolume: Volume) derives CanEqual {
  def |+|(other: SceneAudioSource): SceneAudioSource =
    SceneAudioSource.combine(this, other)
}
object SceneAudioSource {

  def apply(bindingKey: BindingKey, playbackPattern: PlaybackPattern): SceneAudioSource =
    SceneAudioSource(bindingKey, playbackPattern, Volume.Max)

  val None: SceneAudioSource =
    SceneAudioSource(BindingKey("none"), PlaybackPattern.Silent, Volume.Min)

  def combine(a: SceneAudioSource, b: SceneAudioSource): SceneAudioSource =
    (a, b) match {
      case (None, y) =>
        y

      case (x, None) =>
        x

      case (_, y) =>
        y
    }
}
