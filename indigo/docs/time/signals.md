---
id: signals
title: Signals & Signal Functions
---

## Motivation

We want pure, referentially transparent, testable, procedural animations.

> Definition: A procedural animation is an animation produced by code, rather than created by hand in, say, an animation tool and played back.

## Background

The goal of Indigo is to make programming games (as a pose to making them...), easier to reason about and easier to test by leveraging the good ideas that come with functional programming.

One of those good ideas that Indigo borrows is the notion of Signals and Signal Functions. Signals for animation were first proposed in the Functional Reactive ANimation (FRAN) system, but are most readily seen in [Yampa](https://github.com/ivanperez-keera/Yampa).

Indigo makes use of Signals too, though not to the same extent as Yampa. The main difference is that Signals in Indigo are stateless and therefore somewhat limited. Nonetheless, Signals in Indigo are still interesting and useful, and provide the backbone of the Automata subsystem.

## `Signal[A]`

At it's core, a signal is a very simple thing. Consider this hypothetical abstract function signature:

```scala
val f: A => B
```

What this function signature says is that when provided some value of type `A`, it _will_ produce some value of type `B`. In concrete terms, if we fix the types to known primitives, the follow example says that when given a `String`, it will return an `Int`:

```scala
val f: String => Int
```

A `Signal[A]` is similar and looks like this:

```scala mdoc
import indigo._

final case class Signal[A](f: Seconds => A)
```

In other words, a `Signal` of `A` is nothing more than a function that when given the current running time in `Seconds`, _will_ produce some value of type `A`.

## Signals & Animation

Lets consider a very very simple animation: Moving a graphic from coordinates (0, 0) to (10, 0), i.e. 10 pixels along the x-axis.

If you animated this by hand, you'd have a series of animation frames rendered to an image that you playback one after the other, which is fine but rather limited.

So instead, lets think about how we might produce this animation in code.

### Just move it along the x-axis

We could just say:

1. On frame tick
2. Increment the x of the object by 1
3. if the x > 10, make it 10

That will work, but the trouble is that the animation will be jerky*. Since the amount of time between frame updates varies and you are moving the object by a fixed amount regardless of time, it will speed up and slow down rapidly and look jittery.

> *It _would_ be jerky if you had time to see it. The animation would be complete in a fraction of a second!

### Can we introduce frame independence?

To fix the jitter above, all we have to do is factor in time, as follows:

1. On frame tick, and grab the time delta in seconds
2. Increment the x of the object by 1 * the time delta
3. if the x > 10, make it 10

Here the time delta is the amount of time that has elapsed since the last frame. What we're saying is that "1" is the number of units to move per second, and by multiplying it by the time delta we will move at a rate of 1 units per second across the screen.

That's a much slower rate of progress than method #1 but if we want it to go faster we can just increase the units per second. The point is that progress across the screen is now smooth since time is taken into consideration and the amount of movement is proportional to the time elapsed. This is known as frame independence.

***...except it doesn't work.*** Or rather, in Indigo it is subject to a new failure mode: Integer rounding.

Since it is designed for pixel art, Indigo works in whole pixels by default which are represented as integers. Consider the following:

```scala mdoc
val positionX: Int = 0 // We start at 0 but this works at any value
val unitsPerSsecond: Int = 1 // move at 1 pixel per second
val timeDelta: Double = 0.01666666667 // Average time delta for 60 frames per second

(positionX + 1 * timeDelta).toInt // toInt for pixels, which are integers
// res2: Int = 0 // Oops!
```

We made no progress! In fact we will never make any progress because it doesn't matter how many times you add zero to anything the answer is always the original value! Gah!

### Using a higher precision

Frame independence is an improvement, but we need something else.

One thing we can do is to hold the position information in a higher precision type like a `Double` or a `Float`. We can't keep that information in the view though, so to hold that bit of state we need to purposefully put it in the model or view model somewhere.

Assuming we do hold that state, we have now separated the model from the presentation (slightly). In the model we work in "world space" (in `Double`s) and convert into screen space to draw (by converting the `Double` value to `Int` at that last moment).

> In this example, converting from world space to screen space is simply casting a Double to an Int, but it's common for presentation and model representations to be very different.

This is the right solution for any animation where taking the trouble to model and store the data is useful. A character walking across the screen that needs to change direction in response to the world for example.

But sometimes that's just noise. For example, imagine a character picking up a gold coin and a little dollar sign floating up over their heads momentarily. It's an important visual aid, but modeling and tracking the state for that little dollar sign's motion is neither interesting or useful in the wider context of the game.

Is there another way?

### Stateless procedural animations

Lets start over. All we want do is smoothly move along the x axis by 10 pixels over a certain duration, say 10 seconds.

Which means that how far through our animation we are, is actually a function of time, i.e. at time 0 seconds we're at the beginning of our animation, at 5 seconds we're in the middle, and at 10 seconds we've reach the end. That could be frames in a animation, position along a trajectory, time through a simulation - anything.

To make this work we need some initial conditions and a function that given those conditions with the current time produces the correct animation output.

Lets try it in vanilla Scala first.

```scala mdoc
def calculatePosition(
    startPositionX: Int, // initial condition
    runningTime: Double // how long the animation has been running, a relative value
  ): Int = { // Our output x position as a whole pixel
  val clampedTime: Double = if(runningTime > 10d) 10d else runningTime
  val distanceToMove: Double = 10
  val pixelsPerSecond = 1 / distanceToMove

  startPositionX + (distanceToMove * pixelsPerSecond * clampedTime).toInt
}

calculatePosition(0, 0)
// res2: Int = 0

calculatePosition(0, 2.5)
// res2: Int = 2

calculatePosition(0, 5)
// res2: Int = 5

calculatePosition(0, 7.5)
// res2: Int = 7

calculatePosition(0, 10)
// res2: Int = 10

calculatePosition(0, 15)
// res2: Int = 10
```

So that's an animation described in terms of a function of time ...which means we must be able to encode that as a `Signal`, so lets try the `Signal` version:

```scala mdoc:reset
import indigo._

def calculatePosition(startPositionX: Int): Signal[Int] =
  Signal { t =>
    val clampedTime: Double = if(t.toDouble > 10d) 10d else t.toDouble
    val distanceToMove: Double = 10
    val pixelsPerSecond = 1 / distanceToMove

    startPositionX + (distanceToMove * pixelsPerSecond * clampedTime).toInt
  }

val signal = calculatePosition(0)

signal.at(Seconds(0))
// res2: Int = 0

signal.at(Seconds(2.5))
// res2: Int = 2

signal.at(Seconds(5))
// res2: Int = 5

signal.at(Seconds(7.5))
// res2: Int = 7

signal.at(Seconds(10))
// res2: Int = 10

signal.at(Seconds(15))
// res2: Int = 10
```

Very similar! The result is that we have now captured the animated value as a frame independent function of time that requires no ongoing state management.

### Lets do the time warp

To get the value out of a signal, you just need to tell give it a time in seconds, e.g. `signal.at(Seconds(5))`. Where things get interesting is that there is no requirement to give it the "next" time. You can give it any time you like.

Want to play the animation backwards?
`signal.at(total time - running time)`

Want to play the animation at half speed?
`signal.at(running time * 0.5)`

Want to jump to random "frames" in the animation?
`signal.at(Seconds(dice.roll(10)))`

Want to squidge (technical term) backwards and forwards through the animation?
`Signal.SmoothPulse.flatMap(signal.at(total time * _))`

## Testing

Since the animation is now captured as a pure function based on time, you can now test your animation!

You can replicate something simple in a unit test, not unlike the examples above, or you can use property based testing as demonstrated in the [fireworks example](https://github.com/PurpleKingdomGames/indigo-examples/blob/master/examples/fireworks/src/test/scala/indigoexamples/model/TrailParticleSpecification.scala).

## Signal construction

Making signals can get complicated, particularly if you try to wrap up all of the business logic in a single `Signal` definition as we have done above.

Signals are Monads, meaning that many of the usual functions like `map`, `ap`, and `flatMap` that you'd expect to see are available to use. For example, here is a signal being constructed in a for comprehension:

```scala mdoc:reset
import indigo._

val signal =
  for {
    a <- Signal.fixed(10)
    b <- Signal.fixed(20)
    c <- Signal.fixed(30)
  } yield a + b + c

signal.at(Seconds.zero) // 60
```

This helps a lot with building signal values, but another useful construct is the `SignalFunction`.

## `SignalFunction`s

`SignalFunction`s allow you to compose signals and functions that operator on signals together.

Signal functions are combinators, and a combinator is a function that takes a function as an argument and returns another function, like this:

```scala
// A function that take as an argument a function that takes an
// Int and returns a String, and then returns a function that
// takes and Int and returns a Boolean
val f: (Int => String) => (Int => Boolean)
```

A Signal function takes a `Signal[A]` and returns a `Signal[B]`. Recall that a `Signal[T]` is really just a function from `time` to `T`, thus:

```scala
SignalFunction(f: Signal[A] => Signal[B])
```

is really a combinator:

```scala
// pseudo scala
SignalFunction((time => A) => (time => B))
```

The constructor for a Signal function is actually `SignalFunction(f: A => B)`, as a pose to `SignalFunction(f: Signal[A] => Signal[B])`. this is much more convenient and better explains what you're actually doing with signal functions. Here's a simple example:

```scala mdoc:reset
import indigo._

val signal = Signal.fixed(10) |> SignalFunction((i: Int) => "count: " + i.toString)

signal.at(Seconds.zero)
// "count: 10"
```

In this example we pipe a fixed value (ignores time) into a signal function which prints a string. So far, we could have achieved this by just using `map`:

```scala mdoc:reset
import indigo._

val signal = Signal.fixed(10).map(i => "count: " + i.toString)

signal.at(Seconds.zero)
// "count: 10"
```

You don't _need_ signal functions, but they are a nice way to describe combining and processing signals. Here is an more interesting example:

```scala mdoc:reset
import indigo._

val makeRange: SignalFunction[Boolean, List[Int]] =
  SignalFunction { p =>
    val num = if (p) 10 else 5
    (1 to num).toList
  }

val chooseCatsOrDogs: SignalFunction[Boolean, String] =
  SignalFunction(p => if (p) "dog" else "cat")

val howManyPets: SignalFunction[(List[Int], String), List[String]] =
  SignalFunction {
    case (l, str) =>
      l.map(_.toString + " " + str)
  }

// Pulse is a type of signal. Based on the time, it with produce
// an on/off boolean like:
//      ____    ____    ____
//  ___|   |___|   |___|   |___
val signal = Signal.Pulse(Seconds(1))

// &&& / and - run in parallel and tuple the results
// >>> / andThen - compose the functions together from left to right
val signalFunction = (makeRange &&& chooseCatsOrDogs) >>> howManyPets
// or
// val signalFunction = (makeRange and chooseCatsOrDogs) andThen howManyPets

(signal |> signalFunction).at(Seconds.zero)
// List("1 dog", "2 dog", "3 dog", "4 dog", "5 dog", "6 dog", "7 dog", "8 dog", "9 dog", "10 dog")

(signal |> signalFunction).at(Seconds(1))
// List("1 cat", "2 cat", "3 cat", "4 cat", "5 cat")
```

## SignalReader

`// TODO`

## SignalState

`// TODO`
