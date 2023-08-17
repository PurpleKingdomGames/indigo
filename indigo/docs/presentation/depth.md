# Depth

One thing you will need to consider when building your game, is the order that visual elements are drawn in.

Depth is quite straight forward. All visual elements can be given a `Depth`, which is just a number.

Zero is right in front of the camera, and bigger numbers are further away. It makes sense if you think in terms of "I am the camera and it's further away from me, so it's a bigger number".

> There is an open [issue](https://github.com/PurpleKingdomGames/indigo/issues/223) suggesting this behavior should be reversed.

If several things have the same depth, the elements are draw in the order given to the `SceneUpdateFragment` e.g.:

```scala mdoc:js
import indigo.*

Batch(
  Graphic(10, 10, Material.Bitmap(AssetName("texture 1"))), // drawn first (on the bottom)
  Graphic(10, 10, Material.Bitmap(AssetName("texture 2"))), // drawn second (on top of graphic1)
  Graphic(10, 10, Material.Bitmap(AssetName("texture 3")))  // drawn third (drawn last on top of graphic2)
)
```

`Group`s are a special case. `Group`s have child nodes, which can also be other groups. When a group is encountered, the depth of it's children are all relative to the group's depth.

### Layer depth

This is mentioned in the [layers](layers.md) documentation, but it's worth calling out here too:

**Depth on layers currently works in the opposite order to scene entities.**

- With scene entities, ***0*** means ***as close to you as possible*** and bigger numbers are further away.
- With layers, ***0*** mean ***bottom of the stack***, like the bottom layer of a cake, and bigger numbers are placed above it.
