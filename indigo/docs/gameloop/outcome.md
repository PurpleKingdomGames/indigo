---
id: outcome
title: Outcome Type
---

## What is an Outcome?

The `Outcome` type is one you'll see a lot.

All of the key functions produce an Outcome. An Outcome is the combination of an updated piece of state, and a list of `GlobalEvent`s that updating the state produced. It can also represent an error that you may or may not be able to recover from.

For example, let's say you update your game's model, and it turns out that the player has lost the game somehow. You might need to do two things:

1. Update the model to reflect the fact that the player's turn is over - perhaps calculating the final score.
1. Emit a `JumpTo(SceneName("game over"))` event to tell Indigo to render the Game Over screen.

This could be described in an Outcome as follows:

```scala mdoc
import indigo._
import indigo.scenes._

final case class Model(totalScore: Int)
val model = Model(0)
def calculateFinalScore(score: Int): Int = score + 10

val outcome = 
  Outcome(model.copy(totalScore = calculateFinalScore(model.totalScore)))
    .addGlobalEvents(SceneEvent.JumpTo(SceneName("game over")))
```

You can access the state or global events with:

```scala mdoc
outcome.getOrElse(Model(0))
outcome.globalEventsOrNil
```

Notice that the syntax is similar to an option (`getOrElse`), this is because outcomes can fail and represent an error that you may be able to recover from.

However the expectation is that you will generally access the values of an Outcome by mapping or perhaps in a for comprehension.

## Examples of Operations on Outcomes

There are lots of ways to manipulate Outcomes, and all of them preserve the events contained in each Outcome.

### Basic operations

An Outcome behaves much like other Monadic types in Scala such as `Option` or `Either`. They are bias towards the state it holds rather than the events. Some basic operations are below:

```scala mdoc
Outcome(10)                                // Outcome(10)
Outcome(10).map(_ * 20)                    // Outcome(200)
Outcome(10).ap(Outcome((i: Int) => i * 5)) // Outcome(50)
Outcome(10).flatMap(i => Outcome(i * 20))  // Outcome(200)
Outcome(10).merge(Outcome(20))(_ + _)      // Outcome(30)
Outcome("a") combine Outcome("b")          // Outcome(("a", "b"))
```

As mentioned, `Outcome`'s map function is bias towards the state, but you can also modify the events with `mapGlobalEvents`.

Sequencing can be done as follows:

```scala mdoc
import Outcome._

List(Outcome(1), Outcome(2), Outcome(3)).sequence // Outcome(List(1, 2, 3))
```

## Creating events based on the state

Sometimes, you need to reference the new state to decide if we should emit an event:

```scala mdoc
final case class Counter(count: Int)

val newState = Counter(count = 10)
val events = if(newState.count > 5) List(PlaySound(AssetName("tada"), Volume.Max)) else Nil

Outcome(newState)
  .addGlobalEvents(events)
```

But this is boring and requires the creation of a couple of variables. The thing to observe is that this scenario is about creating events based on the _updated_ state rather than the original state. Instead, you can do this:

```scala mdoc
Outcome(Counter(count = 10))
  .createGlobalEvents(foo => if(foo.count > 5) List(PlaySound(AssetName("tada"), Volume.Max)) else Nil)
```

Here, `foo` is the state held in the `Outcome`.

## Error handling

> Indigo 0.6.0 or later

The `Outcome` type also comes with error handling.

If an exception is thrown within an outcome's constructor then it will be caught in a similar way to Scala standard `Try` monad. E.g.:

```scala mdoc
Outcome(throw new Exception("Boom!"))
```

Exceptions are a fact of life on the JVM and also in JS. If you access an array index outside it's range, you'll get an exception.

You can model errors with types like `Either[Error, A]` or as an ADT that live in your `Outcome` as it's value:

```scala mdoc
sealed trait MyJourney
case object HappyPath extends MyJourney
case object UnhappyPath extends MyJourney // Error case
```

You could think of these as expected unhappy paths. ...but you're still going to get exceptions from time to time - at least during development.

In Indigo, the hope it that 99 times out of 100 you can get away with an ADT or an `Either` because you've done lots of testing and the unhappy paths are _recoverable_ in a way that is fairly local to the place where the error occurred so that your game can continue. It would be a shame to crash the game.

Sometimes though, you can recover _eventually_ but you need to bail out right now. This is where it might be appropriate to throw an exception or raise an error (`Outcome.raiseError(e)`) in an outcome.

To handle such an error, we can do the following:

> Please note that - contrary to the following example - you should really only catch exceptions you're expecting by declaring a class that extends Exception and catching that.

```scala mdoc
Outcome(10)
  .map[Int](_ => throw new Exception("Boom!"))
  .map(i => i * i)
  .handleError {
    case e =>
      Outcome(e.getMessage.length)
  }
```

Here we start with a value and throw an exception during the first map. We attempt to map the value again, but it's ignored and the exception is carried over. We then handle the error with a partial function that allows us to recover back to a (slightly suspect) `Outcome[Int]` again.

**If you game is going to crash, then you ought to let it crash.**

That said, if your game is going to crash then it might be helpful to log something useful in the dying moments. Indigo itself looks out for exceptions and attempts to log them before it crashes a game. You can give it something meaningful to log by using the `logCrash` outcome method. Invoked similarly to `handleError` above, it is a partial function that takes an exception and returns any string value you like. Indigo then logs this message before the game crashes.

```scala mdoc
Outcome(10)
  .map[Int](i => throw new Exception(i.toString))
  .map(i => i * i)
  .logCrash {
    case e =>
      "The game crashed at integer:" + e.getMessage
  }
```
