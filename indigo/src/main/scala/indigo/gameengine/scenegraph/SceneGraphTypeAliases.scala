package indigo.gameengine.scenegraph
import indigo.gameengine

trait SceneGraphTypeAliases {

  type SceneUpdateFragment = gameengine.scenegraph.SceneUpdateFragment
  val SceneUpdateFragment: gameengine.scenegraph.SceneUpdateFragment.type = gameengine.scenegraph.SceneUpdateFragment

  // Audio
  type SceneAudio = gameengine.scenegraph.SceneAudio
  val SceneAudio: gameengine.scenegraph.SceneAudio.type = gameengine.scenegraph.SceneAudio

  type Volume = gameengine.scenegraph.Volume
  val Volume: gameengine.scenegraph.Volume.type = gameengine.scenegraph.Volume

  type Track = gameengine.scenegraph.Track
  val Track: gameengine.scenegraph.Track.type = gameengine.scenegraph.Track

  type PlaybackPattern = gameengine.scenegraph.PlaybackPattern
  val PlaybackPattern: gameengine.scenegraph.PlaybackPattern.type = gameengine.scenegraph.PlaybackPattern

  type SceneAudioSource = gameengine.scenegraph.SceneAudioSource
  val SceneAudioSource: gameengine.scenegraph.SceneAudioSource.type = gameengine.scenegraph.SceneAudioSource

  type PlaySound = gameengine.scenegraph.PlaySound
  val PlaySound: gameengine.scenegraph.PlaySound.type = gameengine.scenegraph.PlaySound

  // Animation
  type Animations = gameengine.scenegraph.Animations
  val Animations: gameengine.scenegraph.Animations.type = gameengine.scenegraph.Animations

  type Cycle = gameengine.scenegraph.Cycle
  val Cycle: gameengine.scenegraph.Cycle.type = gameengine.scenegraph.Cycle

  type Frame = gameengine.scenegraph.Frame
  val Frame: gameengine.scenegraph.Frame.type = gameengine.scenegraph.Frame

  type AnimationsKey = gameengine.scenegraph.AnimationsKey
  val AnimationsKey: gameengine.scenegraph.AnimationsKey.type = gameengine.scenegraph.AnimationsKey

  // Primitives
  type Sprite = gameengine.scenegraph.Sprite
  val Sprite: gameengine.scenegraph.Sprite.type = gameengine.scenegraph.Sprite

  type Text = gameengine.scenegraph.Text
  val Text: gameengine.scenegraph.Text.type = gameengine.scenegraph.Text

  type Graphic = gameengine.scenegraph.Graphic
  val Graphic: gameengine.scenegraph.Graphic.type = gameengine.scenegraph.Graphic

  type Group = gameengine.scenegraph.Group
  val Group: gameengine.scenegraph.Group.type = gameengine.scenegraph.Group

}
