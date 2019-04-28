# Animation

There are two type of animations found in Indigo.

1. Timeline or Key frame based animations.
1. Procedural or programmed animations.

## Timeline Animations

The idea with Indigo has always been that it's a "code-only" game engine. There are better tools out there for the many and varied other tasks associated with game production, and Indigo is aimed at people who like tools that do one job well.

As such, the expectation is that you'll use a tool in order to manufacture animation sprite sheets, and import them into Indigo.

An example of this is that Indigo already has support for Aseprite (an excellent pixel art editor), and the process is:

1. Create your animation in Aseprite.
1. Export the animations and frames as a sprite sheet and a JSON description.
1. Add the sprite sheet and JSON description as a static asset to Indigo.
1. During startup, use the Aseprite helper class to parse the JSON into an animations object and add it to the `Startup` result type, thereby making it available to your game.
1. You then create a `Sprite` and associate it to the Animation using an `AnimationKey`.
1. In your game view logic, you can then control the animation by calling the animation functions on the sprite: `play`, `changeCycle`, `jumpToFirstFrame`, `jumpToLastFrame`, and `jumpToFrame`.

 See the Sprite example for a very basic, hand rolled animation example.

## Procedural Animations

### Describing movement with code, Time vs Frames

The other kind of animation, is animation described in code, and this comes in essentially three forms.

### DIY / Manually Coded.

It is entirely reasonable to just do the animation yourself - you're a capable programmer after all! How hard can it be to make something move across the screen?

The main gotcha to be aware of with this kind of programming, is that the amount of time that passes between frames is not even remotely consistent. In other words, you can't add `1` to a characters `x` position and expect it to move smoothly across the screen.

All movement must therefore be described in terms of the amount of time that has passed, and there are a few helpful functions on the `GameTime` you are supplied on every frame to help you do that.

Timeline animations, Time Varying Values, and Signals already have time either taken care of or factored into the equation for you in some way or other.

### Time Varying Values

`TimeVaryingValue` are stateful little constructs that you need to call the update function on yourself every frame tick.

However, they're very useful! An easy example:

You have a little lumber jack, he walks over to a tree and is now going to cut to down. Work is effort over time, so you have a `TimeVaryingValue` like this in your model:

```scala
val woodChoppingProcess: TimeVaryingValue =
  TimeVaryingValue(0, gameTime.running) // No progress, starting now.

final case class LumberJack(chopWood: TimeVaryingValue, working: Boolean) {
  def update(gameTime): LumberJack =
    if(working) {
      this.copy(
        chopWood = chopWood.increaseTo(
          100,             // upper limit
          10,              // units per second
          gameTime.running // current time
        )
      )
    } else this
}
val lumberJack = LumberJack(woodChoppingProcess, true)
```

And then when you update the lumberJack during your frame update `lumberJack.update(gameTime)`.

You can then render a progress bar as a percentage in the view by simply asking the `TimeVaryingValue` for its current `value`.

### Signals, Signal Functions & Automata

// TODO
