package com.purplekingdomgames.indigo.gameengine.audio

import com.purplekingdomgames.indigo.gameengine.assets.LoadedAudioAsset
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneAudio
import org.scalajs.dom.{AudioBufferSourceNode, AudioDestinationNode, GainNode}
import org.scalajs.dom.raw.{AudioBuffer, AudioContext}

object AudioPlayer {

  private var player: Option[IAudioPlayer] = None

  def apply(loadedAudioAssets: List[LoadedAudioAsset]): IAudioPlayer = {
    player match {
      case Some(p) => p
      case None =>

        val p = new AudioPlayerImpl(loadedAudioAssets, new AudioContext())

        player = Some(p)
        player.get
    }
  }

}

trait IAudioPlayer {
  def playSound(assetRef: String, volume: Double): Unit
  def playAudio(sceneAudio: SceneAudio): Unit
}

final class AudioPlayerImpl(loadedAudioAssets: List[LoadedAudioAsset], context: AudioContext) extends IAudioPlayer {

  //TODO: Should use a pool of buffer / gain node combos?
  def playSound(assetRef: String, volume: Double): Unit = {
    loadedAudioAssets.find(_.name == assetRef).foreach { sound =>
      val source = context.createBufferSource()
      source.buffer = sound.data

      val gainNode = context.createGain()
      source.connect(gainNode)
      gainNode.connect(context.destination)
      gainNode.gain.value = volume

      source.start(0)
    }
  }

  //TODO: Set up dedicated sources and gain nodes for the three that can be described.
  def playAudio(sceneAudio: SceneAudio): Unit = {}

}

class SoundEffectPool(size: Int, context: AudioContext) {

  private var position: Int = 0
  private val pool: scalajs.js.Array[AudioNode] = new scalajs.js.Array[AudioNode](size)

  def populate(): Unit = {
    (0 until size).foreach { _ =>
      val source = context.createBufferSource()
      val gainNode = context.createGain()
      source.connect(gainNode)

      pool.push(new AudioNode(source, gainNode))
    }
  }

  def addAndPlay(audioAsset: LoadedAudioAsset, volume: Double): Unit = {
    val node: AudioNode = pool(position)

    pool(position) = node
      .replaceBuffer(audioAsset.data)
      .connectToDestination(context.destination)
      .setVolume(volume)
      .startPlayback()

    position = if(position + 1 < size) position + 1 else 0
  }

}

class AudioNode(audioBufferSourceNode: AudioBufferSourceNode, gainNode: GainNode) {

  def replaceBuffer(audioBuffer: AudioBuffer): AudioNode = {
    audioBufferSourceNode.buffer = audioBuffer
    this
  }

  def connectToDestination(audioDestinationNode: AudioDestinationNode): AudioNode = {
    gainNode.connect(audioDestinationNode)
    this
  }

  def disconnectFromDestination(audioDestinationNode: AudioDestinationNode): AudioNode = {
    gainNode.disconnect(audioDestinationNode)
    this
  }

  def setVolume(volume: Double): AudioNode = {
    gainNode.gain.value = volume
    this
  }

  def startPlayback(): AudioNode = {
    audioBufferSourceNode.start(0)
    this
  }

}