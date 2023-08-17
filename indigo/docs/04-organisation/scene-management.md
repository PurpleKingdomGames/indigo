---
id: scene-management
title: Scenes & Scene Management
---

## What are Scenes?

As soon as you decide to build a game of any complexity, you immediately hit the problem of how to organize your code so that everything isn't lumped together. What you'd really like, hopefully, is a nice way to think about each part of your game in logical groups of state and functions i.e. All the things to do with your menu screen in one place, separated from your game over screen.

Most game engines have some sort of concept for these groupings, they're often called scenes, and Indigo is no exception.

## Working example: "Snake!"

If you'd like to dive right in, the Snake implementation uses scenes to manage it's screens. To help you find your way around, here are a few points of interest:

- Initial declaration of the [list of scenes](https://github.com/PurpleKingdomGames/indigo/blob/master/demos/snake/snake/src/snake/SnakeGame.scala#L19).

- The "Start" [scene](https://github.com/PurpleKingdomGames/indigo/blob/master/demos/snake/snake/src/snake/scenes/StartScene.scala), which is one of the simpler scenes in the game.

- The point where the "Start" scene decides to [jump](https://github.com/PurpleKingdomGames/indigo/blob/master/demos/snake/snake/src/snake/scenes/StartScene.scala#L40) to the "Controls" scene (where the player chooses a keyboard layout) after the space bar has been pressed.

## A quick look under the hood

Scenes give the appearance of building a separate game per scene, with only a nod to the underlying mechanics.

The way it works, is that all scene data for every scene is held in the game's model and you provide a way to take it out and put it back again (known as a lens). Then the normal game update and presentation functions are delegated to the currently running scene, and scene navigation is controlled by events. That's it.

> In fact, you could build you're own scene system right on top of one of Indigo's other entry points if you were so inclined.

## Building a game with Scenes

To use scenes, you need to use the `IndigoGame` entry point (extend an object in the usual way and fill in the blanks), which builds on `IndigoDemo` (which builds on `IndigoSandbox`) and adds the additional mechanics for using scenes.

The `IndigoGame` entry point looks almost the same as `IndigoDemo`, but adds these functions:

```scala mdoc:js:shared
import indigo.*
import indigo.scenes.*

// Just examples
final case class BootData(debugModeOn: Boolean)
final case class StartUpData(viewport: Size)
final case class Model(inventory: List[String])
final case class ViewModel(items: List[String])

def scenes(bootData: BootData): NonEmptyList[Scene[StartUpData, Model, ViewModel]] = ???
def initialScene(bootData: BootData): Option[SceneName] = ???
```

These mean that you...

1. That you _must_ provide an _ordered_ `NonEmptyList` (see below) of `Scenes`.
1. That you _can_ provide the name of the first scene Indigo should use, otherwise the scene at the head of the list will be used.

## Building a Scene

A scene is built by creating an object (or class) that extends `Scene[StartupData, GameModel, ViewModel]`. Here's the trait:

```scala mdoc:js
trait Scene[StartUpData, GameModel, ViewModel] derives CanEqual {
  type SceneModel
  type SceneViewModel

  def name: SceneName
  def modelLens: Lens[GameModel, SceneModel]
  def viewModelLens: Lens[ViewModel, SceneViewModel]
  def eventFilters: EventFilters
  def subSystems: Set[SubSystem]

  def updateModel(context: SceneContext[StartUpData], model: SceneModel): GlobalEvent => Outcome[SceneModel]
  def updateViewModel(context: SceneContext[StartUpData], model: SceneModel, viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel]
  def present(context: SceneContext[StartUpData], model: SceneModel, viewModel: SceneViewModel): Outcome[SceneUpdateFragment]
}
```

As you can hopefully see, mostly this is very much like a normal game, but for a few exceptions:

1. No initialization, animations or fonts, that all happens in the main game (shown in the previous section).
1. Scenes have a name, which is important for navigation.
1. Scenes have their own models and view models, more on that later.
1. The game can have global sub systems, and scenes can also have their own subsystems too.

## Funny types

There's a couple of funny types in the code snippets above, namely `NonEmptyList` and `Lens`. The main thing to stress (if you're familiar with them already) is that they are minimal implementations within Indigo itself, and not the fully featured versions you might find in specialist libraries.

This choice - right or wrong - was made because most of Indigo is vanilla Scala* and requires nothing out of the ordinary to work beyond Scala.js, but just occasionally we can do much better with a cleverer type.

The two used above are `NonEmptyList` and `Lens`. The latter is discussed in the next section. A `NonEmptyList` is a `List` that cannot be empty, i.e. it will always have at least one element, and so the normally unsafe (i.e. throws an exception if the list is empty) `.head` method becomes a safe operation. You can think of the encoding as being like this:

```scala mdoc:js
final case class NonEmptyList[A](head: A, tail: List[A]) {
  def toList: List[A] = head :: tail
}
```

> *There is absolutely nothing stopping you from using all your favorite libraries, such as Cats or Monocle.

## Navigating between scenes

The non-empty list of scenes in the original declaration is static, and cannot be added to later in the game. It is also ordered.

Here's the one from Snake:

```scala
def scenes(bootData: GameViewport): NonEmptyList[Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel]] =
    NonEmptyList(StartScene, ControlsScene, GameScene, GameOverScene)
```

Snake also declares it's initial scene like this:

```scala
def initialScene(bootData: GameViewport): Option[SceneName] = Option(StartScene.name)
```

But this isn't actually necessary since `StartScene` is at the head of the list.

To move between scenes you use events, defined simply as:

```scala mdoc:js
enum SceneEvent extends GlobalEvent:
  case Next extends SceneEvent
  case Previous extends SceneEvent
  case JumpTo(name: SceneName) extends SceneEvent
  case SceneChange(from: SceneName, to: SceneName, at: Seconds) extends SceneEvent
```

`Next` and `Previous` proceed forwards and backwards respectively through the list of scenes until they run out. They do not loop back on themselves.

`JumpTo` moves to whichever scene you specify with the `SceneName`. This turns out of be very convenient since scenes are normally objects, and you can just call something like `JumpTo(GameOverScene.name)`.

## State handling with Lenses

The final thing to know about with `Scene`s, is how they manage state.

Essentially the model of the game contains the state for all scenes. This applies to both model and view model, but we'll just talk about the model from now on. Consider a game with the following model:

```scala mdoc:js:shared
final case class MenuModel(menuItems: List[String])
final case class LevelModel(health: Int, inventory: Map[String, Int])
final case class DungeonGameModel(menuScene: MenuModel, level: LevelModel)

val level: LevelModel = LevelModel(10, Map("health potions" -> 3))
val model: DungeonGameModel = DungeonGameModel(MenuModel(List("Press space to start!")), level)
```

Here is a simple lens that will extract the `LevelModel` from the `DungeonGameModel` and put it back again:

```scala mdoc:js
Lens[DungeonGameModel, LevelModel](
  getter = (model: DungeonGameModel) => model.level,
  setter = (model: DungeonGameModel, nextLevel: LevelModel) => model.copy(level = nextLevel)
)
```

Let's declare a scene that is using this fictional model:

```scala mdoc:js
object LevelScene extends Scene[Unit, DungeonGameModel, Unit]:
  type SceneModel = LevelModel
  type SceneViewModel = Unit

  def eventFilters: EventFilters = EventFilters.Permissive
  def modelLens: Lens[DungeonGameModel, LevelModel] = Lens(_.level, (m, lvl) => m.copy(level = lvl))
  def viewModelLens: Lens[Unit, Unit] = Lens.unit
  def name: SceneName = SceneName("level")
  def subSystems: Set[SubSystem] = Set()

  def updateModel(context: SceneContext[Unit], model: LevelModel): GlobalEvent => Outcome[LevelModel] =
    _ => Outcome(model.copy(health = model.health + 1)) // On any event, increase health!

  def updateViewModel(context: SceneContext[Unit], model: LevelModel, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: SceneContext[Unit], model: LevelModel, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)
```

Notice that the `modelLens` uses a shorter version of the same lens we wrote earlier, but it's the same thing.

Now when we run the game, the scene manager uses the lens to extract `LevelModel` from the `DungeonGameModel`, `updateModel` function updates the `LevelModel`, and then the scene manager uses the lens again to replace the old version with the new one.

### Lenses

To formalize this sort of relationship, Indigo has a just-about-good-enough `Lens` implementation (there are no prisms or anything fancy). Lenses are a really interesting subject and if you'd like to know more you could take a look at something like [Monocle](https://github.com/optics-dev/Monocle).

An Indigo lens implements a `get` and a `set` function, like so:

```scala mdoc:js
def get[A, B](from: A): B = ???
def set[A, B](into: A, value: B): A = ???
```

- `get` looks at, in our case, `DungeonGameModel` and extracts / returns `LevelModel`.
- `set` puts a `LevelModel` into a `DungeonGameModel` and returns the new `DungeonGameModel` instance.

Lets try it out! Lets start with the simple `copy` example from earlier:

```scala mdoc:js
val modelLens =
  Lens[DungeonGameModel, LevelModel](
    getter = (model: DungeonGameModel) => model.level,
    setter = (model: DungeonGameModel, nextLevel: LevelModel) => model.copy(level = nextLevel)
  )

modelLens.get(model) // LevelModel
modelLens.set(model, level) // DungeonGameModel
```

You can also do things like construct temporary models by aggregating several parts of the main model into a one-shot case class, that you than de-construct and discard at the end of the frame when putting the values back.

### Lens composition

We can also posed the question of how you update things inside other things, for this we have to compose lenses together using the `andThen` operator, for example:

```scala mdoc:js
final case class Sword(shininess: Int)
final case class Weapons(sword: Sword)
final case class Inventory(weapons: Weapons)

val inventoryLens: Lens[Inventory, Weapons] =
  Lens(_.weapons, (i, w) => i.copy(weapons = w))

val weaponsLens: Lens[Weapons, Sword] =
  Lens(_.sword, (w, s) => w.copy(sword = s))

val inventory     = Inventory(Weapons(Sword(1)))
val betterSword   = Sword(2)

val mySwordLens = 
  inventoryLens andThen weaponsLens // Composing lenses!

mySwordLens.get(inventory) // a sword
mySwordLens.set(inventory, betterSword) // an inventory with a better sword in it

val polishTheSword: Sword => Sword = s => s.copy(shininess = s.shininess + 1)

mySwordLens.modify(inventory, polishTheSword)
// modify is the just the composition of get, set, and a function f.
```

### Limitations

As with all things in Indigo, the `Lens` implementation is the bare minimum needed - if you assume most models are just nested objects - and does not currently support things like prisms.

No doubt extra functionality will be added as soon as the need arises, but in the meantime, note that Indigo's lens definition says nothing about how it's implemented. If you had a complicated case, you could look at building your lenses using a Scala.js compatible lens library, and just use the Indigo Lens as an interface to the engine.

## Global operations

The `IndigoGame` entry point also provides the standard update and present functions that run at a global level. This wasn't in the original design because it is quite confusing! Unfortunately it's also very useful and the benefits out-weigh the complexity drawbacks.

**The Good!**

Example: Your model is comprised of the data needed for each level as a separate field in a case class, and you have a global inventory. You'd like to be able to update both.

One way to do that is with a slightly complicated lens arrangement that knows how to extract both into a temporary object that only exists for this frame, and then de-construct that object to set the new values back into the main model. That works, but it get increasingly complicated the more elements you need to draw together, and eventually the temptation is just to use the whole model, which defeats the point of having the lenses.

The other way, as long as the update isn't time critical, is to emit an event! If you emit an event then the event can be caught in the next frame at the global level and the inventory can be updated. Nice and clean. As mentioned this works well for updates that aren't time critical since the effect won't be visible until the frame after next.

**The not so good..**

You'd like to render a global UI at all times - great - this can be done with our global present function.

_...but how will that be merged with the scene's own present function?_

Well, the global view is always processed first. The idea is that you should use layers with layer keys to control output destinations since the original position of layers with keys is preserved when another layer with the same key is added to the scene.

## Tips for working with Scenes

### Passing events between scenes

When one scene is running none of the others are, but sometimes it's useful to be able to pass a message to the scene you're about to switch to.

This turns out to be very easy in Indigo. Since events are ordered and strictly evaluated, all you need to do is:

```scala mdoc:js
final case class MessageForNextScene(message: String) extends GlobalEvent

Outcome(model) // here, the result of a model update, but could be any Outcome
  .addGlobalEvents(
    SceneEvent.JumpTo(SceneName("next!")),
    MessageForNextScene("Hello next scene!")
  )
```

The first event will re-route all functions to the new scene, and the next event will therefore be received immediately by that scene.
