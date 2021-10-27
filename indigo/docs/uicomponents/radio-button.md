---
id: radio-button
title: Radio Button
---

Radio buttons are a collection of buttons where only one of them can be in a selected state at any given time, and one must always be selected once an initial selection has been made. No doubt you've seen them on multiple choice forms.

To set up Radio buttons, you would initialize them in your view model as follows:

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

final case class MyRadioButtonEvent(color: RGBA) extends GlobalEvent

val buttonAssets =
  ButtonAssets(
    up = Graphic(50, 50, Material.Bitmap(AssetName("up"))),
    over = Graphic(50, 50, Material.Bitmap(AssetName("over"))),
    down = Graphic(50, 50, Material.Bitmap(AssetName("down")))
  )

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
