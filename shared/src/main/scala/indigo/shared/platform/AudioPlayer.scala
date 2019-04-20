package indigo.shared.platform

import indigo.shared.audio.Volume
import indigo.shared.scenegraph.SceneAudio
 
trait AudioPlayer {
  def playSound(assetRef: String, volume: Volume): Unit
  def playAudio(sceneAudio: SceneAudio): Unit
}