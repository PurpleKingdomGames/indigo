---
id: hello-indigo
title: Hello, Indigo!
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

This is a quick start tutorial to help you build something with Indigo.

There are [Mill](https://github.com/PurpleKingdomGames/hello-indigo) and [SBT](https://github.com/PurpleKingdomGames/hello-indigo-sbt) repositories that go with this guide, please note that you can follow the games original development progression by looking through the commits on the _Mill_ version.

## Choose your game API style

Indigo comes with three game templates. Called entry points, they are just traits you extend that help give your game some shape, and can be described as:

1. `IndigoSandbox` - The smallest API interface, great for trying things out but doesn't scale as well, and is missing some functionality.
2. `IndigoDemo` - Technically gives you access to everything, but doesn't provide Scene management (you'd have to roll your own).
3. `IndigoGame` - Like `IndigoDemo`, but with Scene management built it.

> You can also write your own entry point, take a look at the code for `IndigoSandbox` in the repo.

In this guide, we'll be using `IndigoSandbox` for brevity and our "game" will be called `helloindigo`.

> Reminder: The sandbox is limited in what it can do, and it geared towards briefly trying things out without the clutter of the two larger interfaces.

## "Hello, Indigo!"

We'll skip over the initial project set up and assume that you followed the [set up guide](setup-and-configuration.md), or have [checked out the repo](https://github.com/PurpleKingdomGames/hello-indigo) for reference.

Here is our starting point:

```scala mdoc
import indigo._
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object HelloIndigo extends IndigoSandbox[Unit, Unit] {

  val config: GameConfig =
    GameConfig.default

  val animations: Set[Animation] =
    Set()

  val assets: Set[AssetType] =
    Set()

  val fonts: Set[FontInfo] =
    Set()

  val shaders: Set[Shader] =
    Set()

  def setup(
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(
      context: FrameContext[Unit],
      model: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  def present(
      context: FrameContext[Unit],
      model: Unit
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
    )

}
```

A lot of this is self explanatory hopefully, but let's go through a couple of the more note worthy points.

The `indigo._` import is optional, but conveniently brings in all of the basic syntax so that you don't need to worry about finding things.

Next up are the only two lines of Scala.js you have to know:

```scala mdoc
import scala.scalajs.js.annotation.JSExportTopLevel
@JSExportTopLevel("IndigoGame")
object HelloIndigo extends IndigoSandbox[Unit, Unit]
```

Indigo games are Scala.js projects. We've worked hard to make Indigo feel as much like a normal Scala project as possible, however, we do need a hook for the page. If you're using the standard Indigo Mill or SBT plugins, you ***must name your game "IndigoGame" or it won't work***. Once you move to your own page embed you can call it whatever you like!

`IndigoSandbox` takes two type parameters that define your start up data type, and the type of your model. Later on we'll introduce a real model, but for now we're just using `Unit` to say "I'm not using these".

```scala mdoc
object HelloIndigo extends IndigoSandbox[Unit, Unit]
```

The other entry points mentioned earlier require you to declare more type parameters to cover: Boot data, start up data, model, and view model.

Everything else is just filling in the blanks to make it compile.

One small thing to note is that most types in Indigo try to provide sensible defaults such as `GameConfig.default` and `SceneUpdateFragment.empty`, so it's always worth checking the companion object.

### Running the demo - a blank screen!

We're going to follow the Mill version of the project below, but the SBT version is almost identical, substituting `sbt runGame` in place of `mill helloindigo.runGame`.

So assuming you have followed the [set up guide](setup-and-configuration.md), to run the demo enter the following from your command line:

```bash
mill helloindigo.runGame
```

## Putting something on the screen

On the assumption that you have the same assets as the demo repo (note that you can change the asset source folder in the build settings!):

Replace:

```scala mdoc
val assets: Set[AssetType] =
  Set()
```

with:

```scala mdoc
val assetName = AssetName("dots")

val assets: Set[indigo.AssetType] = Set(
  AssetType.Image(assetName, AssetPath("assets/dots.png"))
)
```

This tells indigo to load your image asset and file it away for future reference. To recall it when needed, you give it an `AssetName` as an identifier.

Next replace:

