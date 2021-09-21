---
id: subsystems
title: SubSystems
---

## What SubSystems are

`SubSystem`s are a way of breaking parts of your game off into mini-games. They offer you a means of encapsulation for certain kinds of game system.

Typically a subsystem is made from an object or class that extends this trait, or by using the `SubSystem.apply` constructor. SubSystem's can produce renderable output or just sit and process things in the background. Either way their only mechanism for interacting with the main game is through the event loop.

As an example, consider this simple (and arguably unhelpful) subsystem that tracks a score. This one happens to be using a `case class` as a convenient way to supply the initial score, but this could have been done in other ways. The important part is that the constructor arguments are effectively immutable, because the `update` function returns an `Int`, not a `PointsTrackerExample`.

> ["The Cursed Pirate"](https://github.com/PurpleKingdomGames/indigo-examples/blob/master/demos/pirate/src/main/scala/pirate/scenes/level/subsystems/CloudsSubSystem.scala) uses an alternative and arguable cleaner SubSystem construction method than the one below.

```scala mdoc
import indigo._

final case class PointsTrackerExample(startingPoints: Int) extends SubSystem {
  type EventType      = PointsTrackerEvent
  type SubSystemModel = Int

  val eventFilter: GlobalEvent => Option[PointsTrackerEvent] = {
    case e: PointsTrackerEvent => Option(e)
    case _                     => None
  }

  def initialModel: Outcome[Int] =
    Outcome(startingPoints)

  def update(context: SubSystemFrameContext, points: Int): PointsTrackerEvent => Outcome[Int] = {
    case PointsTrackerEvent.Add(pts) =>
      Outcome(points + pts)

    case PointsTrackerEvent.LoseAll =>
      Outcome(0)
        .addGlobalEvents(GameOver)
  }

  def present(context: SubSystemFrameContext, points: Int): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(Text(points.toString, FontKey(""), Material.Bitmap(AssetName("font"))))
    )
}

sealed trait PointsTrackerEvent extends GlobalEvent with Product with Serializable
object PointsTrackerEvent {
  case class Add(points: Int) extends PointsTrackerEvent
  case object LoseAll         extends PointsTrackerEvent
}

case object GameOver extends GlobalEvent
```

SubSystems are really useful for doing nice bits of encapsulated work that add the all-important sense of polish to your game, but that you'd rather not have polluting your main game logic. For example: You might like to have a system of clouds floating through the sky, or a pinball score counter rattling up - they look great - but as purely visual effects the do not represent important data (in terms of saving your game state) and can be handled independently of your main game.

> Hypothetically, they can also be used as really good encapsulation mechanisms for async or side-effecting processes. For example, perhaps you'd like to call down to the browsers DOM in a clean way? Or run an FS2/Cats based process? You could make a subsystem that talks to your game via nice clean events, but internally does whatever specialist logic you need.

The Indigo Extras module gives you two really helpful SubSystems: Automata for particle-like effects (not a real particle system in the conventional sense), and an "Asset Bundle Loader" that can be used for dynamically loading new assets during your game, both of which are used in ["The Cursed Pirate"](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/demos/pirate).

## How SubSystems work

Indigo's APIs are an exercise in composition, and if we ignore the state for a moment, the functions of a frame are approximately:

```scala
def update: context => Outcome[_] = ??? // next version of the model
def present: context => SceneUpdateFragment = ??? // What graphics to draw, what audio to play
```

Which is exactly what you can see in the trait definition above. Yes, the standard entry points for indigo look more complicated, but really they all boil down to this.

Importantly, the context is immutable and the result types are monoidal.

This means we can imagine doing something like this when our frame is executed:

```scala
// All the outcomes combined
(context) =>
  game.update(context) |+| subsystem1.update(context) |+| subsystem2.update(context)

// All the scene fragments combined
(context) =>
  game.render(context) |+| subsystem1.render(context) |+| subsystem2.render(context)
```

That isn't accurate or the full picture by any means, but hopefully it gives you a sense of how Indigo puts things together.

## Adding a SubSystem to your game

The Sandbox entry point does not cater for subsystems, but the Demo and Game entry points both allow you to add global subsystems to the `BootResult` type using the `addSubSystems` method.

You cannot use model or start up data to initialise SubSystems, but you can use Boot data, just in case things like the configured magnification level or screen dimensions are important.

Additionally, you can also add scene specific SubSystems to individual scene definitions. For example in "The Cursed Pirate", the loading scene makes use of the Asset Bundle Loader SubSystem, but this is no longer updated once we switch to the demo level itself, since by then all the assets have been loaded.
