package indigojs.delegates

final class SceneAudioDelegate(sourceA: SceneAudioSourceDelegate, sourceB: SceneAudioSourceDelegate, sourceC: SceneAudioSourceDelegate)

object SceneAudio {

  val None: SceneAudioDelegate =
    new SceneAudioDelegate(SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None, SceneAudioSourceDelegate.None)

}
