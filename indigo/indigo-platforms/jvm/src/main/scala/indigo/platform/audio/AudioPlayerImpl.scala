package indigo.platform.audio

// import indigo.shared.datatypes.BindingKey
// import indigo.shared.scenegraph.{PlaybackPattern, SceneAudio, SceneAudioSource}
import indigo.shared.scenegraph.SceneAudio
import indigo.shared.platform.AudioPlayer
import indigo.shared.audio.Volume
// import indigo.platform.assets.{AssetCollection, AssetName}
import indigo.platform.assets.AssetCollection
import indigo.shared.assets.AssetName

// import org.scalajs.dom.{AudioBufferSourceNode, GainNode}
// import org.scalajs.dom.raw.AudioContext

// import indigo.shared.EqualTo._

object AudioPlayerImpl {

  def apply(assetCollection: AssetCollection): AudioPlayer =
    new AudioPlayerImpl(assetCollection/*, new AudioContext()*/)

}

final class AudioPlayerImpl(assetCollection: AssetCollection/*, context: AudioContext*/) extends AudioPlayer {

  // private def setupNodes(audioBuffer: AssetCollection.AudioDataFormat, volume: Volume, loop: Boolean): AudioNodes = {
  //   val source = context.createBufferSource()
  //   source.buffer = audioBuffer
  //   source.loop = loop

  //   val gainNode = context.createGain()
  //   source.connect(gainNode)
  //   gainNode.connect(context.destination)
  //   gainNode.gain.value = volume.amount

  //   new AudioNodes(source, gainNode)
  // }

  def playSound(assetName: AssetName, volume: Volume): Unit = {
    println(assetCollection.images.length.toString())
    println(assetName.value)
    println(volume.amount.toString)
  ()
  }
    // assetCollection.findAudioDataByName(AssetName(assetRef)).foreach { sound =>
    //   setupNodes(sound, volume, loop = false).audioBufferSourceNode.start(0)
    // }

  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  // private var sourceA: AudioSourceState = new AudioSourceState(BindingKey("none"), None)
  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  // private var sourceB: AudioSourceState = new AudioSourceState(BindingKey("none"), None)
  // @SuppressWarnings(Array("org.wartremover.warts.Var"))
  // private var sourceC: AudioSourceState = new AudioSourceState(BindingKey("none"), None)

  def playAudio(sceneAudio: SceneAudio): Unit = {
    // updateSource(sceneAudio.sourceA, sourceA).foreach { src =>
    //   sourceA = src
    // }
    // updateSource(sceneAudio.sourceB, sourceB).foreach { src =>
    //   sourceB = src
    // }
    // updateSource(sceneAudio.sourceC, sourceC).foreach { src =>
    //   sourceC = src
    // }
    println(sceneAudio.hashCode().toString)
    ()
  }

  // private def updateSource(sceneAudioSource: SceneAudioSource, currentSource: AudioSourceState): Option[AudioSourceState] =
  //   if (sceneAudioSource.bindingKey === currentSource.bindingKey) None
  //   else
  //     Option {
  //       sceneAudioSource.playbackPattern match {
  //         case PlaybackPattern.Silent =>
  //           currentSource.audioNodes.foreach(_.audioBufferSourceNode.stop(0))
  //           new AudioSourceState(sceneAudioSource.bindingKey, None)

  //         case PlaybackPattern.SingleTrackLoop(track) =>
  //           currentSource.audioNodes.foreach(_.audioBufferSourceNode.stop(0))

  //           val nodes =
  //             assetCollection
  //               .findAudioDataByName(AssetName(track.assetRef))
  //               .map(asset => setupNodes(asset, track.volume * sceneAudioSource.masterVolume, loop = true))

  //           nodes.foreach(_.audioBufferSourceNode.start(0))

  //           new AudioSourceState(
  //             bindingKey = sceneAudioSource.bindingKey,
  //             audioNodes = nodes
  //           )
  //       }
  //     }

  // private class AudioSourceState(val bindingKey: BindingKey, val audioNodes: Option[AudioNodes])
  // private class AudioNodes(val audioBufferSourceNode: AudioBufferSourceNode, val gainNode: GainNode)

}
