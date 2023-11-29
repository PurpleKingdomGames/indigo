package indigo.shared.scenegraph

/** Describes what audio is currently being played by the scene as part of a `SceneUpdateFragment`. Can play up to three
  * audio sources at once.
  */
final case class SceneAudio(
    sourceA: Option[SceneAudioSource],
    sourceB: Option[SceneAudioSource],
    sourceC: Option[SceneAudioSource]
) derives CanEqual:
  def |+|(other: SceneAudio): SceneAudio =
    SceneAudio.combine(this, other)

object SceneAudio:

  val Mute: SceneAudio = SceneAudio(None, None, None)

  def apply(sourceA: SceneAudioSource): SceneAudio =
    SceneAudio(Some(sourceA), None, None)

  def apply(sourceA: SceneAudioSource, sourceB: SceneAudioSource): SceneAudio =
    SceneAudio(Some(sourceA), Some(sourceB), None)

  def combine(a: SceneAudio, b: SceneAudio): SceneAudio =
    SceneAudio(b.sourceA.orElse(a.sourceA), b.sourceB.orElse(a.sourceB), b.sourceC.orElse(a.sourceC))
