---
id: button
title: Buttons
---

The out-of-the-box button is created out of three graphics that represent the up, over, and down states. Aside from handling the button's state, the main advantage of using the button component is that it's easier to to define interactions. Rather than pattern matching on a click event at the top of your update function, and then deciding whether the click happened inside the button or not, you can just define you button as follows and it will do the rest for you, once it has been wired in:

```scala mdoc:silent
import indigo._
import indigoextras.ui._

case object LaunchTheRocket extends GlobalEvent

val buttonAssets =
  ButtonAssets(
    up = Graphic(50, 50, Material.Bitmap(AssetName("up"))),
    over = Graphic(50, 50, Material.Bitmap(AssetName("over"))),
    down = Graphic(50, 50, Material.Bitmap(AssetName("down")))
  )

val button = 
  Button(
    buttonAssets = buttonAssets,
    bounds = Rectangle(10, 10, 16, 16),
    depth = Depth(2)
  ).withUpActions(LaunchTheRocket)
```

[The full button example is in the indigo-examples repo.](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/examples/button)

A quick explanation of the `updateViewModel` function in the example above, it looks like this:

```scala mdoc:silent
final case class MyViewModel(button: Button)

def updateViewModel(context: FrameContext[Unit], model: Unit, viewModel: MyViewModel): GlobalEvent => Outcome[MyViewModel] = {
  case FrameTick =>
    viewModel.button.update(context.inputState.mouse).map { btn =>
      viewModel.copy(button = btn)
    }

  case _ =>
    Outcome(viewModel)
}
```

To help see what's happening here, we could rewrite this:

```scala mdoc:invisible
import indigo._
import indigo.platform.assets.DynamicText
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
val boundaryLocator = new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)
val context = new FrameContext(GameTime.zero, Dice.fromSeed(1l), InputState.default, boundaryLocator, ())
```

```scala mdoc:silent
val viewModel = MyViewModel(button)

viewModel.button.update(context.inputState.mouse).map { btn =>
  viewModel.copy(button = btn)
}
```

...as:

```scala mdoc:silent
for {
  updatedButton    <- viewModel.button.update(context.inputState.mouse)
  updatedViewModel <- Outcome(viewModel.copy(button = updatedButton))
} yield updatedViewModel
```

First we have to update the button, which is done by calling the buttons's built in update method and supplying the current state of the mouse on this frame. The button update returns an [`Outcome`](gameloop/outcome.md) because as well as containing a freshly updated button, it can also return events that will need to be collected at the end of the frame. Since we need to return an outcome containing an updated view model at the end of the frame, we then need to map over the outcome, and insert the button in the view model.

***A word of caution***, you might be tempted to do this instead, which appears to work and compiles just fine:

```scala mdoc:silent
val updatedButton = viewModel.button.update(context.inputState.mouse).unsafeGet
val updatedViewModel = viewModel.copy(button = updatedButton)

Outcome(updatedViewModel)
```

The trouble is the by pulling the button instance out of the `Outcome` after the update by calling `.state`, ***you lose any events that the button generated during it's update***. You must `map` or `flatMap` over the `Outcome` types.
