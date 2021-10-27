---
id: hit-area
title: Hit Area
---

Hit areas are different to the other UI components.

If you want to make a really _really_ fancy button, a common trick is actually to have the visual elements of a button be some sort of animation on the screen, and the "button" is an invisible component floating above the pretty visuals.

Another thing you might want to do is make a point and click adventure when the player must try clicking on parts of the scene to see if anything happens.

That is what hit areas are for, except in Indigo, they aren't even invisible - they are in fact not there!

Like other ui components, hit areas are intended to live in your view model, and are comprised of a closed polygon and actions that fire events.

Here is an example:

```scala mdoc:silent
import indigo._
import indigoextras.ui._
import indigoextras.geometry._

final case class Log(message: String) extends GlobalEvent
final case class ViewModel(hitArea: HitArea):
  def update(mouse: Mouse): Outcome[ViewModel] =
    hitArea.update(mouse).map { ha =>
      this.copy(hitArea = ha)
    }

val points =
  List(Point(5, 0), Point(0, 5), Point(5, 10), Point(10, 5))
    .map(Vertex.fromPoint)

val viewModel =
  ViewModel(
    HitArea(Polygon.Closed(points))
      .moveTo(175, 10)
      .withClickActions(Log("I was clicked!"))
  )
```

You must then update as usual during the `updateViewModel` function:

```scala mdoc:invisible
import indigo._
import indigo.platform.assets.DynamicText
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
val boundaryLocator = new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)
val context = new FrameContext(GameTime.zero, Dice.fromSeed(1l), InputState.default, boundaryLocator, ())
```

```scala mdoc:silent
viewModel.update(context.mouse)
```