```scala mdoc
SceneUpdateFragment.empty
```

with:

```scala mdoc
SceneUpdateFragment(
  Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
)
```

Note the use of `assetName` again to identify which image you want to use.

`Graphic` is a member of the `SceneGraphNode` types, which also include things like `Sprite`, `Text`, `Group`, and `Clone`. Please note that most of these have a range of constructors to try and make using them easier, and all of them follow a fluent API design to modify their parameters.

Run the demo again and you should see a graphic in the top left corner at position `(0,0)`!

It's quite small though... so come back to your code and replace `GameConfig.default` with:

```scala mdoc
val magnification = 3

val config: indigo.GameConfig =
  GameConfig.default.withMagnification(magnification)
```

Indigo is built for pixel art, and will automatically scale up not just your graphics, but also things like mouse positions. You just have to build your game as if the game was running at a 1:1 pixel ratio and Indigo will do the rest.

## What else can `Graphic`s do?

`Graphic`s are relatively cheap on-screen objects, in terms of performance, but their unique party trick is being able to crop their contents. Update this:

```scala mdoc
SceneUpdateFragment(
  Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
)
```

to:

```scala mdoc
SceneUpdateFragment(
  Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName)),
  Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
    .withCrop(Rectangle(16, 16, 16, 16))
    .withRef(8, 8)
    .moveTo(config.viewport.giveDimensions(magnification).center)
)
```

Run it again and you should now have just the yellow circle right in the middle of the screen. The image is 32x32, and we've cropped down to the bottom right corner which is at location 16x16 and is 16x16 pixels in size. We've then moved the "reference point" which is the point Indigo uses to position, scale, and rotate things to being in the middle of the new graphic, i.e. 8x8. Finally we moved it to the middle of the screen. Normally the top left of the image would now be at the screen's center, but because we moved the reference point, the graphic is placed evenly over the mid point.

> An important but subtle thing has happened here, that if you're used to conventional game engines might surprise you. Normally to add an entity to the screen you have to explicitly add it (perhaps after a callback or event), and then later, explicitly remove or delete it by location in the tree or by id. This is because usually scene graphs are mutable trees of some kind, and each leaf carries state and so on. In Indigo, the view is stateless and simply draws whatever is currently returned by the `present` function. So "deleting" something from the view is just the same as omitting it from the returned `SceneUpdateFragment` on this frame.

## Let's make the dot move

We're going to make the dot move using a `Signal`. Signals are powerful but a bit complicated, so we're going to use it here just to show you them in action and get rid of it again in the next step.

Replace:

```scala mdoc
Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
  .withCrop(Rectangle(16, 16, 16, 16))
  .withRef(8, 8)
  .moveTo(config.viewport.giveDimensions(magnification).center)
```

with:

```scala mdoc
Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
  .withCrop(Rectangle(16, 16, 16, 16))
  .withRef(8, 8)
  .moveTo(
    Signal
      .Orbit(config.viewport.giveDimensions(magnification).center, 30)
      .map(_.toPoint)
      .at(context.gameTime.running)
  )
```

Signals are just a function from `time: Seconds => A`. This code uses an inbuilt Signal called Orbit that rotates around a point at a fixed distance based on the current time.

```text
Question: Orbit looks a bit simple, what if you wanted it to rotate slower or faster?

Answer: Slow down or speed up time! (There is an `affectTime` function for this.)
```

Stepping through the code, we request an Orbit signal that rotates around the center of the screen at a distance of 30 pixels. The signal produces a `Vector2` so we have to convert that to a `Point` because Indigo insists that everything is drawn on a whole pixel. Finally we tell the signal what time it is, and it produces the value we want: A point to move our dot to.

## Time matters

Before we move on, if you're new to game development, it's worth noting the importance of that last bit. All movement in your real game should be based on time, one way or another.

Consider how we might move something along the x-axis:

```scala mdoc
graphic.moveBy(10, 0)
```

Every time that code is run, it will increment the graphic's x position by 10. Great! Our games runs at 60 frames per second (FPS) so we're going to move at a velocity of 600 pixels per second. Or are we?

