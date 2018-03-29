package com.purplekingdomgames.indigo.gameengine.scenegraph

case class SceneAudio(sourceA: SceneAudioSource, sourceB: SceneAudioSource, sourceC: SceneAudioSource) {
  def |+|(other: SceneAudio): SceneAudio =
    SceneAudio.combine(this, other)
}
object SceneAudio {

  def apply(sourceA: SceneAudioSource): SceneAudio =
    SceneAudio(sourceA, SceneAudioSource(Silent, 0), SceneAudioSource(Silent, 0))

  def apply(sourceA: SceneAudioSource, sourceB: SceneAudioSource): SceneAudio =
    SceneAudio(sourceA, sourceB, SceneAudioSource(Silent, 0))

  val None: SceneAudio =
    SceneAudio(SceneAudioSource(Silent, 0), SceneAudioSource(Silent, 0), SceneAudioSource(Silent, 0))

  def combine(a: SceneAudio, b: SceneAudio): SceneAudio =
    SceneAudio(a.sourceA |+| b.sourceA, a.sourceB |+| b.sourceB, a.sourceC |+| b.sourceC)

}

case class SceneAudioSource(playbackPattern: PlaybackPattern, masterVolume: Double) {
  def |+|(other: SceneAudioSource): SceneAudioSource =
    SceneAudioSource.combine(this, other)
}
object SceneAudioSource {

  def apply(playbackPattern: PlaybackPattern): SceneAudioSource =
    SceneAudioSource(playbackPattern, 1)

  val None: SceneAudioSource =
    SceneAudioSource(Silent, 0)

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
case object Silent extends PlaybackPattern
case class SingleTrackLoop(track: Track) extends PlaybackPattern

case class Track(assetRef: String, volume: Double)
object Track {
  def apply(assetRef: String): Track =
    Track(assetRef, 1)
}
