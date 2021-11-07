---
id: clones-and-mutants
title: Clones & Mutants
---

> Available in the forthcoming Indigo 0.10.0 only. There were earlier versions of Clones but they were never documented properly and they've all been superseded.

Sometimes you really need to draw a lot of things. Thousands and thousands of things.

If you try and do that with standard primitives you'll be disappointed to find that you quickly hit a ceiling somewhere in the hundreds. To be clear, hundreds of things is probably ample for most 2D games, but sometimes you need more.

The reason you'll cap out at perhaps 400 to 600 primitives is that each one carries an overhead through the rendering pipeline. Each one has to be processed, events collected, actions performed, animations updated, and finally rendered. This pipeline allows each element to be unique and easy to use.

If you want to go faster, what you need to do is trade off uniqueness and convenience for sheer quantity, and you do that by skipping some part of the rendering pipeline.

This is what clones and mutants do, the take an archetypal primitive, process it once, and then skip huge sections of the rendering pipeline for all the copies of that thing.

You lose some aspect of control, but you gain volume.

## Clones vs Mutants

While clones are mutants are based on the same basic idea, they work very differently. Both allow you to increase the number of things you can render, but they make different trade offs.

- **Clones** make many copies of the original utilising hardware instancing, but only allow you to modify a few basic pieces of transformation information per instance.*

- **Mutants** allow you to do whatever you want, but the catch is that all changes - even basic transformations like position - must be done within the shader.

Clones are designed to reduce graphics card context switches to practically nothing. Mutants still need to set shader data per object and so are significantly faster than normal primitives, but can't get close to the volume of rendered items that clones can.

For sheer volume, you want clones. For maximum visual uniqueness, it's mutants.

(* Strictly speaking, you can make each instance pretty unique with custom entities and clever shaders. A common technique is to make use of the `INSTANCE_ID` to provide uniqueness, for example by modulo'ing it to get a fixed number range and then using that to set, say, transparency.)

## Trading off convenience for speed

Most of Indigo's APIs and primitives are written to be ergonomic, but this is not true of Clones and Mutants, which have far fewer convenience methods.

Why should that be the case?

Indigo's primitives and APIs are designed to be friendly and aren't too concerned about the performance implications of that choice because in general, it's fast enough.

The point of clones and mutants is speed and volume. The easiest way to lose speed in Scala.js/JavaScript is to do many object/memory allocations or use poor data structures for large volumes of entities.

If you allocate a lot, the result is garbage collection pressure, which means GC pauses and poor frame rates.

If you use a `List` rather than an `Array`, you end up with [linear rather than constant speed](https://docs.scala-lang.org/overviews/collections-2.13/performance-characteristics.html) for the kind of operations we care about.

## Using Clones and Mutants

### Clone blanks

Whether you're using clones or mutants, you will need an archetypal entity that your clone/mutant is going to be based on. These are called "clone blanks".

A clone blank can be any primitive that extends the `Cloneable` trait, which includes custom entity nodes. Groups and other compound entities cannot be used as clone blanks.

Clone blanks are processed once per frame, and you can add them to your scene as follows:

```scala mdoc:silent
import indigo.*

val graphic = Graphic(32, 32, Material.Bitmap(AssetName("bob texture")))
val cloneId = CloneId("bob")
val cloneBlank = CloneBlank(CloneId("bob"), graphic)

SceneUpdateFragment.empty.addCloneBlanks(cloneBlank)
```

The clone ID is used to reference the blank at render time. If the ID isn't found, the clone or mutant instances are skipped.

### Clone batch

The simplest and most high volume type of clone is the `CloneBatch`.

`CloneBatch` entities allow you to change only basic transform properties of your entity. Your clone blank will be copied once for every instance of the `CloneBatchData` you provide, which sets the x, y, rotation, scale x and scale y properties of your clone blank entity.

This example will render three instances of the clone blank in different positions on the screen.

```scala mdoc:silent
val particles = Array(Point(10), Point(20), Point(30))

CloneBatch(
  cloneId,
  particles.map(pt => CloneBatchData(pt.x, pt.y))
)
```

### Clone tiles

The problem with `CloneBatch` is that the copies are pretty much identical - particularly when using Indigo's standard primitives. Meaning you could render 10,000 red dots, but not 10,000 red, green and blue dots (unless you use a custom entity).

`CloneTiles` are almost identical to `CloneBatch` except that they also allow you to choose a texture crop on a per instance basis. As such they are only really useful with custom entities or `Graphic`s, but they are very useful nonetheless. For example, one might use them to render large tile based level maps.

This example renders our three instances again but this time crops the texture in three different places.

```scala mdoc:silent
val crops = Array(Rectangle(0, 0, 10, 10), Rectangle(10, 0, 10, 10), Rectangle(0, 10, 10, 10))

CloneTiles(
  cloneId,
  particles.zip(crops).map { case (p, c) =>
    CloneTileData(p.x, p.y, Radians.zero, 1, 1, c.x, c.y, c.width, c.height)
  }
)
```

### Mutants

The idea of mutants is to take a clone blank and 'mutate' it per instance by providing different data to it's shader program. In theory this makes mutants extremely powerful, as you can render pretty much anything, but this also makes them quite complicated.

`Mutants` are considered an advanced feature, as they are really only useful with custom entities.

From a usage perspective, `Mutants` are no more difficult to use than `CloneBatch`s or `CloneTiles`, here is a simple example that renders a single copy of a clone blank:

```scala mdoc:silent
Mutants(
  cloneId,
  Array(
    List(
      UniformBlock(
        "MutantData",
        List(
          Uniform("MOVE_TO")  -> vec2(10.0, 10.0),
          Uniform("SCALE_TO") -> vec2(2.0, 2.0),
          Uniform("ALPHA")    -> float(0.75)
        )
      )
    )
  )
)
```

The idea here is that you have [created a custom entity](guides/howto-custom-entity.md) and for each instance, you want to set it's UBO data, which here contains a position, a scale and an alpha value.

In many ways the important part of this example is the alpha. If you only wanted to position the entities you be far better off with one of the clone types. The whole point is that you also plan to programmatically influence the visual rendering of the entity in some way.

You will then need a pair of shaders to go with that data.

**Vertex:**

```glsl
#version 300 es

precision mediump float;

vec2 POSITION;
vec2 SCALE;

//<indigo-vertex>
layout (std140) uniform MutantData {
  vec2 MOVE_TO;
  vec2 SCALE_TO;
  float ALPHA;
};

void vertex(){
  POSITION = MOVE_TO;
  SCALE = SCALE_TO;
}
//</indigo-vertex>
```

This vertex shader takes the position and scale variables and overrides them based on the UBO 'MutantData' provided.

**Fragment:**

```glsl
#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 CHANNEL_0;
vec4 COLOR;

//<indigo-fragment>
layout (std140) uniform MutantData {
  vec2 MOVE_TO;
  vec2 SCALE_TO;
  float ALPHA;
};

void fragment(){
  float a = CHANNEL_0.a * ALPHA;
  COLOR = vec4(CHANNEL_0.rgb * a, a);
}
//</indigo-fragment>
```

The fragment shader takes the default texture channel and applies the supplied alpha to it. Note the use of [premultiplied alpha](shaders/premultiplied-alpha.md).
