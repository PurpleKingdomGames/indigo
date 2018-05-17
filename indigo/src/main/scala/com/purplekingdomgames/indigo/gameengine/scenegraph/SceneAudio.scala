package com.purplekingdomgames.indigo.gameengine.scenegraph

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.BindingKey

case class SceneAudio(sourceA: SceneAudioSource, sourceB: SceneAudioSource, sourceC: SceneAudioSource) {
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

case class SceneAudioSource(bindingKey: BindingKey, playbackPattern: PlaybackPattern, masterVolume: Volume) {
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
  case object Silent                       extends PlaybackPattern
  case class SingleTrackLoop(track: Track) extends PlaybackPattern
}

case class Track(assetRef: String, volume: Volume)
object Track {
  def apply(assetRef: String): Track =
    Track(assetRef, Volume.Max)
}

class Volume(val amount: Double) extends AnyVal {
  def *(other: Volume): Volume =
    Volume.product(this, other)
}
object Volume {
  val Min: Volume = Volume(0)
  val Max: Volume = Volume(1)

  def apply(volume: Double): Volume =
    new Volume(if (volume < 0) 0 else if (volume > 1) 1 else volume)

  def product(a: Volume, b: Volume): Volume =
    Volume(a.amount * b.amount)
}
