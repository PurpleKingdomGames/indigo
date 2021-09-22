---
id: assets
title: Assets & Asset Loading
---

## Asset Types

There are three types of assets that Indigo understands how to load and make available:

1. Images - which can be JPEG or PNG format (others may work but are untested) - max size of 4096 x 4096.
2. Text - Any plain text format be it prose, yaml, json or xml. Indigo does not understand the text format, it just loads the text and makes it available.
3. Sound - Any browser supported audio format

When assets are loaded they are registered, and in the case of Sounds and Images, some amount of preparation is immediately done to ensure they're usable.

Text is different. Plain text isn't useful in of itself in the scene construction, and the only time you can access text assets is during Startup via the `AssetCollection`, this give you an opportunity to read/decode the contents and build them into your model somewhere.

### `AssetCollection`s

The `IndigoSandbox` entry point defines a setup function with the following signature:

```scala mdoc
import indigo._

def setup(assetCollection: AssetCollection, dice: Dice): Startup[Unit] = ???
```

The idea of this function is to give you an opportunity to do some light processing or preparation before your game starts (or re-starts), and this process can succeed or fail. The `Dice` provides a random element, and the `AssetCollection` gives you your _one and only_ opportunity to directly access asset data directly.

As previously mentioned, this is particularly useful for reading plain text files and parsing them.

The interesting methods on an `AssetCollection` are:

```scala mdoc
import org.scalajs.dom
import org.scalajs.dom.html

def exists(name: AssetName): Boolean = ???
def findImageDataByName(name: AssetName): Option[html.Image] = ???
def findTextDataByName(name: AssetName): Option[String] = ???
def findAudioDataByName(name: AssetName): Option[dom.AudioBuffer] = ???
def findFontDataByName(name: AssetName): Option[AssetName] = ???
```

> `AssetDataFormats.TextDataFormat` in this case is just a `String`. Although Indigo only works in the browser currently, architecturally it is somewhat organized for other platforms too.

## Asset Loading

Asset loading happens in either one or two phases, depending on whether you only need your assets before the game starts, or also want to load some of them later.

### Load ahead of game start

The simplest from of asset loading happens based on your initial game definition, for example:

```scala mdoc
val baseUrl = "./"

// If you're using the `IndigoSandbox` entry point
val assets: Set[AssetType] =
  Set(
    AssetType.Text(AssetName("map"), AssetPath(baseUrl + "assets/map.txt")),
    AssetType.Image(AssetName("font"), AssetPath(baseUrl + "assets/font.png")),
    AssetType.Image(AssetName("snake"), AssetPath(baseUrl + "assets/snake.png")),
    AssetType.Audio(AssetName("intro"), AssetPath(baseUrl + "assets/intro.mp3")),
    AssetType.Audio(AssetName("point"), AssetPath(baseUrl + "assets/point.mp3")),
    AssetType.Audio(AssetName("lose"), AssetPath(baseUrl + "assets/lose.mp3"))
  )

// Or if you're using the IndigoDemo` or `IndigoGame` entry points
def boot(flags: Map[String, String]): BootResult[Unit] =
  BootResult.noData(GameConfig.default)
    .withAssets(
      Set(
        AssetType.Text(AssetName("map"), AssetPath(baseUrl + "assets/map.txt")),
        AssetType.Image(AssetName("font"), AssetPath(baseUrl + "assets/font.png")),
        AssetType.Image(AssetName("snake"), AssetPath(baseUrl + "assets/snake.png")),
        AssetType.Audio(AssetName("intro"), AssetPath(baseUrl + "assets/intro.mp3")),
        AssetType.Audio(AssetName("point"), AssetPath(baseUrl + "assets/point.mp3")),
        AssetType.Audio(AssetName("lose"), AssetPath(baseUrl + "assets/lose.mp3"))
      )
    )
