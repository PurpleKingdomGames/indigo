package com.purplekingdomgames.indigo.gameengine.audio

import com.purplekingdomgames.indigo.gameengine.assets.LoadedAudioAsset
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneAudio
import org.scalajs.dom.{AudioBufferSourceNode, AudioDestinationNode, GainNode}
import org.scalajs.dom.raw.{AudioBuffer, AudioContext}

object AudioPlayer {

  val MaxSoundEffects: Int = 5

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

  private val pool: SoundEffectPool = new SoundEffectPool(AudioPlayer.MaxSoundEffects, context)

  def playSound(assetRef: String, volume: Double): Unit = {
    loadedAudioAssets.find(_.name == assetRef).foreach { sound =>
      pool.addAndPlay(sound, volume)
    }
  }

  //TODO: Set up dedicated sources and gain nodes for the three that can be described.
  def playAudio(sceneAudio: SceneAudio): Unit = {}

}

class SoundEffectPool(size: Int, context: AudioContext) {

  private var position: Int = 0
  private val pool: scalajs.js.Array[AudioNode] = new scalajs.js.Array[AudioNode](size)

  private def populate(): Unit = {
    (0 until size).foreach { i =>
      println("> " + i)
      val source = context.createBufferSource()
      val gainNode = context.createGain()
      source.connect(gainNode)

      pool.update(i, new AudioNode(source, gainNode))
    }
  }

  def addAndPlay(audioAsset: LoadedAudioAsset, volume: Double): Unit = {

    println(position)
    val node: AudioNode = pool(position)

    pool.update(
      position,
      node
        .replaceBuffer(audioAsset.data)
        .connectToDestination(context.destination)
        .setVolume(volume)
        .startPlayback()
    )

    position = if(position + 1 < size) position + 1 else 0
  }

  populate()

}

class AudioNode(audioBufferSourceNode: AudioBufferSourceNode, gainNode: GainNode) {

  def replaceBuffer(audioBuffer: AudioBuffer): AudioNode = {
    println("replaceBuffer")
    audioBufferSourceNode.buffer = audioBuffer
    this
  }

  def connectToDestination(audioDestinationNode: AudioDestinationNode): AudioNode = {
    println("connectToDestination")
    gainNode.connect(audioDestinationNode)
    this
  }

  def disconnectFromDestination(audioDestinationNode: AudioDestinationNode): AudioNode = {
    println("disconnectFromDestination")
    gainNode.disconnect(audioDestinationNode)
    this
  }

  def setVolume(volume: Double): AudioNode = {
    println("setVolume")
    gainNode.gain.value = volume
    this
  }

  def startPlayback(): AudioNode = {
    println("startPlayback")
    audioBufferSourceNode.start(0)
    this
  }

}