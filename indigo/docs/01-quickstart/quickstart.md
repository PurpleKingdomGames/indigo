# Quick start

### Using giter8 / sbt new

The quickest way to set up an Indigo game is to use our giter8 template, which provides a (rather opinionated) sbt based game shell to get you started.

[https://github.com/PurpleKingdomGames/indigo.g8](https://github.com/PurpleKingdomGames/indigo.g8)

### For sbt

Add the following to your `project/plugins.sbt` file:

```scala
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "@SCALAJS_VERSION@")
addSbtPlugin("io.indigoengine" % "sbt-indigo" % "@VERSION@")
```

Here is an example minimal `build.sbt` file:

```scala
import indigoplugin._

lazy val mygame =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo) // Enable the Scala.js and Indigo plugins
    .settings( // Standard SBT settings
      name := "mygame",
      version := "0.0.1",
      scalaVersion := "@SCALA_VERSION@",
      organization := "org.mygame"
    )
    .settings( // Indigo specific settings
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("My Game")
          .withWindowSize(720, 480),
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo" % "@VERSION@",
        "io.indigoengine" %%% "indigo-extras" % "@VERSION@",
        "io.indigoengine" %%% "indigo-json-circe" % "@VERSION@",
      )
    )
```

### For Mill

Example minimal `build.sc` file for your game:

```scala
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._

import $ivy.`io.indigoengine::mill-indigo:@VERSION@`, millindigo._

object mygame extends ScalaJSModule with MillIndigo {
  def scalaVersion   = "@SCALA_VERSION@"
  def scalaJSVersion = "@SCALAJS_VERSION@"

  val indigoOptions: IndigoOptions =
    IndigoOptions.defaults
      .withTitle("My Game")
      .withWindowSize(720, 480)

  val indigoGenerators: IndigoGenerators =
    IndigoGenerators.None

  def ivyDeps = Agg(
    ivy"io.indigoengine::indigo::@VERSION@",
    ivy"io.indigoengine::indigo-extras::@VERSION@",
    ivy"io.indigoengine::indigo-json-circe::@VERSION@"
  )

}
```

## Library dependencies

Each release of Indigo is published against the latest version of each of its few dependencies, that was available at the time.

As a rule of thumb, we encourage you to upgrade to the latest Scala 3, Scala.js, sbt, and Mill versions that you can at the point of project creation, and you should be fine.

## Building Indigo Games

Indigo games are completely normal [Scala.js](https://www.scala-js.org/) projects.

You can use either [Mill](https://mill-build.com/) (Mill @MILL_VERSION@  or above) or [SBT](https://www.scala-sbt.org/) (recommend sbt @SBT_VERSION@ or greater) to build your games, and for your convenience both Mill and SBT have associated plugins, `mill-indigo` and `sbt-indigo` respectively.

The plugins help you bootstrap your game during development, they marshal your assets and serve as a reference implementation for _one_ fairly basic way to embed your game into a web page or electron app.

The plugins let you build a simple web page via the `indigoBuild` task, or run an Electron app of your game with `indigoRun`.

Example output from a Mill indigo build of the [Snake example game](https://github.com/PurpleKingdomGames/indigo/tree/master/demos/snake), the SBT version is nearly identical:

```bash
> mill snake.buildGame
[46/48] snake.indigoBuild
dirPath: /Users/(...)/indigo/demos/snake/out/snake/indigoBuild/dest
Copying assets...
/Users/(...)/indigo/demos/snake/out/snake/indigoBuild/dest/index.html
[48/48] snake.buildGame
```

The second to last line is an absolute path to where your game is.

### Running your game locally (Electron Application)

If you use the plugin commands, `indigoRun` will automatically use the version of electron specified in your `IndigoOptions` config. By default, this installs the latest version locally.

From your command line:

```bash
mill mygame.fastLinkJS # Compiles and produces the JS file of your game.
mill mygame.indigoRun # First runs indigoBuild, then bundles it into an app and runs it.
```

Your game should appear on your desktop.

### Running your game locally (Web)

If your game is in active development, you might prefer to run it as a website that is slightly quicker to refresh and develop against.

First you need to build your game, as follows:

```bash
mill mygame.fastLinkJS # Compiles and produces the JS file of your game.
mill mygame.indigoBuild # Marshall scripts and assets together and link them to an HTML file.
```

Most modern browsers do not allow you to run local sites that load in assets and modules just by opening the HTML file in your browser. So if you use the Indigo build tool to produce a bootstrapped game, the quickest way to run it is to use [http-server](https://www.npmjs.com/package/http-server) as follows:

1. Install with `npm install -g http-server`. (For a global install, you can also use it locally if you prefer by omitting the `-g` and running using `npx`)
1. Navigate to the output directory shown after running the indigo plugin.
1. Run `http-server -c-1` - which means "serve this directory as a static site with no caching".
1. Go to [http://127.0.0.1:8080/](http://127.0.0.1:8080/) (or whatever `http-server` says in it output) and marvel at your creation..

## Scala.js "Fast" vs "Full" Optimisation

The examples below show you how to publish with both "fast" and "full" optimisation of your Scala.js project.

The difference is speed and size. As the name implies, the "fast" version compiles _very significantly_ faster than the "full" version, but even small projects will result in ~5Mb of JavaScript, where the "full" version will be in the region of ~500kb. The "full" version will likely be more performant at run time, for more information please refer to the official [Scala.js performance page](https://www.scala-js.org/doc/internals/performance.html).

Note that during development the fast version is perfectly acceptable. Your browser will chew through 5-10Mb of JavaScript or more with no problem at all, the performance difference is generally small enough not to be a big deal, and the compilation time reduction is definitely worth it.

## Mill Guide

### build.sc

Example minimal `build.sc` file for your game:

```scala
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._

import $ivy.`io.indigoengine::mill-indigo:@VERSION@`, millindigo._

object mygame extends ScalaJSModule with MillIndigo {
  def scalaVersion   = "@SCALA_VERSION@"
  def scalaJSVersion = "@SCALAJS_VERSION@"

  val indigoOptions: IndigoOptions =
    IndigoOptions.defaults
      .withTitle("My Game")
      .withWindowSize(720, 480)

  val indigoGenerators: IndigoGenerators =
    IndigoGenerators.None

  def ivyDeps = Agg(
    ivy"io.indigoengine::indigo::@VERSION@",
    ivy"io.indigoengine::indigo-extras::@VERSION@",
    ivy"io.indigoengine::indigo-json-circe::@VERSION@"
  )

}
```

### Running via Mill

Run the following:

1. `mill mygame.compile`
1. `mill mygame.fastOpt`
1. `mill mygame.indigoRun`

### Building via Mill

Run the following:

1. `mill mygame.compile`
1. `mill mygame.fastLinkJS`
1. `mill mygame.indigoBuild`

This will output your game and all the correctly referenced assets into `out/mygame/indigoBuild/`. Note that the module will give you a full path at the end of it's output.

To run as a web site, navigate to the folder, run `http-server -c-1`, and got to [http://127.0.0.1:8080/](http://127.0.0.1:8080/) in your browser of choice.

### Rolling it up into one command in Mill

You can also define the following in your `build.sc` file inside the `mygame` object:

```scala
  def buildGame() = T.command {
    T {
      compile()
      fastLinkJS()
      indigoBuild()() // Note the double parenthesis!
    }
  }

  def runGame() = T.command {
    T {
      compile()
      fastLinkJS()
      indigoRun()() // Note the double parenthesis!
    }
  }

  def buildGameFull() = T.command {
    T {
      compile()
      fullLinkJS()
      indigoBuildFull()() // Note the double parenthesis!
    }
  }

  def runGameFull() = T.command {
    T {
      compile()
      fullLinkJS()
      indigoRunFull()() // Note the double parenthesis!
    }
  }
```

Which allows you to run `mill mygame.buildGame` and `mill mygame.runGame` from the command line for the "fast" compile version.

## SBT Guide

### plugins.sbt

Add the following to your `project/plugins.sbt` file:

```scala
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "@SCALAJS_VERSION@")
addSbtPlugin("io.indigoengine" %% "sbt-indigo" % "@VERSION@") // Note the double %%
```

### build.sbt

Example minimal `build.sbt` file for the root of your project:

```scala
lazy val mygame =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin, SbtIndigo) // Enable the Scala.js and Indigo plugins
    .settings( // Standard SBT settings
      name := "mygame",
      version := "0.0.1",
      scalaVersion := "@SCALA_VERSION@",
      organization := "org.mygame"
    )
    .settings( // Indigo specific settings
      indigoOptions :=
        IndigoOptions.defaults
          .withTitle("My Game")
          .withWindowSize(720, 480),
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo" % "@VERSION@",
        "io.indigoengine" %%% "indigo-extras" % "@VERSION@",
        "io.indigoengine" %%% "indigo-json-circe" % "@VERSION@",
      )
    )
```

### Running via SBT

Run the following:

`sbt compile fastLinkJS indigoRun`

### Building via SBT

Run the following:

`sbt compile fastLinkJS indigoBuild`

This will output your game and all the correctly referenced assets into `target/indigoBuild/`. Note that the plugin will give you a full path at the end of it's output.

To run as a web site, navigate to the folder, run `http-server -c-1`, and go to [http://127.0.0.1:8080/](http://127.0.0.1:8080/) in your browser of choice.

### Rolling it up into one command in SBT

You can also define the following in your `build.sbt` file:

```scala
addCommandAlias("buildGame", ";compile;fastLinkJS;indigoBuild")
addCommandAlias("runGame", ";compile;fastLinkJS;indigoRun")
addCommandAlias("buildGameFull", ";compile;fullLinkJS;indigoBuildFull")
addCommandAlias("runGameFull", ";compile;fullLinkJS;indigoRunFull")
```

Which give you some convenient shortcuts to speed up development.
