---
id: depth-and-layers
title: Depth & Layers
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

One thing you will need to consider when building your game, is the order that visual elements are drawn in.

Indigo has two concepts of ordering: Depth and Layers.

>Warning! Neither work how you might expect, please read on!

## Depth

Depth is quite straight forward. All visual elements can be given a `Depth`, which is just a number.

Zero is right in front of the camera, and bigger numbers are further away. It makes sense if you think in terms of "I am the camera and it's further away from me, so it's a bigger number".

If several things have the same depth, the elements are draw in the order given to the `SceneUpdateFragment` e.g.:

```scala mdoc
import indigo._

List(
  Graphic(10, 10, Material.Bitmap(AssetName("texture 1"))), // drawn first (on the bottom)
  Graphic(10, 10, Material.Bitmap(AssetName("texture 2"))), // drawn second (on top of graphic1)
  Graphic(10, 10, Material.Bitmap(AssetName("texture 3")))  // drawn third (drawn last on top of graphic2)
)
```

`Group`s are a special case. `Group`s have child nodes, which can also be other groups. When a group is encountered, all of it's children inherit the groups depth and then have there own depth added to it, in effect allowing you to start again at zero i.e. Group with depth 1000 has a child with depth 10, the child node's final depth is 1010.

## Layers

> Layers are probably **NOT** what you're expecting if you've used any modern design software.

Layers are typically added to and have their various properties set via `SceneUpdateFragment`.

Indigo has 5 layers. Exactly 5, always 5. In order they are:

1. The Game Layer
2. The Dynamic Lighting Layer (currently WebGL 2.0 only)
3. The Image Based Lighting Layer
4. The Distortion Layer (currently WebGL 2.0 only)
5. The UI Layer

Some of the layers can have their magnifications changed, they are the game, image based lighting and ui layers.

> Note: The depth of objects are only relevant within the layer the object is being draw in, a graphic with `Depth(1)` in the game layer with still be underneath and button with `Depth(10000)` on the ui layer.

### The Game and UI layers

The Game and UI layers are very similar, and are rendered the same way. You can set the magnification of them independently - perhaps you'd like a slick HD UI over funky retro pixels?

These two layers work exactly how you'd expect, you add scene graph nodes and they're drawn as instructed.

### Dynamic lighting

This layer allows you to light your 2D artwork with faux-3D point, spot, and directional lights, as long as you use the correct material settings.

Example usage: A revolving warning light or light from a flickering torch.

### Image Based Lighting

Appealing to the pixel art crowd, sometimes you'd like to paint pixelated volumetric lighting right onto the scene with an image, or apply and ambient light to everything.

This layer accepts normal scene objects, but at render time treats them differently to create light and shadow in your scene.

Example: You'd like to project a solid polygon of lamp light down onto a street and have everything that walks through it uniformly light up. If you also wanted an edge lighting effect, you could couple this with a dynamic spotlight.

### Distortion layer

The distortion layer is quite odd.

The aim of the layer is to allow you to warp the game image, think of simulating a concussion blast or heat rising off a fire. It is a screen level effect, and cannot be isolated to particular objects.

This works by assuming that everything it is give to render is a [normal map](https://en.wikipedia.org/wiki/Normal_mapping).

Indigo in general does not process things on the fly, and so does not currently generate the normal maps! We may try and make this process more friendly in the future, but for now you need to generate normal maps yourself. Luckily! We're here to help, you can convert any hand drawn or acquired [height map / bump map](https://en.wikipedia.org/wiki/Heightmap) into a normal map using our [free handy online tool](https://indigoengine.io/tools/)!

## Why are the layers like this?

If you could write custom shaders for indigo, perhaps none of this would be necessary. We do plan to add support for custom shaders in some form in a future release.

Even if custom shaders were an option however, part of the mission of indigo is to let programmers build games in an accessible and productive way. We want people to focus on building their games. The layering system may seem unusual, but the cumulative effect is pretty powerful, and requires little specialist knowledge to use.
