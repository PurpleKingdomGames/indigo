package indigo.gameengine.audio

import indigo.gameengine.assets.LoadedAudioAsset
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.gameengine.scenegraph.{PlaybackPattern, SceneAudio, SceneAudioSource, Volume}
import org.scalajs.dom
import org.scalajs.dom.{AudioBufferSourceNode, GainNode}
import org.scalajs.dom.raw.AudioContext

object AudioPlayer {

  def apply(loadedAudioAssets: List[LoadedAudioAsset]): IAudioPlayer =
    new AudioPlayerImpl(loadedAudioAssets, new AudioContext())

}

trait IAudioPlayer {
  def playSound(assetRef: String, volume: Volume): Unit
  def playAudio(sceneAudio: SceneAudio): Unit
}

final class AudioPlayerImpl(loadedAudioAssets: List[LoadedAudioAsset], context: AudioContext) extends IAudioPlayer {

  private def setupNodes(audioBuffer: dom.AudioBuffer, volume: Volume, loop: Boolean): AudioNodes = {
    val source = context.createBufferSource()
    source.buffer = audioBuffer
    source.loop = loop

    val gainNode = context.createGain()
    source.connect(gainNode)
    gainNode.connect(context.destination)
    gainNode.gain.value = volume.amount

    AudioNodes(source, gainNode)
  }

  def playSound(assetRef: String, volume: Volume): Unit =
    loadedAudioAssets.find(_.name == assetRef).foreach { sound =>
      setupNodes(sound.data, volume, loop = false).audioBufferSourceNode.start(0)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var sourceA: AudioSourceState = AudioSourceState(BindingKey("none"), None)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var sourceB: AudioSourceState = AudioSourceState(BindingKey("none"), None)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var sourceC: AudioSourceState = AudioSourceState(BindingKey("none"), None)

  def playAudio(sceneAudio: SceneAudio): Unit = {
    updateSource(sceneAudio.sourceA, sourceA).foreach { src =>
      sourceA = src
    }
    updateSource(sceneAudio.sourceB, sourceB).foreach { src =>
      sourceB = src
    }
    updateSource(sceneAudio.sourceC, sourceC).foreach { src =>
      sourceC = src
    }
  }

  private def updateSource(sceneAudioSource: SceneAudioSource, currentSource: AudioSourceState): Option[AudioSourceState] =
    if (sceneAudioSource.bindingKey === currentSource.bindingKey) None
    else
      Option {
        sceneAudioSource.playbackPattern match {
          case PlaybackPattern.Silent =>
            currentSource.audioNodes.foreach(_.audioBufferSourceNode.stop(0))
            AudioSourceState(sceneAudioSource.bindingKey, None)

          case PlaybackPattern.SingleTrackLoop(track) =>
            currentSource.audioNodes.foreach(_.audioBufferSourceNode.stop(0))

            val nodes =
              loadedAudioAssets
                .find(_.name == track.assetRef)
                .map(asset => setupNodes(asset.data, track.volume * sceneAudioSource.masterVolume, loop = true))

            nodes.foreach(_.audioBufferSourceNode.start(0))

            AudioSourceState(
              bindingKey = sceneAudioSource.bindingKey,
              audioNodes = nodes
            )
        }
      }

  private case class AudioSourceState(bindingKey: BindingKey, audioNodes: Option[AudioNodes])
  private case class AudioNodes(audioBufferSourceNode: AudioBufferSourceNode, gainNode: GainNode)

}
