package indigo.platform.audio

import indigo.shared.datatypes.BindingKey
import indigo.shared.scenegraph.{PlaybackPattern, SceneAudio, SceneAudioSource}
import indigo.shared.platform.AudioPlayer
import indigo.shared.audio.Volume
import indigo.platform.assets.{AssetDataFormats}

import org.scalajs.dom.{AudioBufferSourceNode, GainNode}
import org.scalajs.dom.raw.AudioContext
import scala.scalajs.js

import indigo.shared.EqualTo._
import indigo.shared.assets.AssetName
import indigo.platform.assets.LoadedAudioAsset

object AudioPlayerImpl {


  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  val audioContext: AudioContext = {
    val ctx = (js.Dynamic.global.window.AudioContext || js.Dynamic.global.window.webkitAudioContext).asInstanceOf[AudioContext]
    ctx
  }

  def init: AudioPlayerImpl =
    new AudioPlayerImpl(audioContext)
}

final class AudioPlayerImpl(context: AudioContext) extends AudioPlayer {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var soundAssets: List[LoadedAudioAsset] = Nil

  def addAudioAssets(audioAssets: List[LoadedAudioAsset]): Unit =
    soundAssets = soundAssets ++ audioAssets

  private def setupNodes(audioBuffer: AssetDataFormats.AudioDataFormat, volume: Volume, loop: Boolean): AudioNodes = {
    val source = context.createBufferSource()
    source.buffer = audioBuffer
    source.loop = loop

    val gainNode = context.createGain()
    source.connect(gainNode)
    gainNode.connect(context.destination)
    gainNode.gain.value = volume.amount

    new AudioNodes(source, gainNode)
  }

  private def findAudioDataByName(assetName: AssetName): Option[AssetDataFormats.AudioDataFormat] =
    soundAssets.find(a => a.name === assetName).map(_.data)

  def playSound(assetName: AssetName, volume: Volume): Unit =
    findAudioDataByName(assetName).foreach { sound =>
      setupNodes(sound, volume, loop = false).audioBufferSourceNode.start(0)
    }

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var sourceA: AudioSourceState = new AudioSourceState(BindingKey("none"), None)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var sourceB: AudioSourceState = new AudioSourceState(BindingKey("none"), None)
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var sourceC: AudioSourceState = new AudioSourceState(BindingKey("none"), None)

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
            new AudioSourceState(sceneAudioSource.bindingKey, None)

          case PlaybackPattern.SingleTrackLoop(track) =>
            currentSource.audioNodes.foreach(_.audioBufferSourceNode.stop(0))

            val nodes =
              findAudioDataByName(track.assetName)
                .map(asset => setupNodes(asset, track.volume * sceneAudioSource.masterVolume, loop = true))

            nodes.foreach(_.audioBufferSourceNode.start(0))

            new AudioSourceState(
              bindingKey = sceneAudioSource.bindingKey,
              audioNodes = nodes
            )
        }
      }

  private class AudioSourceState(val bindingKey: BindingKey, val audioNodes: Option[AudioNodes])
  private class AudioNodes(val audioBufferSourceNode: AudioBufferSourceNode, val gainNode: GainNode)

}
