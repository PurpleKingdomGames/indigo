---
id: shapes
title: Shapes
---

Shapes allow you to draw simple graphics without needing images.

There are four `Shape` types that come shipped with Indigo at the time of writing, although we may add more in the future:

1. `Shape.Box` - Any rectangle
1. `Shape.Circle` - A circle centered around a point
1. `Shape.Line` - A straight line made of two points
1. `Shape.Polygon` - An arbitrary shape with up to 16 vertices.

They all share similar properties:

- They can all (aside from Line) have a `Fill` that is solid color or a linear or radial gradient.
- They can all have a `Stroke` applied to them with color and thickness.
- They can all be transformed in the usual ways
- They can all receive simple lighting effects (no normal mapping)

One limitation that shape strokes have, is that they are all solid and all use rounded corners. This is because of the way they are calculated. If there is interest we may look at adding more stroke effects like dashed lines or alternate capping types.

Below are examples of each to help get you started.

## Box

```scala mdoc
import indigo._

Shape.Box(
  Rectangle(Point(100, 100), Size(50, 50)),
  Fill.Color(RGBA.White),
  Stroke(4, RGBA.Blue)
)
```

## Circle

```scala mdoc
Shape.Circle(
  center = Point(30, 30),
  radius = 20,
  fill = Fill.RadialGradient(
    Point(20),
    RGBA.Magenta,
    Point.zero,
    RGBA.Cyan
  ),
  stroke = Stroke(3, RGBA.White)
)
```

## Line

```scala mdoc
Shape.Line(Point(30, 80), Point(100, 20), Stroke(6, RGBA.Cyan))
```

## Polygon

```scala mdoc
Shape.Polygon(
  Fill.LinearGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan),
  Stroke(4, RGBA.Black)
)(
  Point(10, 10),
  Point(20, 70),
  Point(90, 90),
  Point(70, 20)
)
```
