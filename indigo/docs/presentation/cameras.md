---
id: cameras
title: Cameras
---

When you look at a scene rendered by Indigo, you are looking through a default camera.

This camera produces a viewport with pixel coordinates that start at (0,0) in the top left and proceed down and right to the width and height specified in the game config.

The default camera is always at 1:1 scale (regardless of magnification), and is never rotated.

Adding a big background to the scene at coordinates (0,0) places it at the top left and as much of it as possible will be drawn into the viewport.

What if you want to pan across that background? Perhaps to follow a little character as they wander across the world?

Well, one solution is to keep the character in the middle of the screen and update the coordinates of everything in the background all the time, to create the appearance of the character moving forward.

That's a valid approach, but it adds a bit of complexity. While the character might be in the scene's model correctly, the translation from model to view is a bit tricky.

The alternative is to render the scene more in line with the model, and have a camera follow the character around, removing a fair bit of programming complexity.

## Types of camera

There are two types of camera built into Indigo: 'Fixed' and 'Look at'.

Fixed cameras are very much the same as the Indigo default camera, except that you can zoom and rotate them. Placing a `Camera.Fixed` at position (10, 10), effectively moves (10,10) to (0,0) for rendering purposes.

Here is an example of a fixed camera, positioned at (10, 10), zoomed in by a factor of 2, and rotated by 45 degrees:

```scala mdoc:js:shared
import indigo.*
```

```scala mdoc:js
Camera.Fixed(Point(10, 10), Zoom.x2, Radians.fromDegrees(45))
```

`Camera.LookAt` works differently, in that targeting a 'look at' camera at (10,10) centers position (10,10) on the screen. Here is the same camera but as a 'look at', which centers the `target` position on the screen.

```scala mdoc:js
Camera.LookAt(Point(10, 10), Zoom.x2, Radians.fromDegrees(45))
```

## Using cameras

Cameras can be optionally added to your `SceneUpdateFragment`s or `Layer`s, e.g.:

```scala mdoc:js
Layer(BindingKey("my layer"))
  .withCamera(Camera.LookAt(Point(10, 10), Zoom.x2, Radians.fromDegrees(45)))
```

Which camera is used follows some simple fall back logic:

1. If a camera is defined for the current `Layer`, use that one.
2. Otherwise, if a camera is defined for the `SceneUpdateFragment`, use that one
3. Otherwise, fall back to the default camera.

Cameras are affected by `Layer` and `SceneUpdateFragment` composition, ***and they behave differently.***

- In scene update fragments, the **last added** camera is the one that will be used.
- In layers, that the **earliest defined** camera is assumed to be desired one, and is retained.

In this example, the camera in `sceneB` looking at position `Point(20, 20)` is retained:

```scala mdoc:js
val sceneA = SceneUpdateFragment.empty.withCamera(Camera.LookAt(Point(10, 10)))
val sceneB = SceneUpdateFragment.empty.withCamera(Camera.LookAt(Point(20, 20)))
```

```scala
(sceneA |+| sceneB).camera
```

..where as here, the camera in `layerA` looking at position `Point(10, 10)` is selected.

```scala mdoc:js
val layerA = Layer(BindingKey("Layer A")).withCamera(Camera.LookAt(Point(10, 10)))
val layerB = Layer(BindingKey("Layer B")).withCamera(Camera.LookAt(Point(20, 20)))
```

```scala
(layerA |+| layerB).camera
```

> `Layer`s and `SceneUpdateFragments` without cameras that are added to scenes with cameras retain the defined one.

Why the difference in behavior? There is an assumed difference in intent - whether this was the right decision or not is debatable - but nonetheless, the assumptions are as follows:

- `SceneUpdateFragment` - there is only ultimately one, and the idea is that you are "building up" to a renderable result. Hence the latest is assumed to be the intended one.
- `Layer` - a common pattern is to define empty placeholder layers and use them as rendering destinations. So rather than "building up" you are "squashing down."
