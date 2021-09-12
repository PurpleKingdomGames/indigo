---
id: audio
title: Audio
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Indigo's audio support is fairly unsophisticated, but probably good enough for the kind of games Indigo was designed for. The authors are not audio experts, please feel free to raise issues if you have any suggestions for improvements.

There are two kinds of audio support in Indigo:

1. Sound effects
2. Background music

## Audio formats

Indigo uses web technologies, so you should refer to current recommended practices for audio formats. That being said, anything a browser will play Indigo will play, and most of the existing demos and examples just use MP3 files, but that is a fairly dated format these days.

Please refer to the assets documentation for information on [how to load audio files](platform/assets.md).

## Sound effects

Sound effects are tiny clips of sound that you will play many times, and are often triggered by some event in your game, such as a character jumping or a button push.

To play a sound effect you simply need to emit an event at the end of an update or scene draw and Indigo will do the rest, e.g.:

```scala mdoc
Outcome(updateModel).addGlobalEvents(PlaySound(AssetName("twang!"), Volume(0.5)))
```

or

```scala mdoc
SceneUpdateFragment.empty.addGlobalEvents(PlaySound(AssetName("bounce"), Volume.Max))
```

## Background Music

Background music must be added as part of your scene description, as follows:

```scala mdoc
SceneUpdateFragment(mySceneNodes)
  .withAudio(
    SceneAudio(
      SceneAudioSource(
        BindingKey("My bg music"),
        PlaybackPattern.SingleTrackLoop(Track(AssetName("music")))
      )
    )
  )
```

A `SceneAudio` can describe up to three music channels at once, called `SceneAudioSource`s. A `SceneAudioSource` specifics a `BindingKey` to keep track of what is being played and it's progression state, and a playback pattern which at the moment can only be either `PlaybackPattern.SingleTrackLoop` or `Silent`. `SingleTrackLoop` takes a `Track` which is very similar to the `PlaySound` type above, taking an `AssetName` to tell it what to play, and optionally a `Volume`.
