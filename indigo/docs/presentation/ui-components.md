---
id: ui-components
title: UI Components
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

> UI Components currently live in the "Indigo Extras" library, since they are built on top of Indigo itself and require no special machinery to work.

UI components are the kinds of elements your expect to see in any web input form, or game options menu.

At the time of writing, Indigo does not provide a large suite of UI Components out of the box although we hope to expand, [see issue for progress](https://github.com/PurpleKingdomGames/indigo/issues/41). This is because _basic_ UI components are not terribly complicated to build on top of Indigo by aspiring game devs, and so have been pushed down the priority list in favor of more fundamental / specialized pieces of functionality.

## The Pattern

The components Indigo does provide (buttons and input fields) follow a very specific pattern. The idea is that while the values UI components temporarily represent are interesting, and should be stored in the model (e.g. the players name for their character), the UI Components themselves are not interesting and should be somewhat ephemeral. In other words, you'd want to save the characters name, but not the state of an input field. Therefore in their current design, UI Components are only supposed to live in the view model and the view.

The pattern UI components currently follow then, is as follows:

1. User provided assets - you need to provide information about what Indigo should use to draw your components.
2. An entry in the View Model - UI components hold a small amount of state, and it is designed to be stored in the view model.
3. Presentation - pulling the assets, state, and relevant events together to draw the component.

The main thing to be aware of is that UI Components are not magic. In an OO game engine, you could expect the add a button and for it to be a self contained entity that at least renders itself without a lot of wiring. In Indigo - like everything in Indigo! - you have to stitch them into the relevant processes, i.e. the button won't mysteriously draw itself if it isn't included in your view logic.

## Available Components

### Buttons

The out-of-the-box button is created out of three graphics that represent the up, over, and down states. Aside from handling the button's state, the main advantage of using the button component is that it's easier to to define interactions. Rather than pattern matching on a click event at the top of your update function, and then deciding whether the click happened inside the button or not, you can just define you button as follows and it will do the rest for you, once it has been wired in:

```scala mdoc
Button(
  buttonAssets = buttonAssets,
  bounds = Rectangle(10, 10, 16, 16),
  depth = Depth(2)
).withUpActions(LaunchTheRocket)
```

[The full button example is in the indigo-examples repo.](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/examples/button)

A quick explanation of the `updateViewModel` function in the example above, it looks like this:

```scala mdoc
def updateViewModel(context: FrameContext[Unit], model: MyGameModel, viewModel: MyViewModel): GlobalEvent => Outcome[MyViewModel] = {
  case FrameTick =>
    viewModel.button.update(context.inputState.mouse).map { btn =>
      viewModel.copy(button = btn)
    }

  case _ =>
    Outcome(viewModel)
}
```

To help see what's happening here, we could rewrite this:

```scala mdoc
viewModel.button.update(context.inputState.mouse).map { btn =>
  viewModel.copy(button = btn)
}
```

...as:

```scala mdoc
for {
  updatedButton    <- viewModel.button.update(context.inputState.mouse)
  updatedViewModel <- Outcome(viewModel.copy(button = updatedButton))
} yield updatedViewModel
```

First we have to update the button, which is done by calling the buttons's built in update method and supplying the current state of the mouse on this frame. The button update returns an [`Outcome`](gameloop/outcome.md) because as well as containing a freshly updated button, it can also return events that will need to be collected at the end of the frame. Since we need to return an outcome containing an updated view model at the end of the frame, we then need to map over the outcome, and insert the button in the view model.

***A word of caution***, you might be tempted to do this instead, which appears to work and compiles just fine:

```scala mdoc
val updatedButton = viewModel.button.update(context.inputState.mouse).state
val updatedViewModel = viewModel.copy(button = updatedButton)

Outcome(updatedViewModel)
```

The trouble is the by pulling the button instance out of the `Outcome` after the update by calling `.state`, ***you lose any events that the button generated during it's update***. You must `map` or `flatMap` over the `Outcome` types.

### Input Fields

Input fields are text boxes that all users to type values into them. As with button, you need to provide some assets, specifically font information and a graphic to use as the cursor while a user is inputing values. Indigo's input field is quite basic, but input fields are a bit fiddly to implement. Hopefully it will either save someone some time or be useful as a reference to someone who'd like to do make something more sophisticated.

Setting up an input field is as simple as adding something like this to your view model:

```scala mdoc
InputField("<Default text>", assets)
  .makeSingleLine
  .moveTo(Point(10, 10))
```

Then updating it in the view model:

```scala mdoc
viewModel.myInputField.update(context)
```

...and drawing it:

```scala mdoc
viewModel.myInputField.draw(context.gameTime, context.boundaryLocator)
```

Input fields will also emit an `InputFieldChange` event when the text they hold is altered by a user. They can also send custom events on focus / focus loss.

[The full input field example is in the indigo-examples repo.](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/examples/inputfield)

### Radio Buttons

Radio buttons are a collection of buttons where only one of them can be in a selected state at any given time, and one must always be selected once an initial selection has been made. No doubt you've seen them on multiple choice forms.

To set up Radio buttons, you would initialize them in your view model as follows:

```scala mdoc
RadioButtonGroup(buttonAssets, 16, 16)
  .withRadioButtons(
    RadioButton(Point(5, 5))
      .withSelectedActions(MyRadioButtonEvent(RGBA.Red))
      .selected,
    RadioButton(Point(25, 5)).withSelectedActions(MyRadioButtonEvent(RGBA.Green)),
    RadioButton(Point(45, 5)).withSelectedActions(MyRadioButtonEvent(RGBA.Blue))
  )
```

In the example above, we construct an empty `RadioButtonGroup` group with `ButtonAssets` display data, and a width and height. The width and height are really producing a hit area `Rectangle(0, 0, width, height)` that is used to decide if a mouse action should be accounted for. The hit area does not need to start at `0, 0` though, and can be changed with the `.withHitArea` method.

We then add a group of radio buttons that all share the same button assets, giving each a unique position and defining what events will be fired as the radio button pass through the different states we care about.

> Please note that each radio button can optionally have a unique `ButtonAssets` instance and hit area. When unspecified, the radio buttons will use the group level versions.

[The full radio button example is in the indigo-examples repo.](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/examples/radio)
