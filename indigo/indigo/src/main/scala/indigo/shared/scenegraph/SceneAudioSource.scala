package indigo.shared.scenegraph

import indigo.shared.audio.Volume
import indigo.shared.datatypes.BindingKey

/** Represents a single audio source, how it is being played, and at what volume. You could implement a cross fade
  * between two audio sources.
  */
final case class SceneAudioSource(bindingKey: BindingKey, playbackPattern: PlaybackPattern, masterVolume: Volume)
    derives CanEqual:
  val volume: Volume =
    playbackPattern match
      case PlaybackPattern.SingleTrackLoop(track) => track.volume

object SceneAudioSource:
  def apply(bindingKey: BindingKey, playbackPattern: PlaybackPattern): SceneAudioSource =
    SceneAudioSource(bindingKey, playbackPattern, Volume.Max)
