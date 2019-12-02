package indigo.shared.scenegraph

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
