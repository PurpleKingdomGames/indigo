package indigo.scenegraph

import indigo.shared.datatypes.BindingKey
import indigo.shared.audio.{Track, Volume}

final case class SceneAudio(sourceA: SceneAudioSource, sourceB: SceneAudioSource, sourceC: SceneAudioSource) {
  def |+|(other: SceneAudio): SceneAudio =
    SceneAudio.combine(this, other)
}
object SceneAudio {

  def apply(sourceA: SceneAudioSource): SceneAudio =
    SceneAudio(sourceA, SceneAudioSource.None, SceneAudioSource.None)

  def apply(sourceA: SceneAudioSource, sourceB: SceneAudioSource): SceneAudio =
    SceneAudio(sourceA, sourceB, SceneAudioSource.None)

  val None: SceneAudio =
    SceneAudio(SceneAudioSource.None, SceneAudioSource.None, SceneAudioSource.None)

  def combine(a: SceneAudio, b: SceneAudio): SceneAudio =
    SceneAudio(a.sourceA |+| b.sourceA, a.sourceB |+| b.sourceB, a.sourceC |+| b.sourceC)

}

final case class SceneAudioSource(bindingKey: BindingKey, playbackPattern: PlaybackPattern, masterVolume: Volume) {
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

sealed trait PlaybackPattern
object PlaybackPattern {
  case object Silent                             extends PlaybackPattern
  final case class SingleTrackLoop(track: Track) extends PlaybackPattern
}
