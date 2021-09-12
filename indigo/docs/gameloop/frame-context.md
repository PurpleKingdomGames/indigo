---
id: frame-context
title: Frame context
---

Updating a frame in Indigo is principally about doing two things:

1. Updating - ...the model and view model based on events and previous state
2. Presenting - ...the frame to the player

For simplicity, we'll only talk about model updates from here on but this applies to model, view model and presentation updates equally.

One of the goals of Indigo is to make frame updates referentially transparent and testable. Superficially that's easy, here's a simple model class:

```scala mdoc
import indigo.Point

final case class Bob(position: Point):
  def update: Bob =
    this.copy(position = position + Point(1, 0))
```

So when we update Bob he's going to proceed across the screen. The trouble is that his movement is pretty limited because the only things we can base his next move on is Bob's own state.

What if we wanted to make Bob's movement totally erratic?

```scala mdoc:reset
import indigo.Point
import scala.util.Random

final case class Bob(position: Point):
  def update: Bob =
    this.copy(position = position + Point(Random.nextInt(2), Random.nextInt(2)))
```

Remembering that in `Random.nextInt(2)`, 2 is an exclusive upper limit - Bob is now going to jitter around by adding random coordinates made of 1's and 0's to his current position.

The problem is that `Random` goes against our referential transparency principle. What if you needed to write a test for Bob's update function, how would you know what value was going to come next? You could write a test to say that his next position would be at most 1 pixel away from his current position, but that's the best you could do.

> Side note: Testing this kind of behavior with property based testing techniques works very well and often neatly side steps the issue of unknown values.

Similar problems occur if you attempt to implement [frame independence](/docs/information/glossary#frame-independence) based on the elapsed time. You could add a last updated field to Bob's model and use `System.currentTimeMillis` to get the delta, but again, how would you write a test for that.

The problem is that `Random` and `System.currentTimeMillis` are side effecting and that makes them hard to test.

## `FrameContext[_]`

To make these things testable, a deterministic frame context is provided to give you a predictable environment to work in.

For some given starting definitions:

```scala mdoc
final case class StartUpData()
final case class Model()
```

Here is the definition of the model update function from `IndigoDemo`:

```scala mdoc
import indigo._

def updateModel(context: FrameContext[StartUpData], model: Model): GlobalEvent => Outcome[Model] = ???
```

This function is supposed to update the model given some events, but it does so in the presence of `context: FrameContext[StartUpData]`.

The frame context instance provides:

- `gameTime` - A sampled instance of time that you should use everywhere that you need a time value.
- `dice` - A pseudo-random number generator, made predictable / reproducible by being seeded on the current running time.
- `inputState` - A snapshot of the state of the various input methods, also allows input mapping of combinations of inputs.
- `boundaryLocator` - A service that can be interrogated for the calculated dimensions of composite screen elements.
- `startUpData` - A read only reference to any and all data created during start up / set up.
