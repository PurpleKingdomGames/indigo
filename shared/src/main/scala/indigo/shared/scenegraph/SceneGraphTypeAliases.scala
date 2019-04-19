package indigo.shared.scenegraph

import indigo.shared

trait SceneGraphTypeAliases {

  type SceneUpdateFragment = shared.scenegraph.SceneUpdateFragment
  val SceneUpdateFragment: shared.scenegraph.SceneUpdateFragment.type = shared.scenegraph.SceneUpdateFragment

  type SceneGraphNode = shared.scenegraph.SceneGraphNode
  val SceneGraphNode: shared.scenegraph.SceneGraphNode.type = shared.scenegraph.SceneGraphNode

  type Renderable = shared.scenegraph.Renderable

  // Audio
  type SceneAudio = shared.scenegraph.SceneAudio
  val SceneAudio: shared.scenegraph.SceneAudio.type = shared.scenegraph.SceneAudio

  type Volume = shared.scenegraph.Volume
  val Volume: shared.scenegraph.Volume.type = shared.scenegraph.Volume

  type Track = shared.scenegraph.Track
  val Track: shared.scenegraph.Track.type = shared.scenegraph.Track

  type PlaybackPattern = shared.scenegraph.PlaybackPattern
  val PlaybackPattern: shared.scenegraph.PlaybackPattern.type = shared.scenegraph.PlaybackPattern

  type SceneAudioSource = shared.scenegraph.SceneAudioSource
  val SceneAudioSource: shared.scenegraph.SceneAudioSource.type = shared.scenegraph.SceneAudioSource

  // Animation
  type Animation = shared.scenegraph.animation.Animation
  val Animation: shared.scenegraph.animation.Animation.type = shared.scenegraph.animation.Animation

  type Cycle = shared.scenegraph.animation.Cycle
  val Cycle: shared.scenegraph.animation.Cycle.type = shared.scenegraph.animation.Cycle

  type CycleLabel = shared.scenegraph.animation.CycleLabel
  val CycleLabel: shared.scenegraph.animation.CycleLabel.type = shared.scenegraph.animation.CycleLabel

  type Frame = shared.scenegraph.animation.Frame
  val Frame: shared.scenegraph.animation.Frame.type = shared.scenegraph.animation.Frame

  type AnimationKey = shared.scenegraph.animation.AnimationKey
  val AnimationKey: shared.scenegraph.animation.AnimationKey.type = shared.scenegraph.animation.AnimationKey

  type AnimationAction = shared.scenegraph.animation.AnimationAction
  val AnimationAction: shared.scenegraph.animation.AnimationAction.type = shared.scenegraph.animation.AnimationAction

  // Primitives
  // type Sprite = shared.scenegraph.Sprite
  // val Sprite: shared.scenegraph.Sprite.type = shared.scenegraph.Sprite

  // type Text = shared.scenegraph.Text
  // val Text: shared.scenegraph.Text.type = shared.scenegraph.Text

  // type Graphic = shared.scenegraph.Graphic
  // val Graphic: shared.scenegraph.Graphic.type = shared.scenegraph.Graphic

  type Group = shared.scenegraph.Group
  val Group: shared.scenegraph.Group.type = shared.scenegraph.Group

}
