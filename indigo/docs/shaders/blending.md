---
id: blending
title: Blending
---

Once you've told Indigo what you'd like your entity to look like, perhaps with a standard material or a custom shader, you then need to tell Indigo how to blend it into you game's scene. This allows you to describe scene level effects.

The blending process goes through the following stages:

1. Blend your entity into the current layer, and all this previous elements of that layer.
2. Blend the current layer onto the previous layers.
3. Blend the whole scene onto the canvas.

The two main building blocks of the blending process are:

1. Blend modes
2. Blend materials

## Blend modes

Blend modes are hardware level functions with limited but important options. They instruct the graphics card on fundamentally how to combine two pixels together.

Explaining blend modes is beyond the scope of this guide, and there are many tutorials available, but as an example, we can compare Indigo's standard entity blend mode with it's entity lighting blend mode (used in image based [lighting](presentation/lighting.md)):

```scala mdoc:silent
import indigo._

// Normal
Blend.Add(BlendFactor.One, BlendFactor.OneMinusSrcAlpha)

// Lighting
Blend.Lighten(BlendFactor.SrcAlpha, BlendFactor.DstAlpha)
```

The normal mode (add source to destination) simple adds (using the add operation) the source to the destination accounting for the alpha.

The lighting version "lightens" the destination pixel with the source pixel.

## Blend materials

For more control over blending, we have blend materials - though you often need to use blend modes in concert with blend materials to get the right effects.

Blend materials work very much like normal materials, the main difference is that instead of reading textures and value to produce graphics, here you are taking a source pixel value, and a destination pixel value, and telling Indigo how to combine them.

Just like other shaders, you can write both vertex and fragment shaders for your blend material.

### Built in blend materials

Indigo comes with three built in blend materials:

1. `Normal` - the default
2. `Lighting` - used with image based lighting
3. `BlendEffects` - which allows you to set the alpha, tint, overlay/fill, and saturation.

There is one additional blend material in the extras package that can be used to (roughly) describe refraction.

## Blending Layers and Scenes

`Layer`s optionally accept a `Blending` instance, which can be used to set:

1. The blend mode for the entities
2. The blend mode for the layer
3. The blend material for the layer
4. The layers clear color (background colour)

Blending the final scene is less fine grained, and a `SceneUpdateFragment` can only accept a `BlendMaterial` not a full `Blending` instance.

## Example: Lighting

Here is the `Blending` description for Indigo's image based [lighting](presentation/lighting.md).

```scala mdoc:silent
val ambientLightColor = RGBA.White.withAlpha(0.25)

Blending(
  entity = Blend.LightingEntity,
  layer = Blend.Normal,
  blendMaterial = BlendMaterial.Lighting(ambientLightColor),
  clearColor = Option(RGBA.Black)
)
```

What is going on here?

Starting at the bottom, we're doing lighting! So the first thing we want to do is "remove all light" so we clear the screen to black.

Entities are then blended onto the layer using the mode we saw before, which multiplies the colors together such that putting a full red dot on a full green dot on a full blue dot would result in a white dot:

```scala mdoc:silent
Blend.Lighten(BlendFactor.SrcAlpha, BlendFactor.DstAlpha)
```

The layer then appears to be blended normally, simply adding this layer onto the layers below it. However this is misleading as the default behavior is overridden in the blend material.

The blend material actually takes the source and destination pixels and multiplies them together along with an ambient light value, to give the appearance that the scene is lit.
