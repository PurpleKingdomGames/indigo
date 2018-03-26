package com.purplekingdomgames.indigo.gameengine.audio

import com.purplekingdomgames.indigo.gameengine.assets.LoadedAudioAsset
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
}

final class AudioPlayerImpl(loadedAudioAssets: List[LoadedAudioAsset], context: AudioContext) extends IAudioPlayer {

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

}