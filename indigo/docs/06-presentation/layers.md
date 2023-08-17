# Layers

Layers are used to help "layer up" and group the visual elements of your scene.

Indigo's renderer draws layers one at a time from the bottom up, so regardless of the set depths of the elements within your layers, the contents of two layers will never intermingle.

Additionally, certain aspects of your game's visual appearance and layout of elements can be set on a per layer basis.

## Getting started with layers

A layers primary function is to hold scene nodes to be rendered.

All indigo projects require you to describe your games visuals, here is a simple example:

```scala mdoc:js:shared
import indigo.*

val graphic =
  Graphic(50, 50, Material.Bitmap(AssetName("my texture")))

SceneUpdateFragment(
  graphic.moveTo(10, 10),
  graphic.moveTo(10, 20),
  graphic.moveTo(10, 30)
)
```

This is a nice easy way to get started, but when you create a scene fragment like that, what you've actually done is this:

```scala mdoc:js
SceneUpdateFragment(
  Layer(
    graphic.moveTo(10, 10),
    graphic.moveTo(10, 20),
    graphic.moveTo(10, 30)
  )
)
```

You can always add layers to scene fragments:

```scala mdoc:js
SceneUpdateFragment.empty.addLayers(Layer(graphic), Layer(graphic))
```

## Merging scenes and layers

Combining scene fragments works as you might expect:

```scala mdoc:js:shared
val graphicA = Graphic(50, 50, Material.Bitmap(AssetName("a")))
val graphicB = Graphic(50, 50, Material.Bitmap(AssetName("b")))

val a = SceneUpdateFragment(graphicA, graphicA, graphicA)
val b = SceneUpdateFragment(graphicB, graphicB, graphicB)

a |+| b
```

Results in:

```scala mdoc:js
SceneUpdateFragment(
  Layer(graphicA, graphicA, graphicA),
  Layer(graphicB, graphicB, graphicB)
)
```

However, you may want to merge scenes and have all the elements end up on the same layer, in which case you need to name the layers:

```scala mdoc:js
val c = SceneUpdateFragment(
  Layer(BindingKey("my layer"), graphicA, graphicA, graphicA)
)
val d = SceneUpdateFragment(
  Layer(BindingKey("my layer"), graphicB, graphicB, graphicB)
)

c |+| d
```

Results in:

```scala mdoc:js
SceneUpdateFragment(
  Layer(
    BindingKey("my layer"),
    Batch(graphicA, graphicA, graphicA, graphicB, graphicB, graphicB)
  )
)
```

Layers can be merged to!

```scala mdoc:js
Layer(graphicA) |+| Layer(graphicB)
```

## Other layer properties

### Depth

Layers are stored in a `Batch` in the `SceneUpdateFragment`, and are in general rendered in order from first added to last. Generally speaking between ordering and the use of `BindingKey`s, that's often enough to ensure things appear as expected.

However you can also set depths for layers to ensure they end up in the expected place.

**_Important!_ Depth on layers currently works in the opposite order to scene entities.**

- With scene entities, ***0*** means ***as close to you as possible*** and bigger numbers are further away.
- With layers, ***0*** mean ***bottom of the stack***, like the bottom layer of a cake, and bigger numbers are placed above it.

```scala mdoc:js
Layer(graphicA).withDepth(Depth(100))
```

### Visibility

Layers can be made invisible, which may be handy for certain effects or even just debugging. Invisible layers incur no rendering overhead.

### Layers as overrides

There are some properties that layers have that can be used to override game or scene level defaults. Those include:

1. Magnification - have a crisp HD UI layer over a big pixel game!
2. Lights - Scene lights are _always_ take first, and layer lights are added up to the maximum of 8.
3. Camera - Each layer can have a separate camera, for example you can always keep your UI on screen while your game layer camera follows a hero character.

## Blending

If you've ever used a photo editing software you're probably aware that layers can be used to alter the appearance of the image, say by sucking the colour out and turning it black and white. Indigo layers can do the same sorts of thing, however there are only a few built in Blending options currently, and for anything more advanced you'll need to roll up your sleeves and write your own blending functions!

You can read more about this subject in the [blending section of the docs](/07-shaders/blending.md), but in a nutshell a `Blending` instance is comprised of two things:

1. `BlendMode` - this is the hardware blend function
2. `BlendMaterial` - which is a material backed by a special shader specifically for blending.

Blending occurs, in different ways, for each entity as it is rendered onto layer, for each layer rendered on the layers before it, and for the scene as it's finally rendered onto the canvas.

There are three built in `BlendMaterials`: `Normal`, `Lighting` and `BlendEffects`.
