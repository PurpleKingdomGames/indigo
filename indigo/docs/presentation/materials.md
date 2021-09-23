---
id: materials
title: Materials
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Indigo is intended to be a pixel art based game engine, and that means drawing pixels!

## Painting a picture

Currently, Indigo takes a very simple approach to building up a rendered frame: It uses a painters algorithm to draw all the images one at a time, ordered from most distant from the camera, to nearest. In some ways, it isn't even as complicated as [blitting](https://en.wikipedia.org/wiki/Bit_blit).

To tell Indigo what to draw we need to point it at an image asset using a `Material`. There are two standard material types, `Bitmap` and `ImageEffects`.

> You can create your own materials, and Indigo comes with two other materials in the "extras" library: `LegacyEffects` and materials relating to refraction.

```scala mdoc
Material.Bitmap(AssetName("funny cat"))
```

`Bitmap` is a completely flat texture and can be used in a graphic like this:

```scala mdoc
SceneUpdateFragment(
  Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(AssetName("funny cat")))
)
```

Graphics are nice and simple to follow because they use materials directly, but materials also turn up in `Text` and `Sprite` primitives indirectly inside their `FontInfo` and `Animation` classes respectively.

## Effects

The `Bitmap` material is quick and simple for quickly throwing images onto the screen, but has limited display options (keeping it light weight!). By switching to the `ImageEffects` material you get a lot more control over how your texture is displayed at the minor cost of some additional processing overhead.

> In previous Indigo versions materials and primitives were blended together, and came with additional (cheap) effects like limited glows and borders. These are considered low quality, but you can still access them via the `LegacyEffects` material.

```scala mdoc
val graphic = Graphic(32, 32, Material.ImageEffects(AssetName("funny cat")))
```

In the examples that follow, we take this graphic instance and use the `modifyMaterial` method to alter it's properties.

### Alpha

Sets the transparency of the texture.

```scala mdoc
graphic.modifyMaterial(_.withAlpha(0.5))
```

### Saturation

Sets the colour saturation level of the texture where `1.0` is full normal colour and `0.0` is gray scale.

```scala mdoc
graphic.modifyMaterial(_.withSaturation(0.5))
```

### Tint

Tint essentially sets the saturation level of each color channel, like looking through a piece of transparent colored plastic. Examples:

- `RGBA.White` tint doesn't color the graphic white, it leaves it looking normal
- `RGBA.Black` absorbs all the light and the nodes pixels end up black (alpha is respected).
- `RGBA.Red` which is (r=1.0, g=0.0, b=0.0, a=1.0) sucks the blue and green out of the image (like standing in a chemical photography dark room).

```scala mdoc
graphic.modifyMaterial(_.withTint(RGBA.Red))
```

### Color Overlay

Where tint removes color, color overlay adds it. So to take the same examples:

- `RGBA.White` Makes the image white (alpha is respected).
- `RGBA.Black` Makes the image black (alpha is respected).
- `RGBA.Red` Makes the image red (alpha is respected).
- `RGBA.Red.withRed(0.5)` which is (r=0.5, g=0.0, b=0.0, a=1.0) adds 50% red to each pixel up to a maximum value of 1.0.

```scala mdoc
graphic.modifyMaterial(_.withOverlay(Overlay.Color(RGBA.Red)))
```

### Gradient Overlay

Gradients can be linear or radial and work in the same way as color overlay but allows a gradient to be applied instead of one solid color. The `Point` positions are relative to the node being drawn and move around with the node.

```scala mdoc
// For a 16x16 graphic where we want to go top left to bottom right
graphic.modifyMaterial(
  _.withOverlay(
    Overlay.LinearGradiant(
      fromPoint = Point.zero,
      fromColor = RGBA.Magenta,
      toPoint = Point(16, 16),
      toColor = RGBA.Cyan
    )
  )
)
```

## Filling in the space

The standard materials support options for telling Indigo how to fill the space with the material.

```scala mdoc
val material = Material.Bitmap(AssetName("funny cat"))

material.normal
material.stretch
material.tile

// The above methods are aliases for:
material.withFillType(FillType.Tile)
```

When you describe a primitive like a graphic, you have to give it a size. However that size does not have any direct relationship with the dimensions of the texture being used in the material. For example, you could have a graphic with a width and height of 100 x 100, and add a material with a texture that is only 50 x 50. What happens to all the extra space?

