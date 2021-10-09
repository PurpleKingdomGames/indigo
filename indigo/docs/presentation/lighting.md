---
id: lighting
title: Lighting
---

> THIS PAGE IS HOPELESSLY OUT OF DATE! Sincerest apologies, please see the lighting examples for reference while these docs are corrected...

Lighting in Indigo is in every sense of the word, fake. It was not designed to try and mimic real light, but to be useful and understandable to 2D game builders who were trying to add some interest to their games through lighting.

There are a few ways to add lighting effects to your game in order to create a mood, and they can all be used in combination with each other to achieve the look you're after.

## Ambient light & Scene Layer Effects

> Intuition: The time of day, or a sunny day turning gray.

One way is to use some combination of effects, such as the ones below:

```scala mdoc
import indigo._

// Apologies, this is entirely out of date! Will revise soon.
// Ambient lights are now actual lights, and layer effects are
// achieved through blend shaders.

SceneUpdateFragment.empty
  //.withAmbientLight(RGBA.Red)
  //.withColorOverlay(RGBA.Green)
  //.withTint(RGBA.Blue)
  //.withSaturationLevel(0.5)
```

Color overlay, tint, and saturation can be applied to all layers as shown above, or to individual layers by using methods like `.withGameLayerTint` instead of `.withTint`. The settings above are nonsense, but you could make your game look like it's dark and the moon is out using something like:

```scala
SceneUpdateFragment.empty
  //.withGameLayerTint(RGBA(0.0, 0.2, 0.7, 1.0))
  //.withAmbientLight(RGBA.White.withAmount(0.5))
```

## Lighting layer

> Intuition: Like light shining through a stained glass window onto your game.

This is a sort of "image based lighting" and is touched on in the ["depth and layers" page](depth-and-layers.md).

To use the lighting layer, you add graphics, sprites, and text elements as normal to the scene, but add them to the lighting layer instead of the game or UI layers. Note that you will also need to adjust the ambient light or you won't see any chance.

The lighting layer works differently from the game and UI layers, because instead of combining the elements together as normal, they are multiplied together and then combined with the game layer below.

The color of your lighting layer elements is important. Shades of gray with appear as white light at different levels of intensity. Colored elements have the same effect but in their particular hue. The lighting works like a tint rather than a color overlay, so maximum white light is the normal pixel color, not blown out white.

This sort of effect is very useful for drawing things like the light from a street lamp on a foggy night. The limitation is that while your light will affect anything on the game layer under it, it won't produce any anything other than a flat lighting effect. In other words, no highlights or other edge effects. For that you need to combine your lighting layer effects with dynamic lighting below.

## Emissive Materials

> Intuition: A neon tube sign outside a bar.

One of the types of texture you can apply to you [material](materials.md) is called an "emissive", which means that it "emits" light all by itself. Very handy for representing eyes glowing in a dark cave, perhaps.

Limitation: In a real 3D engine, an emissive material might also affect the surround surface colors or at least glow dynamically. Indigo does not do that, if you want it to glow you'll have to draw the glow into the emissive texture.

## Dynamic lighting

> Intuition: A spinning flashing warning light that picks out different objects and surfaces in the dark as it pulses and rotates.

Dynamic lighting changes as the light moves around the scene. It highlights corners and outlines objects. They can be added to your scene like this:

```scala
SceneUpdateFragment.empty
  .withLights(PointLight.default)
```

> Don't forget to set up your [materials](materials.md). Dynamic lighting only affects materials that can be lit, and the materials affect what happens when the light hits them.

Currently, shadow casting is not supported.

### Types of Light

There are three types of light, and their common attributes are:

```scala
sealed trait Light {
  val height: Int   // Lower altitude lights create harder edge/rim lighting. Higher up lights (> 0) affect the front face of surfaces more.
  val color: RGBA    // Note: RGB, not RGBA
  val power: Double // How strong the light is
}
```

#### Point lights

Point lights are a point in space that emit light evenly in all directions. Example:

```scala mdoc
PointLight.default
  .moveTo(Point(50, 50))
  //.withAttenuation(50) // How far the light fades out to
  .withColor(RGBA.Magenta)
  //.withHeight(100)
  //.withPower(1.5)
```

#### Spotlights

Spotlights shine a cone of light onto a scene at a given angle. Example:

```scala mdoc
SpotLight.default
  .withColor(RGBA.Green)
  .moveTo(Point(50, 50))
  .rotateBy(Radians.fromDegrees(45))
  //.withHeight(25)
  //.withPower(1.5)
  //.withAttenuation(60) // How far the light fades out to
  //.withNear(10)        // How far from the position the light starts
  //.withFar(60)         // How far it extends
```

#### Direction lights

Direction lights shin light evenly along one angle, as if from very far away. Useful for things like sun and moonlight. Example:

```scala mdoc
DirectionLight.default
  .withColor(RGBA.Blue)
  //.withHeight(100)
  //.withPower(0.8)
```