The problem is the frame processing times vary, meaning that at _best_ you'll get 60 FPS / 600 pixels movement, but more likely your frame rate will fluctuate, peaking at 60 and occasionally dropping lower, maybe for a minor GC pause, maybe because you suddenly had a massive bit of processing to do for your game or just started drawing more stuff on the screen.

Either way the result is that your x-axis movement is no longer smooth.

The solution is to do this:

```scala mdoc
graphic.moveBy(600 * time delta in seconds, 0)
```

That is, we say that we want a velocity of 600 pixels per second, but multiply that 600 by the fraction of a second since the last frame update. At 60 FPS, `600 * 0.01666 = 9.996` i.e near as makes no odds the 10 pixel movement we wanted, while at a dip to 55 FPS we get `600 * 0.01818 = 10.908`, meaning that you move a little further to make up for lost time.

> This is known as ***frame independent movement***.

## It isn't a game, if you can't play with it.

Time to turn our animation into something you can fiddle with, if not exactly "play".

Please note that you can see the [real diff here](https://github.com/PurpleKingdomGames/hello-indigo/commit/ca6f16c09c0d1be29da960dc26a9b7b5e8198bab).

### Remembering things

So far, we've just been drawing things, and we haven't needed to remember anything in order to decide what to draw or where it should go on the screen, and that's been fine.

As soon as you need to start remembering things, you need to use a `Model` or a `ViewModel`.

The idea of the `Model` is that it should be about storing the _abstract version of your game_ meaning that it has no concept of pixels or screen dimensions of or anything like that. The `Model` should normally be decoupled from the view and all view logic (other than the view reading the model). Normally a save game would be generated from the model only.

Sometimes though, you need to remember things in screen space, concrete details about positions and animation states, and in those cases you would use a `ViewModel`. Conversely, a `ViewModel` should not hold any data you wouldn't mind losing, i.e. presentation data only, no game data.

**One limitation of the Sandbox is that in the spirit of minimalism, it has no `ViewModel`! So we're going to have to immediately break our rule a bit.**

### Adding interaction

What we're going to do is make it so that the screen starts empty (apart from our graphic in the corner), and when you click the screen, a yellow dot is put into orbit around the center of the screen, at the distance you clicked.

To do that we're going to need a simple model, so let us define some case classes to hold our data.

Add this to the bottom of your file:

```scala mdoc
case class Model(center: Point, dots: List[Dot]) {
  def addDot(dot: Dot): Model =
    this.copy(dots = dot :: dots)

  def update(timeDelta: Seconds): Model =
    this.copy(dots = dots.map(_.update(timeDelta)))
}
object Model {
  def initial(center: Point): Model = Model(center, Nil)
}

case class Dot(orbitDistance: Int, angle: Radians) {
  def update(timeDelta: Seconds): Dot =
    this.copy(angle = angle + Radians.fromSeconds(timeDelta))
}
```

Notes:

1. We've got an `initial` model definition - got to start somewhere!
2. For convenience we've got an `addDot` method on the model
3. We've also got a simple `update` function that cascades through our model objects propagating the time delta we talked about earlier.

Angles are measured in Radians, if you're not used to Radians then the `Radian` class has a `fromDegrees` function ...but you're a game developer now! Learn about radians!

Crash course:

```text
360 degress = TAU
TAU = 2 * PI
PI = 3.1415926536

So, 360 degrees in radians is 6.2831853072.

A lot of game engines still talk about radians in terms of PI, but it's far more convenient to use TAU, and the Radians class has TAU based constants (as well as PI based ones).
```

For Maths magic, you can't do better than [Freya Holmér's twitter feed](https://twitter.com/FreyaHolmer), [here](https://twitter.com/FreyaHolmer/status/1202648662049996801) and [especially here](https://twitter.com/FreyaHolmer/status/1173752820954214400?s=20) is how Radians work, for example.

I digress: Let's set up our model.

First you need to tell Indigo what class you're using for your Model, like so:

```scala mdoc
object HelloIndigo extends IndigoSandbox[Unit, Unit]
```

becomes:

```scala mdoc
object HelloIndigo extends IndigoSandbox[Unit, Model]
```

Then we need to give Indigo the empty or first version of our model.

Replace:

```scala mdoc
def initialModel(startupData: Unit): Outcome[Unit] =
  Outcome(())
```

with:

```scala mdoc
def initialModel(startupData: Unit): Outcome[Model] =
  Outcome(
    Model.initial(
      config.viewport.giveDimensions(magnification).center
    )
  )
```

And then we need to update it, replace:

```scala mdoc
def updateModel(
    context: FrameContext,
    model: Unit
): GlobalEvent => Outcome[Unit] =
  _ => Outcome(())
```

with

```scala mdoc
def updateModel(
    context: FrameContext,
    model: Model
): GlobalEvent => Outcome[Model] = {
  case MouseEvent.Click(x, y) =>
    val adjustedPosition = Point(x, y) - model.center

    Outcome(
      model.addDot(
        Dot(
          Point.distanceBetween(model.center, Point(x, y)).toInt,
          Radians(
            Math.atan2(
              adjustedPosition.x.toDouble,
              adjustedPosition.y.toDouble
            )
          )
        )
      )
    )

  case FrameTick =>
    Outcome(model.update(context.delta))

  case _ =>
    Outcome(model)
}
```

The model update function is just a function that has been partially applied with the context of this frame, and then a big pattern match on the event types.

>`GlobalEvent` is a trait used to tag things as events. The largest source of errors / surprises in Indigo - in the authors experience so far - is from the fact that we can't enforce exhaustively checks on these events. In other words, you forgot to catch an event case somewhere.

In this case, we're interested in two events. A `MouseEvent.Click(x, y)` so that we can add a new dot, and a `FrameTick`. FrameTick is a bit special because it always happens last... and it always happens!

When a mouse click is noticed, we call our `addDot` method with a new Dot, providing the orbital distance and the angle from the center of the screen to the point where we clicked the mouse using `Math.atan2(y, x)`.

Keep in mind that multiple events can and often do happen between frame ticks, which should only lead to model updates related to each specific event. In particular, changes that are intended to occur at a constant rate, like motion, should only be applied on frame ticks. Otherwise funny time-dilating side effects can occur.

Notice that everything is wrapped in an `Outcome`. An `Outcome[A]` is a Monad that holds a new state `A` and can also capture any events that are the ...outcome... of processing part of a frame. Outcome's can also be used to handle, recover from and report on errors. `Outcome`s can be composed together in lots of useful ways, much like the standard `Option` type.

Finally we need to draw something, replace:

```scala mdoc
    SceneUpdateFragment(
      Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName)),
      Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
        .withCrop(Rectangle(16, 16, 16, 16))
        .withRef(8, 8)
        .moveTo(config.viewport.giveDimensions(magnification).center)
    )
```

with:

```scala mdoc
SceneUpdateFragment(
  Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName)) :: drawDots(model.center, model.dots)
)

def drawDots(
    center: Point,
    dots: List[Dot]
): List[Graphic[_]] =
  dots.map { dot =>
    val position = Point(
      x = (Math.sin(dot.angle.toDouble) * dot.orbitDistance + center.x).toInt,
      y = (Math.cos(dot.angle.toDouble) * dot.orbitDistance + center.y).toInt
    )

    Graphic(Rectangle(0, 0, 32, 32), 1, Material.Bitmap(assetName))
      .withCrop(Rectangle(16, 16, 16, 16))
      .withRef(8, 8)
      .moveTo(position)
  }
```

Run it, and hopefully clicking on the screen will add yellow dots!

If it doesn't work, remember you can always [compare with the repo](https://github.com/PurpleKingdomGames/hello-indigo)!

Hopefully this has given you a little taste of how Indigo works. Next you could try:

1. Modifying this demo to choose a random coloured circle using the `Dice` instance in the `FrameContext`.
2. Sticking with the sandbox and trying out other features - perhaps take a look at the [examples](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/examples) for ideas?
3. Switch to a different entry point (`IndigoDemo` or `IndigoGame`), and see if you can modify the example above to fit. Could you make use of the view model? You'd probably only need one scene, but how would it be set up?
4. Try porting the code above to a `SubSystem` - what will the relationship between the main game and the subsystem be?

We hope you enjoyed creating something - however simple - that was visual, interactive and fun in Scala, and we look forward to seeing what you create next!
