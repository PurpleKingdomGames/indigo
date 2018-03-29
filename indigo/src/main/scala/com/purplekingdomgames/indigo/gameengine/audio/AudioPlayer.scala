package com.purplekingdomgames.indigo.gameengine.audio

import com.purplekingdomgames.indigo.gameengine.assets.LoadedAudioAsset
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneAudio
import org.scalajs.dom.raw.AudioContext

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