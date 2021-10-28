---
id: scene-update-fragment
title: SceneUpdateFragment
---

The `SceneUpdateFragment` is one of the most important types in Indigo, as it is the type that describes everything you want your player to experience.

Below is an example of it's usage:

```scala mdoc:silent
import indigo._

val graphic =
  Graphic(50, 50, Material.Bitmap(AssetName("placeholder"))
    .withLighting(LightingModel.Lit.flat))

SceneUpdateFragment(
    Layer(BindingKey("game layer"), graphic)
  )
  .withLights(
    AmbientLight(RGBA.White.withAmount(0.25)),
    PointLight.default
      .moveTo(Point(50, 50))
      .withColor(RGBA.Green)
  )
```

Unlike `Outcome`, `SceneUpdateFragment`s are not Functors, but they are Monoids, which is to say that:

1. There is a concept of an identity value, `SceneUpdateFragment.empty`, that if you add it to another `SceneUpdateFragment` it has no effect.
2. There is an append operation `|+|` for combining to of them together.

This is really important as it allows you to build parts of your scene up in lots of different ways, and then easily and reliably combine all the results together at the end. For example:

```scala
val sceneAudio: SceneUpdateFragment = ???
val background: SceneUpdateFragment = ???
val clouds: SceneUpdateFragment = ???
val player: SceneUpdateFragment = ???
val foreground: SceneUpdateFragment = ???

sceneAudio |+| background |+| clouds |+| player |+| foreground
```

Consider also the following:

```scala mdoc:silent
val visible = true
val vanishingThing =
  if(visible) SceneUpdateFragment(graphic)
  else SceneUpdateFragment.empty

SceneUpdateFragment(graphic) |+| vanishingThing
```

Or this:

```scala mdoc:silent
val scene = SceneUpdateFragment(graphic)
val l: List[SceneUpdateFragment] = List(scene, scene, scene)

l.foldLeft(SceneUpdateFragment.empty)(_ |+| _)
```

There are some special rules about what happens when you combine `SceneUpdateFragment`s that optionally have things like cameras. The rules are: Lists are concatenated in the expected way. Optional elements (cameras, blend materials, scene audio elements) are "appended" such that a defined element is always taken in preference to an undefined element, and if two are defined, the last or "incoming" version is assumed to be the desired one. Consider the following illustration:

```scala mdoc:silent
val a: Option[Int] = Some(1)
val b: Option[Int] = None

(a, b) match
  case (None, None)       => None
  case (Some(i), None)    => Some(i)
  case (None, Some(i))    => Some(i)
  case (Some(_), Some(i)) => Some(i)
```

## Relationship with `Layer`s

Layers describe horizontal bands through the total scene that need to be drawn. Ultimately all layers are part of the final scene, and as such the resulting `SceneUpdateFragment` acts in some regards as a fall back or default state. For example, if a camera is defined on a layer it will be used first, but if not the scene level camera will be used (or else a default).

An exception to this is lights. Since there is no need to set them, scene level lights are assumed to be important. You can display a maximum of 8 lights per layer (they can be different on each layer) and Indigo chooses the lights to use by first selecting all the scene lights, then adding on all the layer lights, and taking the first 8.

It is worth noting that when you append two scene update fragments together where they both contains [layers](layers.md) that have the same binding keys, those layers will be joined together with the original layer positions preserved. A good pattern is to establish an initial placeholder scene with the layers you need in place, empty but correctly ordered. Then whenever you append a new scene update fragment, the contents will be added to the right places in the expected order.

## What can you describe?

The `SceneUpdateFragment` has a fairly rich API that you can explore, but at a high level allows you to describe the following:

- **Layers**: A list of [layers](layers.md) to help structure your visuals and how they are presented.
- **Lights**: A list of dynamic [lights](lighting.md) that affect nodes with the right materials.
- **Audio**: Background [audio](audio.md) tracks and volume mixing.
- **A scene blend material**: Used in [blending](shaders/blending.md), unlike layers you cannot change the blend mode, only the blend material.
- **Clone blanks**: A list of nodes used as look up reference for Cloning.
- **Scene Camera**: A default [camera](camera.md).