```

> The important thing to know here is that whichever entry point style you're using, all of those assets will be forced to load completely before your game will show anything on the screen _at all_. If they can't be loaded, the game will halt.

For demos and tests or local development with no network latency, requiring all the assets to be primed and ready before your game starts is no big deal, even for substantial amounts of data. It is even advantageous, since there is a cost to loading images later, such as re-generating texture atlases.

### Dynamic asset loading

When you're dealing with large amounts of asset data, you may not be happy to leave your player staring at a blank screen while Indigo loads and prepares everything. You might prefer to show them a loading screen, sometimes called a pre-loader.

The basic flow we want to achieve is:

1. Set up a scene that will be your loading screen.
1. Use the standard asset loading mechanism to load only the assets you need to be able to draw your loading screen.
1. Launch your loading screen.
1. Kick off a background load of the remaining assets.
1. As the assets arrive, update a progress bar / animation on the loading screen.
1. Once they all arrive and have been processed, proceed to the next scene of the game - perhaps a menu screen.

> Note that the dynamic asset loading approach can be used to add assets _at any time_, not just at the beginning as the above flow suggests, and will make use of the browser cache to avoid re-delivery.

You can either use the basic inbuilt events to load your assets, and manage the process yourself, or you can use the provided `AssetBundleLoader` `SubSystem` to do the work for you. There is nothing special about the `AssetBundleLoader`, it uses the same basic indigo asset events you have access to, it just abstracts over the problem to give you a friendlier experience. The remainder of this article assumes you are using the subsystem.

[There is a small example of the `AssetBundleLoader` running in the main indigo repo.](https://github.com/PurpleKingdomGames/indigo/blob/master/examples/assetLoading/src/main/scala/com/example/assetloading/AssetLoadingExample.scala)

### Using the Asset Bundler Loader

> Important! One advantage of loading everything up front is that you, the game developer, will find out _immediately_ whether or not you have an asset that can't load for some reason or other. If however, as an example, you defer an asset load until just before the last level, and don't give yourself a way to jump to the last level for testing - you won't know you have a bad asset until that point in your game's testing cycle - which could be very long indeed! Load everything as early as possible to avoid disappointment.

To kick off an asset bundle load, you need to fire off an `AssetBundleLoaderEvent.Load` event by attaching it to an `Outcome` or `SceneUpdateFragment`. In the example linked to above, we use a button (I've simplified here slightly):

```scala mdoc
import indigoextras.ui._
import indigoextras.subsystems._

val otherAssetsToLoad: Set[AssetType] =
  Set(
    AssetType.Text(AssetName("map"), AssetPath(baseUrl + "assets/map.txt")),
    AssetType.Image(AssetName("font"), AssetPath(baseUrl + "assets/font.png")),
    AssetType.Image(AssetName("snake"), AssetPath(baseUrl + "assets/snake.png")),
    AssetType.Audio(AssetName("intro"), AssetPath(baseUrl + "assets/intro.mp3")),
    AssetType.Audio(AssetName("point"), AssetPath(baseUrl + "assets/point.mp3")),
    AssetType.Audio(AssetName("lose"), AssetPath(baseUrl + "assets/lose.mp3"))
  )

def buttonAssets: ButtonAssets = 
  ButtonAssets(
    up = Graphic(50, 50, Material.Bitmap(AssetName("button up"))),
    over = Graphic(50, 50, Material.Bitmap(AssetName("button over"))),
    down = Graphic(50, 50, Material.Bitmap(AssetName("button down")))
  )

Button(
  buttonAssets = buttonAssets,
  bounds = Rectangle(10, 10, 16, 16),
  depth = Depth(2)
).withUpActions {
  println("Start loading assets...")
  List(AssetBundleLoaderEvent.Load(BindingKey("bundle 1"), otherAssetsToLoad))
}
```

As you can see, the asset bundle is just another `Set[AssetType]` like we'd use during the pre-start asset load. We're also required to provide a `BindingKey` instance so that we can track which asset bundle has loaded - or not as the case may be.

We can then track the progress of our bundle load by pattern matching the relevant events:

```scala mdoc
def updateModel(context: FrameContext[Unit], model: MyGameModel): GlobalEvent => Outcome[MyGameModel] = {
  case AssetBundleLoaderEvent.Started(key) =>
    println("Load started! " + key.toString())
    Outcome(model)

  case AssetBundleLoaderEvent.LoadProgress(key, percent, completed, total) =>
    println(s"In progress...: ${key.toString()} - ${percent.toString()}%, ${completed.toString()} of ${total.toString()}")
    Outcome(model)

  case AssetBundleLoaderEvent.Success(key) =>
    println("Got it! " + key.toString())
    Outcome(model.copy(loaded = true))
      .addGlobalEvents(PlaySound(AssetName("sfx"), Volume.Max)) // Make use of a freshly loaded asset.

  case AssetBundleLoaderEvent.Failure(key, message) =>
    println("Lost it... " + key.toString() + ", message: " + message)
    Outcome(model)

  case _ =>
    Outcome(model)
}
```

> Note that the percent loaded is based on items received not data transfered, i.e. 5 out of 10 items loaded is 50%, 5.5 items out of 10 is still 50%!

#### Stating the obvious

You can't use an asset before you've loaded it.

The eagle eyed among you may have noticed that the super simple model above has a loaded flag in it's definition, here is the whole thing:

```scala mdoc
final case class MyGameModel(loaded: Boolean)

val model = MyGameModel(true)
```

The loaded flag above is a crude way of us saying "Ok, the assets are ready for use now!", so that once set to true, we can start drawing with them, again a minimal example could be:

```scala mdoc
SceneUpdateFragment(
  if (model.loaded) {
    List(
      Graphic(Rectangle(0, 0, 64, 64), 1, Material.Bitmap(AssetName("junction box")))
        .moveTo(30, 30)
    )
  } else Nil
)
```

#### Using dynamically loaded `Text` assets

As previously discussed, images and sounds can be used directly but text can only be accessed during startup. As such, a bundle load triggers a full engine restart (which in our admittedly small tests, isn't noticeable) with the current game state preserved. This means that you can check for the existence of a text item (and indeed any asset) at start up and use it if and when it becomes available.
