---
id: input-field
title: Input Field
---

Input fields are text boxes that all users to type values into them. As with button, you need to provide some assets, specifically font information and a graphic to use as the cursor while a user is inputing values. Indigo's input field is quite basic, but input fields are a bit fiddly to implement. Hopefully it will either save someone some time or be useful as a reference to someone who'd like to do make something more sophisticated.

Setting up an input field is as simple as adding something like this to your view model:

```scala mdoc:invisible
import indigo._
import indigo.platform.assets.DynamicText
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
val boundaryLocator = new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)
val context = new FrameContext(GameTime.zero, Dice.fromSeed(1l), InputState.default, boundaryLocator, ())
```

```scala mdoc:silent
import indigo._
import indigoextras.ui._

// Placeholder values
val assets = 
  InputFieldAssets(
    text = Text("", FontKey("my font"), Material.Bitmap(AssetName("my font sheet"))),
    cursor = Graphic(10, 5, Material.Bitmap(AssetName("cursor")))
  )

val inputField =
  InputField("<Default text>", assets)
    .makeSingleLine
    .moveTo(Point(10, 10))
```

Then updating it in the view model:

```scala mdoc:silent
final case class AnotherViewModel(myInputField: InputField)
val anotherViewModel = AnotherViewModel(inputField)

anotherViewModel.myInputField.update(context)
```

...and drawing it:

```scala mdoc:silent
anotherViewModel.myInputField.draw(context.gameTime, context.boundaryLocator)
```

Input fields will also emit an `InputFieldChange` event when the text they hold is altered by a user. They can also send custom events on focus / focus loss.

[The full input field example is in the indigo-examples repo.](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/examples/inputfield)
