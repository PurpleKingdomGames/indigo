---
id: howto-indigo-game
title: Converting `IndigoSandbox` to `IndigoGame`
---

## Basic Concepts

You may have noticed that many of our tutorials and examples make use of
`IndigoSandbox` and `IndigoDemo`. But what if you want to go further than these
examples? How would you add scene management, or deal with sub systems? In these
cases it's time to move your game to use `IndigoGame`, which comes with a host
of features, but also a lot more boiler-plate code.

Note: In this tutorial we'll be moving our code to `IndigoGame` and using an
empty scene, but we won't be making full use of the scene management.
If you want to know more about managing scenes in Indigo you can read the
documentation [here](https://indigoengine.io/docs/organisation/scene-management).

Before we begin, we'll start by defining the types of data that `IndigoGame`
uses and what they may be used for:

* BootData: Used for initial boot data, such as your foundation game settings
* StartUpData: Used for game data or config that may change during asset loading, screen resizes etc.
* Model: The model holding your main game data
* ViewModel: The model used when drawing data. Typically this is transformed from your model

## `IndigoSandbox` to `IndigoGame`

One of the main examples we have is called `hello-indigo`, which you can clone from
 [here](https://github.com/PurpleKingdomGames/hello-indigo) (mill)
or [here](https://github.com/PurpleKingdomGames/hello-indigo-sbt) (sbt). In this
tutorial we're going to take that example and convert it to `IndigoGame`.

The first thing to do here is to make the initial conversion. Change the object declaration like so:

```diff
 @JSExportTopLevel("IndigoGame")
-object HelloIndigo extends IndigoSandbox[Unit, Model] {
+object HelloIndigo extends IndigoGame[Unit, Unit, Model, Unit] {
```

For this example we set boot and startup data to `Unit` (as we're not going to
need them), keep our model, and then use `Unit` for the view model as well.

This example doesn't use animations, fonts, or shaders, so we can remove those
declarations too. We'll keep the asset declaration
but it's worth noting that we no longer need to declare assets this way, we're
keeping it like this for convenience.

```diff
   val config: GameConfig =
     GameConfig.default.withMagnification(magnification)

-  val animations: Set[Animation] =
-    Set()
-
   val assetName = AssetName("dots")

   val assets: Set[AssetType] =
       AssetType.Image(AssetName("dots"), AssetPath("assets/dots.png"))
     )

-  val fonts: Set[FontInfo] =
-    Set()

-  val shaders: Set[Shader] =
-    Set()
```

## Scene Management

`IndigoGame` comes with a built in scene manager. For this exercise we're using
a single scene, so we may as well keep all our logic in this one file. For
bigger projects though you'll want to explore
[how scenes work](https://indigoengine.io/docs/organisation/scene-management).

We'll need to import the appropriate package so that we can use scenes. We can
do that by adding this line to our imports:

```diff
 import indigo._
+import indigo.scenes._
 import scala.scalajs.js.annotation.JSExportTopLevel
```

We're going to add a dummy scene to this game as we're doing a very simple
example. This is done by adding the following lines to `HelloIndigo.scala`:

```diff
+  def initialScene(bootData: Unit): Option[SceneName] =
+    None
+
+  def scenes(bootData: Unit): NonEmptyList[Scene[Unit, Model, Unit]] =
+    NonEmptyList(Scene.empty)
```

## Events

We'll need to filter our events, after all it's inefficient to send all events
to your game if you aren't needing to process them. We'll use the permissive
event filters here, as that excludes subsystem specific events, but will
allow the click event, which is the only one we need right now. Add the following:

```diff
+  val eventFilters: EventFilters =
+    EventFilters.Permissive
```

## Booting

We can now tell Indigo which assets to load at
[boot time](https://indigoengine.io/docs/organisation/boot-and-start-up).
This is where we'll make use of the assets and config `val` that we left earlier.
Add the following:

```diff
+  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
+    Outcome(
+      BootResult
+        .noData(config)
+        .withAssets(assets)
+    )
```

We'll also need to add the boot data to the `setup` signature:
```diff
  def setup(
+     bootData: Unit,
      assetCollection: AssetCollection,
      dice: Dice
  ): 
```

Here we're telling Indigo to boot using the config already assigned and
the 'dots' asset. It's worth noting that if anything fails at this stage
Indigo won't boot.

## View Models

We're almost done! Just a couple more steps and we can compile and see our handy
work.

We need to tell Indigo how to initialise and update our view models. As
we're using `Unit` for our view models, this is simply a case of passing that
back for both instances. We can do this like so:

```diff
+  def initialViewModel(startupData: Unit, model: Model): Outcome[Unit] =
+    Outcome(())
+
+  def updateViewModel(
+      context: FrameContext[Unit],
+      model: Model,
+      viewModel: Unit
+  ): GlobalEvent => Outcome[Unit] =
+    _ => Outcome(())
```

## Presenting

The `present` method is ever so slightly different for an `IndigoGame` as it
also takes a View Model as an argument. Simply add the correct argument like so:

```diff
   def present(
       context: FrameContext[Unit],
-      model: Model
+      model: Model,
+      viewModel: Unit
   ): Outcome[SceneUpdateFragment] =
```

## Compile, Enjoy, and Further Reading

And that's it! If you now compile that code and run it either in a browser or
in electron the results should be exactly the same as they were before. If,
for whatever reason, you've gotten a compile error or something looks wrong
you can refer
[here](https://gist.github.com/hobnob/c24f00936e91a7b7e5d644d19e4f1b32) for the
full file and [here](https://gist.github.com/hobnob/9a1e5a2d1039576948e3a904d915fc64)
for the diff.

What we've done here is fine for a single scene simple example, but when you
need to separate your code into more defined instances (such as main menus, or
levels) you will want to work with scenes and learn about
[Scene Management](https://indigoengine.io/docs/organisation/scene-management).

As you continue to use Indigo, you'll want to capture many events from the
player. A list of the types of events that Indigo supports out of the box can be
found [here](https://indigoengine.io/docs/gameloop/events).

Hopefully you've found all that useful. If you find that you're struggling, have
questions, or just want a chat about all things Indigo, you can find a very
friendly community hanging out on [Discord](https://discord.gg/b5CD47g).
