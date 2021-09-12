---
id: effects
title: Effects
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Effects are a fairly late addition to Indigo, and are cheap and cheerful both in implementation and usefulness. They are all implemented in a single render pass and cost very little to make use of (in terms of rendering performance).

While we feel confident that people will find creative uses for effects, they are fairly limited - especially the edge effects like borders and glows - and we consider them to be more like prototyping aids. Effects are definitely an area for future improvement.

There are two places that you can apply effects:

1. Scene graph nodes
1. Layers

## Scene graph node effects

### Flip Horizontal / Vertical (WebGL 1 & 2)

Flip draws the node in place but mirrored on either the X or Y axis.

```scala mdoc
graphic.flipHorizontal(true)
graphic.flipVertical(false)
```

### Alpha (WebGL 1 & 2)

Sets the transparency of the node.

```scala mdoc
graphic.withAlpha(0.5)
```

### Tint (WebGL 1 & 2)

Tint essentially sets the saturation level of each color channel, like looking through a piece of transparent colored plastic. Examples:

- `RGBA.White` tint doesn't color the graphic white, it leaves it looking normal
- `RGBA.Black` absorbs all the light and the nodes pixels end up black (alpha is respected).
- `RGBA.Red` which is (r=1.0, g=0.0, b=0.0, a=1.0) sucks the blue and green out of the image (like standing in a chemical photography dark room).

```scala mdoc
graphic.withTint(RGBA.Red)
```

### WebGL 2 only effects

### Color Overlay (WebGL 2)

Where tint removes color, color overlay adds it. So to take the same examples:

- `RGBA.White` Makes the image white (alpha is respected).
- `RGBA.Black` Makes the image black (alpha is respected).
- `RGBA.Red` Makes the image red (alpha is respected).
- `RGBA.Red.withRed(0.5)` which is (r=0.5, g=0.0, b=0.0, a=1.0) adds 50% red to each pixel up to a maximum value of 1.0.

```scala mdoc
graphic.withOverlay(Overlay.Color(RGBA.Red))
```

### Gradient Overlay (WebGL 2)

Has the definition:

```scala mdoc
Overlay.LinearGradiant(fromPoint: Point, fromColor: RGBA, toPoint: Point, toColor: RGBA)
```

Works the same way as color overlay but allows a linear gradient to be applied instead of one solid color. The `Point` positions are relative to the node being drawn and move around with the node.

```scala mdoc
// For a 16x16 graphic where we want to go top left to bottom right
graphic.withOverlay(
  Overlay.LinearGradiant(
    Point.zero,
    RGBA.Magenta,
    Point(16, 16),
    RGBA.Cyan
  )
)
```

### Border (WebGL 2)

Borders are a limited edge effect. You can use inner and / or outer borders and both can be set to `Thin` (1 pixel) or `Thick` (2 pixels).

```scala mdoc
graphic.withBorder(
  Border(
    color = RGBA.Green,
    innerThickness = Thickness.None,
    outerThickness = Thickness.Thick
  )
)
```

### Glow (WebGL 2)

Glow is a limited edge effect. You can use inner and / or outer glows and both can have an amount set.

```scala mdoc
graphic.withGlow(
  Glow(
    color = RGBA.Green,
    innerGlowAmount = 0.0,
    outerGlowAmount = 1.0
  )
)
```

## Layer effects

Layer effects work in exactly the same way as the node level effects, but there are fewer of them. They can all be set during your view construction via methods on your `SceneUpdateFragment` instance.

All layers can have **tint**, and **saturation** levels set. Tint is as described above, saturation is how much color is used i.e. 1.0 is full color, 0.0 is gray scale.

Additionally, the game and ui layers can also have a flat **color overlay** applied to them. For example if you want the whole game layer to turn white in a lighting flash, or to fade everything to black, you could use the layer color overlay effect.
