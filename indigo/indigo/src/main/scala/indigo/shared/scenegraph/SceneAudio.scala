package indigo.shared.scenegraph

import indigo.shared.audio.Volume

/** Describes what audio is currently being played by the scene as part of a `SceneUpdateFragment`. Can play up to three
  * audio sources at once.
  */
final case class SceneAudio(
    masterVolume: Volume,
    sourceA: Option[SceneAudioSource],
    sourceB: Option[SceneAudioSource],
    sourceC: Option[SceneAudioSource]
) derives CanEqual:
  def |+|(other: SceneAudio): SceneAudio =
    SceneAudio.combine(this, other)

  def withMasterVolume(volume: Volume): SceneAudio =
    this.copy(masterVolume = volume)

object SceneAudio:

  val Mute: SceneAudio = SceneAudio(Volume.Max, None, None, None)

  def apply(sourceA: SceneAudioSource): SceneAudio =
    SceneAudio(Volume.Max, Some(sourceA), None, None)

  def apply(sourceA: SceneAudioSource, sourceB: SceneAudioSource): SceneAudio =
    SceneAudio(Volume.Max, Some(sourceA), Some(sourceB), None)

  def combine(a: SceneAudio, b: SceneAudio): SceneAudio =
    SceneAudio(a.masterVolume, b.sourceA.orElse(a.sourceA), b.sourceB.orElse(a.sourceB), b.sourceC.orElse(a.sourceC))
