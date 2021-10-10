---
id: time-varying-values
title: Time Varying Values
---

Time varying values describe numeric values (`Double`s) that will change over time. They are supposed to live in your game model to help you do simple time based updates.

They were designed for use with the NPC job system.

For example:

You have a little lumber jack, he walks over to a tree and is now going to cut to down. Work is effort over time, and you can track his progress by having an `IncreaseTo` time varying value in your model, like this.

```scala mdoc
import indigo._
import indigoextras.datatypes._

// A time varying value that here represents the percent of work done
val woodChoppingProcess: IncreaseTo =
  IncreaseTo(
    value = 0,
    unitsPerSecond = 10,
    limit = 100
  )

// A lumber jack character
final case class LumberJack(chopWood: IncreaseTo, working: Boolean):
  def update(gameTime: GameTime): LumberJack =
    if working then
      if chopWood.value == chopWood.limit then
        this.copy(working = false)
      else
        this.copy(
          chopWood = chopWood.update(gameTime.delta)
        )
    else this

// Inititally, our lumber jack is working on a tree but yet to swing his axe.
val lumberJack = LumberJack(woodChoppingProcess, true)
```

You then need to update the lumberJack during your frame tick update `lumberJack.update(gameTime)`, and the time varying value will alter the value for you independent of frame rate.

You can then render a progress bar as a percentage in the view by simply asking the `TimeVaryingValue` for its current `value`.

## Types of time varying value

- `Increase` - increases a starting value with no upper limit
- `IncreaseTo` - increases a starting value until it reaches an upper limit
- `IncreaseWrapAt` - increases a starting value until it reaches an upper limit, then wraps back to zero.
- `Decrease` - decreases a starting value with no lower limit
- `DecreaseTo` - decreases a starting value until it reaches an lower limit
- `DecreaseWrapAt` - decreases a starting value until it reaches an lower limit, then wraps back to zero.
