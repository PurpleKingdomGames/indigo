# Materials

Indigo is intended to be a pixel art based game engine, and that means drawing pixels!

## Painting a picture

Indigo takes a very simple approach to building up a rendered frame: It uses a Painters Algorithm (also called 'Back-to-Front Rendering') to draw all the images one at a time, ordered from most distant from the camera, to nearest.

The main reason for this is that [Rendering transparency on 3D hardware is complicated](https://developer.nvidia.com/content/transparency-or-translucency-rendering), and for all intents and purposes, everything in 2D graphics is transparent.

To tell Indigo what to draw we need to point it at an image asset using a `Material`. There are two standard material types, `Bitmap` and `ImageEffects`.

You can create your own materials, and Indigo comes with two other materials in the "extras" library (that we won't cover here): `LegacyEffects` that mimic older version of Indigo, and a pair of materials for rendering refraction effects.

### `Material.Bitmap`

This is the `Bitmap` material:

```scala mdoc:js:shared
import indigo.*

Material.Bitmap(AssetName("funny cat"))
```

`Bitmap` is a simple flat texture that does nothing more than take the texture and render it as-is. It can be used in a `Graphic` like this:

```scala mdoc:js
SceneUpdateFragment(
  Graphic(32, 32, Material.Bitmap(AssetName("funny cat")))
)
```

The relationship between `Graphic`s and `Material`s is nice and simple to understand because they use materials directly. Materials also turn up in `Text` and `Sprite` primitives too, but indirectly inside their `FontInfo` and `Animation` classes respectively.

### `Material.ImageEffects`

The other standard material is called `ImageEffects`. In essence it does the same thing that `Bitmap` does, i.e. copy pixels from the texture and render them in your primitive. However it comes with more properties you can change to alter how the texture is drawn:

|Property|Type|Details|
|---|---|---|
|alpha|`Double`|Set the transparency, clamped from `0.0` to `1.0`.|
|tint|`RGBA`|Like looking through colored glass, keeps the color specified. For example, `RGBA.Red` would _remove_ the green and blue from the texture.|
|overlay|`Fill.None`, `Fill.Color`, `Fill.LinearGradient`, `Fill.RadialGradient`|Draws a pixel from the texture, and then overdraws it with a color specified by the type of fill.|
|saturation|`Double`|`0.0` gray scale to `1.0` full color.|

The cost of these additional properties is that the `ImageEffect` material is a _little_ bit more expensive to use than `Bitmap`. Not expensive enough to worry about for a few hundred elements, but might make a difference in sufficient volume.

## Common material properties

There are some properties that are common to both standard materials.

### Swapping shaders

If you would like to take the basic form of one of the standard shaders (i.e. and the data they provide) and write your own shader, you can swap the shader id like this:

```scala mdoc:js
Material.Bitmap(AssetName("funny cat"))
  .withShaderId(ShaderId("my bitmap shader"))
```

Arguably you would usually be better off just writing your own Material + Shader, but this is an option if you find it useful.

### Filling in the space

The standard materials support options for telling Indigo how to fill the space with the material.

```scala mdoc:js
val material = Material.Bitmap(AssetName("funny cat"))

material.normal
material.stretch
material.tile

// The above methods are aliases for:
material.withFillType(FillType.Tile)
```

When you describe a primitive like a `Graphic`, you have to give it a size. However, that size does not have any direct relationship with the dimensions of the texture being used in the material. For example, you could have a graphic with a width and height of 100 x 100, and add a material with a texture that is only 50 x 50. What happens to all the extra space?

- In "normal" mode, the space is left empty.
- In "stretch" mode, the texture is stretched (deformed) to fill the available space.
- In "tile" mode, the texture is repeated to fill the space available.

### Lighting Model

Indigo might be a pixel art engine, but modern pixel art sometimes takes advantage of the underlying 3D hardware to create dynamic lighting effects in order to affect the mood of the game.

The standard materials come with a built in `LightingModel`, which by default are set to `LightingModel.Unlit`, meaning that scene lights will be ignored.

If you want to use lighting in your scene, then you'll need to enable lighting on your materials using `LightingModel.Lit`.

## Making textures for lighting

What we want to do, is take a completely flat texture and make it look as if it is in relief (has depth), with corners and curves and angles that catch the light.

To do that we need to give some details to the lighting model, each parameter represents a different aspect of how the texture is lit.

Briefly they are:

1. Albedo - the flat color of a texture, typically with all baked in shadows and highlights removed. (Full color - taken from the main material)
2. Emissive - the parts of a texture that glows in the dark. (Full color)
3. Normal - describes the bumpiness of a texture (see below)
4. Specular - describes how shiny different parts of the texture are. Polished metal vs. cloth. (Gray scale)

Emissive textures are ordinary RGB images that are used to describe areas that are self-illuminating / glow in the dark and are unaffected by lighting.

Similarly the specular is nothing more than a gray scale image that describes the roughness of the various parts of a texture.

So far so good, but normal maps are more complicated and require some explanation...

### Normal mapping

To light a pixel on a surface we need to know it's [normal](https://en.wikipedia.org/wiki/Normal_(geometry)). A normal is just an arrow (vector) that points away from a surface at a tangent to the plane. Imagine a table with an arrow pointing at the ceiling.

The normal is used to work out how much light from a light source makes it to the camera / eye and therefore allows us (as people) to interpret what angle the surface was at.

Consider a sphere - which is a single surface. The angle of the normal of a sphere's surface rotates around depending on which point of the sphere you're looking at. This change in normal is what gives the appearance of a smooth spherical surface, as opposed to a flat circle.

Knowing that, we can take a completely flat image and _pretend_ it's bumpy or textured by _bending_ the real normal to a new angle at different co-ordinates on the texture.

### Height / Bump maps

If you want to describe bumpiness (i.e. the height) of a mountain range, on a 2D top down image, then a good way to do that is the simply draw a gray scale image where black is sea level and white is the maximum height of the mountains.

This is called a [height or bump map](https://en.wikipedia.org/wiki/Heightmap).

The great thing about height / bump maps is that they're really easy for humans to understand, and even to have some intuition about how to draw by hand. But they are really inefficient in two very specific ways:

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

There are lots of tools available for converting height maps to normal maps, a number of them are free to use websites.
