---
id: input-handling
title: User Input Handling
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

At the time of writing, Indigo understands three input device types: Mouse, Keyboard, and Gamepad.

> A note on gamepad support: So far only PS4 gamepads have been tested. Sony made the excellent decision to make them plug and play! XBox and other controllers may work but no time was invested in testing them once it became apparent then they didn't work out of the box on non-windows machines. Pull requests, issue reporting, and funding are all welcome!

Input handling comes in two forms:

1. Discrete events
2. Continuous signals (sampled)

## `InputEvent`s (Discrete events)

A discrete event is something that happens at a point in time, and you are notified about it.

There are input actions that are best represented as events, a mouse click for example, which happens quickly as a result of a mouse button passing quickly through the Up, Down, and Up states. The browser noticing the transition and letting you know that it's happened is very useful.

Input events only (currently) cover Mouse and Keyboard events, _not gamepads_, and can be detected during the model or view model update functions as you would any other event.

For more information, please see the [events](gameloop/events.md) page.

## InputState (Continuous signals)

There are other types of input that don't make as much sense as events.

It is possible in Indigo to listen to every distinct mouse move event for example, but that is almost certainly not what you actually want to do. Most likely, what you really want is the cumulative position at the point that this frame is being processed.

Indigo does this by sampling the input states of the mouse, keyboard and gamepad and allowing you to access it through the [frame context](gameloop/frame-context.md).

> Note that this is how gamepads are usually modeled and at the moment Indigo is no exception. Adding discrete events for specific button presses to emulate mouse-like behavior is under consideration.

To continue our example from above: If you wanted the current mouse position, rather than listening to all of the `Move` events and redundantly reprocessing your model many times per frame, you can simply access the position directly with:

```scala
context.mouse.position // returns a Point(x, y)
```

### Input Mapping

`InputState`s ability to tell you about continuous input signals from mice, keyboards and gamepads is great, but what if you need to capture combinations of events? It's surprisingly hard work!

For this we have created the input mapper, which can be found inside the input state.

Here is an example of a "dot" that can be moved up, down, left, right, _and_ diagonally using combinations of the W, A, S, and D keys.

```scala mdoc
import indigo._

final case class Dot(center: Point) {
  def update(timeDelta: Seconds, inputState: InputState): Outcome[Dot] = {
    val inputForce = inputState.mapInputs(Dot.inputMapping, Vector2(0, 0))
    val nextCenter = Point(
      (center.x + math.round(inputForce.x * timeDelta.toDouble).toInt),
      (center.y + math.round(inputForce.y * timeDelta.toDouble).toInt)
    )
    Outcome(this.copy(center = nextCenter))
  }
}
object Dot {
  def initial(center: Point): Dot =
    Dot(center)

  val inputMapping: InputMapping[Vector2] = {
    val xSpeed = 30.0d
    val ySpeed = -30.0d
    InputMapping(
      // WASD keymap
      Combo.withKeyInputs(Key.KEY_W, Key.KEY_A) -> Vector2(-xSpeed, ySpeed),
      Combo.withKeyInputs(Key.KEY_W, Key.KEY_S) -> Vector2(0.0d, 0.0d),
      Combo.withKeyInputs(Key.KEY_W, Key.KEY_D) -> Vector2(xSpeed, ySpeed),
      Combo.withKeyInputs(Key.KEY_A, Key.KEY_S) -> Vector2(-xSpeed, -ySpeed),
      Combo.withKeyInputs(Key.KEY_A, Key.KEY_D) -> Vector2(0.0d, 0.0d),
      Combo.withKeyInputs(Key.KEY_S, Key.KEY_D) -> Vector2(xSpeed, -ySpeed),
      Combo.withKeyInputs(Key.KEY_W)            -> Vector2(0.0d, ySpeed),
      Combo.withKeyInputs(Key.KEY_A)            -> Vector2(-xSpeed, 0.0d),
      Combo.withKeyInputs(Key.KEY_S)            -> Vector2(0.0d, -ySpeed),
      Combo.withKeyInputs(Key.KEY_D)            -> Vector2(xSpeed, 0.0d)
    )
  }
}

```

`Combo` instances can also be combined using the `|+|` operator.