- In "normal" mode, the space is left empty.
- In "stretch" mode, the texture is stretched (deformed) to fill the available space.
- In "tile" mode, the texture is repeated to fill the space available.

## Light it up

`Bitmap` has an intriguing method on it called `.lit`. Textures are supposed to be completely flat and colored in their full original glory, perhaps with a bit of ambient light provided by the lighting layer. What then does `.lit` do?

The `lit` method tells Indigo that although this texture is flat, you'd like it to receive light from any light sources that are in the scene. If you put a point light next to it you'll end up with a light applied to the texture as if it was a smooth surface.

Indigo might be a pixel art engine, but modern pixel art often takes advantage of the underlying 3D hardware to create dynamic lighting effects in order to affect the mood of the game, but a flat surface isn't terrible interesting.

## The illusion of depth and texture

What we want to do, is take a completely flat texture and make it look as if it is in relief, with corners and curves and angles that catch the light.

To do that we use a different material:

```scala mdoc
Material.Lit(albedo: AssetName, emissive: AssetName, normal: AssetName, specular: AssetName)
```

> You can call `.unlit` on a `Lit` material to have to render like a `Bitmap` material.

In this material, each parameter represents a different aspect of how the texture is lit. Briefly they are:

1. Albedo - the flat color of a texture, typically with all baked in shadows and highlights removed. (Full color)
2. Emissive - the part of a texture that glows in the dark. (Full color)
3. Normal - describes the bumpiness of a texture (see below)
4. Specular - describes how shiny different parts of the texture are. Polished metal vs. cloth. (Gray scale)

## Normal mapping

To light a surface we need to know it's [normal](https://en.wikipedia.org/wiki/Normal_(geometry)). A normal is just an arrow / vector that points away from a surface at a tangent to the plane. Imagine a coffee table with an arrow pointing at the ceiling.

The normal is used to work out how much light from a light source makes it to the camera / eye and therefore allows us (as people) to interpret what angle the surface was at.

Consider a sphere which is a single surface. The angle of the normal of a sphere's surface rotates around depending on which point of the sphere you're looking at. This change in normal is what gives the appearance of a smooth spherical surface, as a pose to a flat circle.

Knowing that, we can take a completely flat image and _pretend_ it's bumpy or textured by _bending_ the real normal to a new angle at different co-ordinates on the texture.

### Height / Bump maps

If you want to describe bumpiness (i.e. the height) of a mountain range, on a 2D top down image, then a good way to do that is the simply draw a gray scale image where black is sea level and white is the maximum height of the mountains.

This is called a [height or bump map](https://en.wikipedia.org/wiki/Heightmap).

The great thing about height / bump maps is that they're really easy for humans to figure out, and even to have some intuition about how to draw by hand. But they are really inefficient in two very specific ways:

1. They're a very poor use of data. You've probably drawn your gray scale image and saved it as a full RGBA PNG, even though you could pack all of the data into a single channel. Weird to think about, maybe, but R and G and B and A are really gray scale i.e. values from 0 to 1 or 0 to 255, meaning you can have 4 gray scale images in one!

2. To calculate a normal from a bump map, the graphics engine has to process the image by looking at any given pixel and calculating the rate of change between it and it's neighbors. Either as a pre-processing step or on the fly.

### Normal maps

Normal maps solve both those problems at once. A normal is a 3 part vector that describes angle and magnitude with a single co-ordinate. For example a vector `(2, 5, 1)` describes a point in space, relative to the origin `(0, 0, 0)` and from the point we can determine amount / distance and angle.

What you can do then, is encode the deformed normal at any given pixel into an RGB image where red is the X, green is a Y, and blue is the Z. Now we have nicely pre-packed data that requires no processing.

### Making normal maps

Normal maps are complicated and are usually generated by a computer from very high resolution 3D models and then applied to lower resolution models to give the appearance of there being more complexity than is really present.

However, given that Indigo is going to be used for 2D graphics and probably pixel art, chances are that you don't have any high res 3D models of your artwork. So how will you make your normal maps?

As stated earlier:

- Humans are good at making height maps.
- Equally, humans are generally _not_ good at hand crafting normal maps.

But, both formats encode normals in either an unprocessed or pre-processed way.

[Wouldn't it be nice if someone had made a tool that converted bump maps to normal maps for you?](https://indigoengine.io/tools/)
