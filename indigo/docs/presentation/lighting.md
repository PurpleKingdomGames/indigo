---
id: lighting
title: Lighting
---

Lighting in Indigo is in every sense of the word, fake. It was not designed to try and mimic real light, but to be useful and understandable to 2D game builders who were trying to add some interest to their games through lighting.

There are a two ways to add lighting effects to your game in order to create a mood, and they can be used in combination with each other to achieve the look you're after.

1. Image based lighting - 'hand drawn' lighting effects.
2. Dynamic lighting - Adding lights to your scene

Neither system currently supports shadows out-of-the-box.

> You can always roll your own lighting system, either with game code, totally custom shaders, or by writing 'lighting model' shaders.

## Image based lighting

Imagine taking your scene, and then overlaying it with a black and white image. Black is full shadow, white is fully lit (which here means, full normal colour, no over exposure), and gray tones are everything in between.

Then imagine that you can add color to the black and white image, and where you do, it's like looking through colored plastic or stained glass: You can see the image underneath, but colors are absorbed, e.g. placing a red filter over a rainbow absorbs all the blue and green.

Finally, imagine you can build up that black/white/colour filter by adding entities to a normal layer (which means you can move and animate them), and they are specially blended so that overlaying red on green on blue produces a white filter, i.e. full color.

That's image based lighting.

This sort of effect is very useful for drawing things like the light from a street lamp on a foggy night. The limitation is that while your light will affect anything on the game layer under it, it won't produce any anything other than a flat lighting effect. In other words, no highlights or other edge effects. For that you need to combine your lighting layer effects with dynamic lighting or some other trick.

### Using Indigo's lighting shaders

Indigo comes bundled with shaders you can use to produce image based lighting. These shaders do not make use of any special machinery, and so you can always write your own versions if you like!

Imaged based lighting in Indigo is all about the [blending](shaders/blending.md).

There are two blending jobs to do:

1. When we add an entity, we need to blend it such that the colors are multiplied together rather than the usual "drawn on top".
2. When we blend the layer onto the scene below it, that will also need to be multiplied correctly.

Below is an example lighting layer that sets a low (25% white) ambient light so the scene is never absolutely black.

```scala mdoc:silent
import indigo._

Layer(Graphic(50, 50, Material.ImageEffects(AssetName("tourch light"))))
  .withBlending(Blending.Lighting(RGBA.White.withAlpha(0.25)))
```

## Dynamic lighting

Dynamic lighting changes the scene's lighting as the light moves around. It highlights corners and outlines objects. They can be added to your scene or layer like this:

```scala
SceneUpdateFragment(
  Layer("my layer").withLights(PointLight(Point(100, 100), RGBA.Green))
).withLights(AmbientLight(RGBA.Blue.withAlpha(0.2)))
```

### Dynamic lighting limitations

Indigo uses what is known as a forward renderer to draw it's scenes. Forward rendering is nice and simple and good for 2D engines, but has its limitations. One such limit is how many lights you can effectively have in a scene.

**Indigo's maximum lights per layer is set to 8.**

Do decide which lights to use, Indigo effectively does this:

`(scene.lights ++ layer.lights).take(8)`

Currently, shadow casting of any sort is not supported out of the box.

### Types of Light

Most of a lights have a number of properties you can experiment with that are easy to discover, but here are some basic examples to get you started.

#### Point lights

Point lights are a point in space that emit light evenly in all directions. Example:

```scala mdoc:silent
PointLight(Point.zero, RGBA.White)
  .withFalloff(Falloff.SmoothQuadratic(0, 100))
```

#### Spotlights

Spotlights shine a cone of light onto a scene at a given angle. Example:

```scala mdoc:silent
SpotLight(Point.zero, RGBA.White)
  .withAngle(Radians.fromDegrees(45))
  .lookAt(Point(100, 100))
```

#### Direction lights

Direction lights shin light evenly along one angle, as if from very far away. Useful for things like sun and moonlight. Example:

```scala mdoc:silent
DirectionLight(Radians.fromDegrees(45), RGBA.Blue)
```

#### Ambient lights

Ambient lights illuminate the whole scene evenly with some amount of 'background' light. Example:

```scala mdoc:silent
AmbientLight(RGBA.White.withAlpha(0.2))
```

### Materials for Dynamic Lighting

Dynamic lighting only affects [materials](presentation/materials.md) that can be lit, i.e. have a lighting model, and the material properties affect what happens when the light hits them.

Lighting models are either: `Unlit` or `Lit`, and if they are either textured or 'flat'. For example a `Shape` can be lit, but is always flat since there are no textures.

If you want a textured / bumpy looking entity then your lighting material can be made up of up to four textures:

1. Albedo - the color texture, albedo has a special meaning compared to diffuse, in that there will be _no_ shadows or highlights drawn into this texture.
2. Emissive - parts of your material that glow in the dark
3. Normal - Describes the bumps on your surface
4. Roughness/Specular - alters how shiny different parts of your texture are.

A note on emissive materials:

"Emissive" means that it "emits" light all by itself. Very handy for representing eyes glowing in a dark cave, perhaps. Emissive materials have a couple of limitations worth calling out:

1. In a real 3D engine, an emissive material might also affect the colors of surrounding surfaces. Indigo does not do that.
2. Emissive materials are ignored by image based lighting.

More information on the main [materials](presentation/materials.md) page.
