---
id: boundaries
title: Boundaries
---

A curious quirk of the way Indigo works is that it seems unusual to need to find the boundary / bounding box of a rendered element. Usually you are telling something how big it needs to be rather than asking it how big or small it actually is.

Nonetheless, Indigo does have a facility for discovering the dimensions of a scene node called the 'Boundary Locator' which can be found on the `FrameContext[_]` instance.

```scala mdoc:invisible
import indigo._
import indigo.platform.assets.DynamicText
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
val boundaryLocator = new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)
val context = new FrameContext(GameTime.zero, Dice.fromSeed(1l), InputState.default, boundaryLocator, ())
```

The boundary locator has a few methods on it, but the two main ones are `findBounds` and `measureText`.

`findBounds` finds the bounds of any scene node you give it.

```scala mdoc:silent
val graphic = Graphic(32, 32, Material.Bitmap(AssetName("my graphic")))
context.boundaryLocator.findBounds(graphic)
```

`measureText` is used specifically to find the real size of the text in a `TextBox`. This is costly as it requires rendering the text with all it's font properties in the background.

```scala
val tb = TextBox("hello, world!")
context.boundaryLocator.measureText(tb)
```

## The fast, the slow, and the unavailable

Measuring the bounds of something is assumed to mean more than just reading it's size property, and takes into account scaling and rotation. This means that measuring the real size of scene entities ranges from being fairly easy to we-won't-even-try hard.

Below is a guide. 'Unavailable' means we cannot sensible work this out for you:

Scene node|Cheap|Expensive|Unavailable|Comments
---|---|---|---|---
`CloneBatch`|-|-|X|
`CloneTiles`|-|-|X|
`EntityNode`|X|-|-|
`Graphic`|X|-|-|
`Group`|-|X|-|Calculated from the calculated bounds of all child nodes. Can be very expensive!
`Mutants`|-|-|X|
`Shape`|X|-|-|
`Sprite`|X|-|-|
`Text`|-|X|-|
`TextBox`|X|-|-|Note that this is the given bounds. Use `measureText` to find the real bounds.
