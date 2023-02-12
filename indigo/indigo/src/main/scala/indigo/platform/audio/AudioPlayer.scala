package indigo.platform.audio

import indigo.platform.assets.LoadedAudioAsset
import indigo.shared.assets.AssetName
import indigo.shared.audio.Volume
import indigo.shared.datatypes.BindingKey
import indigo.shared.scenegraph.PlaybackPattern
import indigo.shared.scenegraph.SceneAudio
import indigo.shared.scenegraph.SceneAudioSource
import org.scalajs.dom
import org.scalajs.dom.AudioBuffer
import org.scalajs.dom.AudioBufferSourceNode
import org.scalajs.dom.AudioContext
import org.scalajs.dom.AudioDestinationNode
import org.scalajs.dom.GainNode
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

import scala.concurrent.Future
import scala.scalajs.js

object AudioPlayer {

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def giveAudioContext(): AudioContextProxy =
    if (
      js.Dynamic.global.window.webkitAudioContext != null && !js.isUndefined(
        js.Dynamic.global.window.webkitAudioContext
      )
    )
      AudioContextProxy.WebKitAudioContext(js.Dynamic.newInstance(js.Dynamic.global.window.webkitAudioContext)())
    else AudioContextProxy.StandardAudioContext(new AudioContext)

  def init: AudioPlayer =
    new AudioPlayer(giveAudioContext())
}

sealed trait AudioContextProxy {

  def createBufferSource(): AudioBufferSourceNode

  def createGain(): GainNode

  def decodeAudioData(
      audioData: js.typedarray.ArrayBuffer,
      successCallback: AudioBuffer => AudioBuffer,
      errorCallback: () => Unit
  ): js.Promise[AudioBuffer]

  val destination: AudioDestinationNode
}
object AudioContextProxy {

  final case class StandardAudioContext(context: AudioContext) extends AudioContextProxy {
    def createBufferSource(): AudioBufferSourceNode =
      context.createBufferSource()

    def createGain(): GainNode =
      context.createGain()

    def decodeAudioData(
        audioData: js.typedarray.ArrayBuffer,
        successCallback: AudioBuffer => AudioBuffer,
        errorCallback: () => Unit
    ): js.Promise[AudioBuffer] =
      context.decodeAudioData(audioData, successCallback, errorCallback)

    val destination: AudioDestinationNode =
      context.destination
  }

  @SuppressWarnings(
    Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.throw")
  )
  final case class WebKitAudioContext(context: js.Dynamic) extends AudioContextProxy {
    import scalajs.js.JSConverters._

    def createBufferSource(): AudioBufferSourceNode =
      context.createBufferSource().asInstanceOf[AudioBufferSourceNode]

    def createGain(): GainNode =
      context.createGain().asInstanceOf[GainNode]

    def decodeAudioData(
        audioData: js.typedarray.ArrayBuffer,
        successCallback: AudioBuffer => AudioBuffer,
        errorCallback: () => Unit
    ): js.Promise[AudioBuffer] =
      Future[AudioBuffer] {
        val decodedBuffer = context.createBuffer(audioData, false).asInstanceOf[AudioBuffer]

        if (decodedBuffer != null && !js.isUndefined(decodedBuffer))
          successCallback(decodedBuffer)
        else
          throw new Exception("Decoding the audio buffer failed");
      }.toJSPromise

    val destination: AudioDestinationNode =
      context.destination.asInstanceOf[AudioDestinationNode]
  }

}

final class AudioPlayer(context: AudioContextProxy) {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var soundAssets: List[LoadedAudioAsset] = Nil

  def addAudioAssets(audioAssets: List[LoadedAudioAsset]): Unit =
    soundAssets = soundAssets ++ audioAssets

  private def setupNodes(audioBuffer: dom.AudioBuffer, volume: Volume, loop: Boolean): AudioNodes = {
    val source = context.createBufferSource()
    source.buffer = audioBuffer
    source.loop = loop

    val gainNode = context.createGain()
    source.connect(gainNode)
    gainNode.connect(context.destination)
    gainNode.gain.value = volume.toDouble

    new AudioNodes(source, gainNode)
  }

  private def findAudioDataByName(assetName: AssetName): Option[dom.AudioBuffer] =
    soundAssets.find(a => a.name == assetName).map(_.data)

  def playSound(assetName: AssetName, volume: Volume): Unit =
    findAudioDataByName(assetName).foreach { sound =>
      setupNodes(sound, volume, loop = false).audioBufferSourceNode.start(0)
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var sourceA: Option[AudioSourceState] = None
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var sourceB: Option[AudioSourceState] = None
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var sourceC: Option[AudioSourceState] = None

  def playAudio(sceneAudioOption: Option[SceneAudio]): Unit = {
    val sceneAudio = sceneAudioOption.getOrElse(SceneAudio.Mute)

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

  private def updateSource(
      sceneAudioSource: Option[SceneAudioSource],
      currentSource: Option[AudioSourceState]
  ): Option[Option[AudioSourceState]] = (currentSource, sceneAudioSource) match {
    case (None, None) =>
      None
    case (Some(playing), Some(next)) if playing.bindingKey == next.bindingKey =>
      None
    case (Some(playing), None) =>
      playing.audioNodes.audioBufferSourceNode.stop()
      Some(None)
    case (_, Some(next)) =>
      Option {
        currentSource.foreach(_.audioNodes.audioBufferSourceNode.stop())

        next.playbackPattern match {
          case PlaybackPattern.SingleTrackLoop(track) =>
            val nodes =
              findAudioDataByName(track.assetName)
                .map(asset => setupNodes(asset, track.volume * next.masterVolume, loop = true))
                .get // throws if no asset found

            nodes.audioBufferSourceNode.start()

            Some(
              new AudioSourceState(
                bindingKey = next.bindingKey,
                audioNodes = nodes
              )
            )
        }
      }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def kill(): Unit =
    soundAssets = Nil
    sourceA = null
    sourceB = null
    sourceC = null
    ()

  private class AudioSourceState(val bindingKey: BindingKey, val audioNodes: AudioNodes)
  private class AudioNodes(val audioBufferSourceNode: AudioBufferSourceNode, val gainNode: GainNode)

}
