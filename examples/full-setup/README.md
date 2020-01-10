# Basic Indigo Game Setup

Once you get past a couple of things you just have to "know", building a game
in indigo is mostly just about following the instructions.

## The bits you just gotta "know"
An indigo game is a completely ordinary Scala project. On purpose. No magic here.

Indigo isn't even a framework, it's a library. You have to initialise your project
and then start indigo when you're ready.

### Scala.js and SbtIndigo
Once you've set up your project and added the indigo dependency, you'll need to
create an entry point and annotate it so that Scala.js knows what the JS entry
point should be.

```
import scala.scalajs.js.annotation.JSExportTopLevel

object BasicSetup {

  @JSExportTopLevel("Example.main")
  def main(args: Array[String]): Unit = {
    //
  }
  
}
```

Along with that, you'll need to tell Scala.js to kick off your project immediately
on load by add this line to your build.sbt configuration:

```
scalaJSUseMainModuleInitializer := true
```

There are couple of other fairly self explanatory sbt settings:

```
showCursor := true

title := "Basic Setup" // HTML page title

gameAssetsDirectory := "assets" // Relative path to the location of the assets you want
``` 

You could now do a compile by doing `fastOptJS` followed by `indigoBuild`

`indigoBuild` simply created the correct directory structure in your target directory
and copies all the relevant things into the right places so that your game works.

Nothing complicated.

The last thing you *need* to know is this import:

`import com.purplekingdomgames.indigo.Indigo`

### All downhill from here
The basic indigo shell is a simple builder pattern designed to be fairly hard to
mess up. After you type `Indigo.` your IDE should just tell you want to do - keep
 filling in the blanks - until you get this (I've left the implementations as `???`):

```
package ingidoexamples.basic

import com.purplekingdomgames.indigo.Indigo
import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneGraphUpdate
import com.purplekingdomgames.shared.{AssetType, GameConfig}

import scala.scalajs.js.annotation.JSExportTopLevel

object BasicSetup {

  val config: GameConfig = ???
  val assets: Set[AssetType] = ???
  val setup: AssetCollection => Startup[MyStartUpError, MyStartupData] = ???
  val initialModel: MyStartupData => MyGameModel = ???
  val updateModel: (GameTime, MyGameModel) => GameEvent => MyGameModel = ???
  val renderer: (GameTime, MyGameModel, InputSignals) => SceneGraphUpdate = ???
  
  @JSExportTopLevel("Example.main")
  def main(args: Array[String]): Unit =
    Indigo.game // <-- just start typing this bit and the rest will emerge...
      .withConfig(config)
      .withAssets(assets)
      .startUpGameWith(setup)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .drawUsing(renderer)
      .start()
  
}

// Start up types - can be anything, but you must supply a way to render the
// error cases
case class MyStartupData()
case class MyStartUpError(errors: List[String])
object MyStartUpError {
  implicit val toReportable: ToReportable[MyStartUpError] =
    ToReportable.createToReportable(e => e.errors.mkString("\n"))
}

// Your game model is anything you like!
case class MyGameModel()
```

Note that the start up classes and game model are entirely up to you.

#### One Gotcha: Async config and assets
When filling out the above you might notice you get the option to use async assets
and config. You probably don't need this, but in theory these two are executed ahead of
everything else so that you can dynamically load different config or assets.

### Filling in the blanks

#### Normal game config example

```
  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(550, 400),
      frameRate = 60,
      clearColor = ClearColor.Black,
      magnification = 1
    )
```

Indigo was designed for pixel art games, so magnification scales up using a 
nearest neighbour algorithm to preserve your lovely pixel art graphics.

In case you're wondering though, you just pretend the scale is 1:1.

#### Advanced game config example
If you want to know what you're game performance is like or where it's spending
most of it's time, you can flip these switches on and reports will be dumped into
your browsers console.

Flipping `disableSkipModelUpdates` and `disableSkipViewUpdates` is not a 
good idea. Indigo works to recover performance by skipping unnecessary work when
it really has to by dropping frames.

```
  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(550, 400),
      frameRate = 60,
      clearColor = ClearColor.Black,
      magnification = 1,
      advanced = AdvancedGameConfig( // these are the defaults
        recordMetrics = false,
        logMetricsReportIntervalMs = 10000,
        disableSkipModelUpdates = false,
        disableSkipViewUpdates = false
      )
    )
```

#### Assets
There are two type of asset (will be three when I add audio...): Images and Text.

```
// note the "assets" in the path is where it *will* load from, which is always a dir called assets
val assets: Set[AssetType] =
    Set(ImageAsset("my image", "assets/graphics.png"))
```

Images are used in the scene rendering and referred to be the name `"my image"`.

Text assets are available to use as you like, just as deserialising into a case class.

#### Setup
The setup function is a chance for you to make use of your assets before...

```
  // does nothing
  val setup: AssetCollection => Startup[MyStartUpError, MyStartupData] =
    _ => MyStartupData()
```


***TODO: finish this and the rest of the setup functions>>***
