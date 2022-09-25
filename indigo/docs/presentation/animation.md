---
id: animation
title: Animation
---

There are two types of animations found in Indigo.

1. Keyframe based animations.
2. Procedural / programmed animations.

## Keyframe Animations

Indigo is a "code-only" game engine, there is no GUI or asset creation pipeline. As such, the expectation is that you'll use another tool in order to manufacture animation sprite sheets, and import them into Indigo.

An example of this is that Indigo has support for [Aseprite](https://www.aseprite.org/) (an excellent pixel art editor and animation tool), and the process is:

1. Create your animation in Aseprite.
1. Export the animations and frames as a sprite sheet and a JSON description.
1. Add the sprite sheet and JSON description as a static asset to Indigo.
1. During startup, use the `AsepriteConverter` and indigo's (confusingly named) `JSON` object to parse the JSON into an `Animation` instance, and add it to the `Startup` result type, thereby making it available to your game.

You can then either turn that into a `Sprite` or a series of `Clip`s.

For `Sprite`s:

1. You can create a `Sprite` from the loaded `Aseprite` and associate it to the Animation using an `AnimationKey`.
1. In your game view logic, you can then control the animation by calling the animation methods on the sprite: `play`, `changeCycle`, `jumpToFirstFrame`, `jumpToLastFrame`, and `jumpToFrame`.

Or, for `Clip`s, you can create a `Map` of `CycleLabel -> Clip` from the loaded `Aseprite` and insert them into your game scene.

See the [Sprite example](https://github.com/PurpleKingdomGames/indigo/blob/master/examples/sprite/src/main/scala/indigoexamples/SpriteExample.scala) for a very basic, hand rolled animation example.

>Caution: If two sprites have the same animation key they will play the same animation in the same place using the same cycle - or more correctly - whatever the latter sprite instructed the animation to do. This is very useful! ...but can also cause confusion.

### Registering animations (for `Sprite`s)

Animations are registered in two ways:

1. During boot up, which happens exactly once.
2. As part of the set up process on the resulting `Startup` data type, which is called whenever new assets are loaded.

You can think of boot and setup as "nothing happens until I've finished" and "the game could be running by now" respectively. The idea of allowing you to load assets and add animations during boot is that you may need a minimal set of data to show such things as loading animations (AKA preloaders).

During boot, how you add your animations depends on the entry point you are using.

With `IndigoSandbox` you will add your animations to:

```scala mdoc:js:shared
import indigo.*

val myAnimation: Animation = Animation(AnimationKey("my anim"), Frame(Rectangle(0, 0, 32, 32), Millis(100)))

val animations: Set[Animation] = Set(myAnimation)
```

However, to add an animation to the boot sequence of `IndigoDemo` or `IndigoGame`, you will need to add them to the `BootResult`:

```scala mdoc:js
BootResult.noData(GameConfig.default).addAnimations(myAnimation)
```

During setup, you can add an animation like this:

```scala mdoc:js
Startup.Success(()).addAnimations(myAnimation)
```

The advantage of adding animations during the set up stage is that they can be based on loaded data, for example an imported Aseprite animation.

### Structure

An instance of an animation actually contains at least one sub-animation, called a `Cycle`. `Cycle`s are animations of the same subject matter doing different things. For example: If you export a sprite sheet for you character, your sheet will contain several animations cycles such as an idle cycle, a walk cycle, a jump cycle etc.

## Procedural Animations

Procedural animations are any animations and movements produced as a result of code execution, and can be seen in a few forms, notably:

- Timeline animations
- Signals & Signal Functions
- Hand coded animations

### Timeline Animations

> The `Timeline` type is new as of Indigo 0.14.0.

Timelines allow you to describe animations over time using a combination of `SignalFunction`s and a nice DSL.

To make use of Timelines, add the following imports:

```scala
import indigo.*
import indigo.syntax.*
import indigo.syntax.animations.*
```
This allows you to write timelines that look like this:

```scala
val tl: Timeline[Graphic[Material.Bitmap]] =
  timeline(
    layer(
      startAfter(2.seconds),
      animate(5.seconds) { graphic =>
        easeInOut >>>
          lerp(Point(0), Point(100)) >>>
          SignalFunction(pt => graphic.moveTo(pt))
      }
    )
  )
```

Each timeline can animate one type of thing, but they and their sub components are reusable and composable. The one above is animating a `Graphic`.

Timelines are build up of 'layers' which are each their own sequence of 'time slots', such as the ones you can see above (i.e. `startAfter` and `animate`). 

Time slots form a back-to-back chain of things to do. There are two here but you can have as many as you like. If you wanted to add another animated value - say you wanted to fade the graphic in by altering its material's alpha property - then you would add another layer. The two layers would then be squashed together automatically to produce the end result.

In this animation, we have one layer that initially waits 2 seconds. Then over the next 5 seconds, it calculates a points position diagonally (lerp means linear interpolation) from (0, 0) to (100, 100), and finally moves a graphic to that position. All of this is performed using an 'ease-in-out' function that accelerates the movement up initially and slows it down towards the end.

The function inside the `animate` block is built up using `SignalFunction`s (see below) to describe the value transformation that results in the animated movement. There are lots of helpful signal functions available on the `SignalFunction` companion object for you to make use of.

> Timelines do now know that they are for animation, and can 'animate' anything at all. We just need to change our idea of animation from something like 'moving a picture' to 'producing a value over time following a series of transformations'.

To render our timeline in our scene, we can ask it to produce it's result like this:

```scala
tl.at(context.running)(myGraphic).toBatch
```

This supplies the timeline with the running time and the graphic to animate, and then it converts that into a `Batch` ready to be inserted into the scene somewhere. Without the conversion to a batch, the timeline would produce an optional value.

### Signals & Signal Functions

Signals in Indigo are pretty simplistic as Signal implementations go, and yet are extremely useful.

As a brief introduction to `Signal`s, a signal is a value of type: `t: Seconds -> A` where `t` is the current time and `A` is _some value of `A` to produce based on a time `t`_.

For example:

```scala mdoc:js:shared
import indigo.*

// a signal that outputs 10 'units' per second
val signal: Signal[Double] = Signal(t => t.toDouble * 10)

signal.at(Seconds(0.0)) // 0
signal.at(Seconds(1.0)) // 10
signal.at(Seconds(1.5)) // 15
signal.at(Seconds(2.0)) // 20
```

You can also use them to bend time:

```scala mdoc:js
signal.affectTime(0.5).at(Seconds(2.0)) // 10
signal.affectTime(1.0).at(Seconds(2.0)) // 20
signal.affectTime(1.5).at(Seconds(2.0)) // 30
```

There are a range of pre-made Signal types you can play with, such as `Lerp` and `Orbit`, but the power of them is that you can combine and transform them.

Signals themselves are Functors up to Monad and compose in all the usual ways.

`SignalFunction`s are Signal combinators. Combinators are functions that take a function and return a function, in this case: `Signal[A] => Signal[B]` which is really:

`(t: Seconds -> A) -> (t: Seconds -> B)`

That's getting complicated but luckily Signal functions, being functors, can be created from any function `A => B`, which is much easier to think about.

Signal functions in indigo have only two operations:

```scala
// Operation 1: "and then" function composition. `andThen` and it's alias `>>>`
def >>>[C](other: SignalFunction[B, C]): SignalFunction[A, C] = ???
def andThen[C](other: SignalFunction[B, C]): SignalFunction[A, C] = ???

//Operation 2: Parallel input. Run A => B & A => C and return (B, C)
def &&&[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] = ???
def and[C](other: SignalFunction[A, C]): SignalFunction[A, (B, C)] = ???
```

Example, one could calculate an orbit like this:

```scala mdoc:js
val xPos: SignalFunction[Radians, Double] =
  SignalFunction(r => Math.sin(r.toDouble))

val yPos: SignalFunction[Radians, Double] =
  SignalFunction(r => Math.cos(r.toDouble))

def distance(d: Double): SignalFunction[(Double, Double), (Int, Int)] =
  SignalFunction {
    case (x, y) =>
      ((x * d).toInt, (y * d).toInt)
  }

def giveCords(range: Int): Signal[(Int, Int)] =
  Signal(t => Radians.fromSeconds(t)) |> (xPos &&& yPos) >>> distance(range)
```

Then calling `giveCords(100).at(t)` with different values of `t` would give you various positions on an orbit with a distance of 100 around a world 0,0 coordinate where 1 full orbit takes 1 second.

It works by:

1. Making a `Signal` representing the angle in radians based on the time.
2. Parallel running the angle through the `xPos` and `yPos` `SignalFunction`s to get the `x` and `y` in a range of -1 to +1.
3. Piping the xy through the `distance` `SignalFuction` to shift the coordinates out the desired range.

### Hand crafted animation code

It is entirely reasonable to just do the animation yourself - you're a capable programmer after all! How hard can it be to make something move across the screen?

The main gotcha to be aware of with this kind of programming, is that the amount of time that passes between frames is not consistent. In other words, you can't add `1` to a characters `x` position and expect it to move smoothly across the screen.

All movement must therefore be described in terms of the amount of time that has passed, and there are a few helpful functions on the `GameTime` instance you are supplied with to help you do that. The typical approach is to work out how many 'units per second' your animated thing should run at, and multiply that by the time delta provided in the `FrameContext`.

Sprite and Clip based animations and Signals already have time either taken care of or factored into the equation for you in some way or other.
