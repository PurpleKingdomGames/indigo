---
id: boot-and-start-up
title: Boot & Start Up
---

> Please note that the terms "start up" and "setup" are used interchangeably here. "Startup" is the name of the data type, while "setup" is the name of the method. This naming should probably be revisited...

In order to get your game up and running, it needs to go through an initialization sequence comprised of two stages:

1. Boot - which only ever happens once;
2. Start up - which occurs on first run, and then whenever new assets are loaded dynamically.

A lot of what this process relates to is the loading and processing of assets, so it's worth [reading up on that too](platform/assets.md).

## Booting your game

During your game's initial boot, you must specify all the things your game needs to get up and running.

At minimum, this is a `GameConfig` definition, but can also include assets, subsystems, animations, fonts and some custom data that you'd like to make available to the rest of the game.

### Sandbox games

Sandbox games (that implement `IndigoSandbox`) have a different boot sequence than other games, or rather they don't have a boot sequence. The main facilities of the boot process have been broken out onto the trait for you to populate. This is an intentional limitation that keeps the Sandbox simple.

### Games & Demos

`IndigoGame`'s and `IndigoDemo`s have a boot method such as this one, where "`BootData`" is some user defined type:

```scala mdoc
import indigo._

def boot(flags: Map[String, String]): Outcome[BootResult[BootData]]
```

#### Flags

> Flags are an opportunity to change your games settings based on the environment in which it is being run.

Running a game though one of the provided run methods, via the `indigoBuild` or `indigoRun` commands, is fine during general development, but at some point you're going to want to run your game in a real environment. Maybe it's going onto a game portal site, maybe its your own web site, maybe it's a desktop game. Whatever it is, those platforms come with practical problems to over come.

> By default Indigo will provide a width and height flag in the standard integration code (that you can choose to use or not) so that you can start your game at the size of the window.

Let's look at the [Snake game on our website as a simple example](https://indigoengine.io/snake.html). When running locally, the assets are marshaled into a nice consistent folder called `assets` in the same directory as our `index.html` page. However, on the real site the assets are served statically from a different location! How do we reconcile these two worlds?

Well we use a flag, like this:

```scala mdoc
  def boot(flags: Map[String, String]): Outcome[BootResult[GameViewport]] = {
    val assetPath: String =
      flags.getOrElse("baseUrl", "")

    ???
  }
```

As long as the games assets are always in the same folder name, we can now just pre-pend the provided `assetPath` to change the location of that folder.

Flags are just a JavaScript object of key value pairs that must be `String`s (like command line arguments), which can be injected into the game when it is embedded on the page, like this:

```javascript
var flags = {
  "baseUrl": "/my/websites/assets/folder/path/"
};

IndigoGame.launch(flags);
```

Flags can represent anything you like. In the Snake example we are providing the url to the assets folder, but you could do browser detection in the page and use your findings to instruct Indigo to specifically use WebGL 1.0 or 2.0, or to change the magnification setting, or starting view port size, or background color, or any number of other things.

The main limitation on flags is that they are typed to `Map[String, String]`, which is bothersome if you're trying to supply a number for instance. Perhaps the best way to use more sophisticated data at start up would be to supply JSON by setting a data flag, e.g. `{ data = '{width: 10, height: 10}' }`, and then pulling out the data flag at boot time and parsing the JSON string.

#### BootResult[_]

In a simple game, all of your animations, fonts, subsystems, shaders, and assets can be declared during the boot stage. For more complex games, such as ones that have a pre-loader, you should only include the elements you need for the preloader scene here.

> Note: You can add more animations, fonts, and assets at a later stage, but subsystems must be declared upfront or inside a `Scene` definition.

Optionally the boot sequence can result in a value, particularly since the boot sequence has access to the initial flags. For example you might return:

```scala mdoc
final case class BootData(runFullScreen: Boolean)
```

## Start up / Setup

After booting up, you hit the start up function (called `setup`...). Why are boot and start up separate functions? ..and what is start up for?

If "boot" is for marshaling your foundation game settings (bootstrapping), then start up is for pre-processing data and assets. Since assets can be loaded on first run, but also added dynamically during the game (and you might want to do something with them such as parse a text file), the start up or setup operation may be invoked more than once. For example:

1. During the first run you load a JSON definition of a animation and it's sprite sheet, which are processed during start up to create a `Sprite` and `Animation` you can render.
2. You use that to present a loading screen (preloader) while the rest of the games assets are loaded.
3. Once the other assets are loaded, `setup` is called again and you can now process the new / remaining asset data for future use.

The setup function signature looks like this:

```scala mdoc
final case class StartUpData(debugMode: Boolean) // an example custom start up data type

def setup(bootData: BootData, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartUpData]] = ???
```

> Important! The `StartUpData` type corresponds to one of the type parameters in `IndigoSandbox`, `IndigoDemo`, and `IndigoGame`.

### Special note on plain text assets

Unlike image and sound assets which are referenced directly in the presentation logic of your game. Plain text assets are only available for use during start up. The idea is that text is most likely not plain text, but actually string encoded data like JSON or perhaps a CSV. You can get hold of their data via the `AssetCollection` during start up / setup.

### The Startup Data Type

If your setup function has succeeded:

```scala mdoc
// This is a made up user defined type that represents some result of the Startup process
final case class MyStartUpData(maxParticles: Int)
case object MyGameEvent extends GlobalEvent

def animation: Animation = ???
def fontInfo: FontInfo = ???

val startup = 
  Startup.Success(MyStartUpData(256))
    .addAnimations(animation) // Optional: New animations you created during startup
    .additionalFonts(fontInfo) // Optional: Font data you created during start up
    .startUpEvents(MyGameEvent) // Optional: Any `GlobalEvent`s you would like to emit
```

If you don't need say anything other than "success", you can just say `Startup.Success(())`.

Should your setup function has fail, you should report errors like this:

```scala mdoc
Startup.Failure(
  "error message 1",
  "error message 2",
  "error message 3"
)
```

If your set up function fails, the game will halt and the errors will be written to the console. The assumption is that a failure is unrecoverable.
