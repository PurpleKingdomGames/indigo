---
id: materials
title: Materials
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Indigo is intended to be a pixel art based game engine, and that means drawing pixels!

## Painting a picture

Currently, Indigo takes a very simple approach to building up a rendered frame: It uses a painters algorithm to draw all the images one at a time, ordered from most distant from the camera, to nearest. In some ways, it isn't even as complicated as [blitting](https://en.wikipedia.org/wiki/Bit_blit).

To tell Indigo what to draw we need to point it at an image asset using a `Material`. There are two material types, the simplest is `Bitmap`.

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

## Light it up!

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
