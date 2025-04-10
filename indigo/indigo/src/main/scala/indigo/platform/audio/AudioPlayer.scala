package indigo.platform.audio

import indigo.platform.assets.LoadedAudioAsset
import indigo.shared.assets.AssetName
import indigo.shared.audio.PlaybackPolicy
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
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.*

import scala.collection.mutable
import scala.concurrent.Future
import scala.scalajs.js

object AudioPlayer:

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def giveAudioContext(): AudioContextProxy =
    if js.Dynamic.global.window.webkitAudioContext != null &&
      !js.isUndefined(js.Dynamic.global.window.webkitAudioContext)
    then AudioContextProxy.WebKitAudioContext(js.Dynamic.newInstance(js.Dynamic.global.window.webkitAudioContext)())
    else AudioContextProxy.StandardAudioContext(new AudioContext)

  def init: AudioPlayer =
    new AudioPlayer(giveAudioContext())

sealed trait AudioContextProxy:

  def createBufferSource(): AudioBufferSourceNode

  def createGain(): GainNode

  def decodeAudioData(
      audioData: js.typedarray.ArrayBuffer,
      successCallback: AudioBuffer => AudioBuffer,
      errorCallback: () => Unit
  ): js.Promise[AudioBuffer]

  val destination: AudioDestinationNode

object AudioContextProxy:

  final case class StandardAudioContext(context: AudioContext) extends AudioContextProxy:
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

  @SuppressWarnings(
    Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.throw")
  )
  final case class WebKitAudioContext(context: js.Dynamic) extends AudioContextProxy:
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

        if decodedBuffer != null && !js.isUndefined(decodedBuffer) then successCallback(decodedBuffer)
        else throw new Exception("Decoding the audio buffer failed");
      }.toJSPromise

    val destination: AudioDestinationNode =
      context.destination.asInstanceOf[AudioDestinationNode]

final class AudioPlayer(context: AudioContextProxy):

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var soundAssets: Set[LoadedAudioAsset] = Set()

  def addAudioAssets(audioAssets: Set[LoadedAudioAsset]): Unit =
    soundAssets = soundAssets ++ audioAssets

  private def setupNodes(audioBuffer: dom.AudioBuffer, volume: Volume, loop: Boolean): AudioNodes =
    val source = context.createBufferSource()
    source.buffer = audioBuffer
    source.loop = loop

    val gainNode = context.createGain()
    source.connect(gainNode)
    gainNode.connect(context.destination)
    gainNode.gain.value = volume.toDouble

    new AudioNodes(source, gainNode)

  private def findAudioDataByName(assetName: AssetName): Option[dom.AudioBuffer] =
    soundAssets.find(a => a.name == assetName).map(_.data)

  private val soundNodes: mutable.Map[AssetName, AudioNodes] = mutable.HashMap.empty

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def stopSound(assetName: AssetName): Unit =
    soundNodes.remove(assetName).foreach { source =>
      source.audioBufferSourceNode.onended = null
      source.audioBufferSourceNode.stop()
    }

  private def stopAllSound(): Unit =
    soundNodes.keySet.foreach(stopSound)

  def playSound(assetName: AssetName, volume: Volume, policy: PlaybackPolicy): Unit =
    findAudioDataByName(assetName).foreach { sound =>
      val node = setupNodes(sound, volume, loop = false)
      node.audioBufferSourceNode.onended = _ => soundNodes.remove(assetName)
      node.audioBufferSourceNode.start(0)
      policy match {
        case PlaybackPolicy.StopAll          => stopAllSound()
        case PlaybackPolicy.StopPreviousSame => stopSound(assetName)
        case PlaybackPolicy.Continue         => ()
      }
      soundNodes.update(assetName, node)
    }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var sourceA: Option[AudioSourceState] = None
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var sourceB: Option[AudioSourceState] = None
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var sourceC: Option[AudioSourceState] = None

  def playAudio(sceneAudioOption: Option[SceneAudio]): Unit =
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

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def updateSource(
      sceneAudioSource: Option[SceneAudioSource],
      currentSource: Option[AudioSourceState]
  ): Option[Option[AudioSourceState]] =
    (currentSource, sceneAudioSource) match
      case (None, None) =>
        None

      case (Some(playing), Some(next)) if noChange(playing, next) =>
        None

      case (Some(playing), Some(next)) if needsVolumeChange(playing, next) =>
        next.playbackPattern match
          case PlaybackPattern.SingleTrackLoop(track) =>
            val volume = track.volume * next.masterVolume

            val gainNode = playing.audioNodes.gainNode
            gainNode.gain.value = volume.toDouble

            val nodes = new AudioNodes(
              audioBufferSourceNode = playing.audioNodes.audioBufferSourceNode,
              gainNode = gainNode
            )

            Option(
              Option(
                new AudioSourceState(
                  bindingKey = next.bindingKey,
                  volume = volume,
                  audioNodes = nodes
                )
              )
            )

      case (Some(playing), None) =>
        playing.audioNodes.audioBufferSourceNode.stop()
        Some(None)

      case (_, Some(next)) =>
        Option {
          currentSource.foreach(_.audioNodes.audioBufferSourceNode.stop())

          next.playbackPattern match
            case PlaybackPattern.SingleTrackLoop(track) =>
              val volume = track.volume * next.masterVolume
              val nodes =
                findAudioDataByName(track.assetName)
                  .map(asset => setupNodes(asset, volume, loop = true))
                  .getOrElse {
                    throw new Exception("Failed to find audio for track with name: " + track.assetName)
                  }

              nodes.audioBufferSourceNode.start()

              Some(
                new AudioSourceState(
                  bindingKey = next.bindingKey,
                  volume = volume,
                  audioNodes = nodes
                )
              )
        }

  private def noChange(playing: AudioSourceState, next: SceneAudioSource): Boolean =
    playing.bindingKey == next.bindingKey && (playing.volume ~== next.volume)

  private def needsVolumeChange(playing: AudioSourceState, next: SceneAudioSource): Boolean =
    playing.bindingKey == next.bindingKey && !(playing.volume ~== next.volume)

  def kill(): Unit =
    soundAssets = Set()
    playAudio(None)
    ()

  private class AudioSourceState(val bindingKey: BindingKey, val volume: Volume, val audioNodes: AudioNodes)
  private class AudioNodes(val audioBufferSourceNode: AudioBufferSourceNode, val gainNode: GainNode)
