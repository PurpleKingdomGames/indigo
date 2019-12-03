package indigojs.delegates

final class SceneAudioDelegate(val sourceA: SceneAudioSourceDelegate, val sourceB: SceneAudioSourceDelegate, val sourceC: SceneAudioSourceDelegate)

object SceneAudioDelegate {

  val None: SceneAudioDelegate =
    new SceneAudioDelegate(SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None)

}
