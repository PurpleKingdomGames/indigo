# IndigoJS

This project does two things:

1. Provides a means to export a controlled JavaScript friendly version of Indigo;
1. Allows generation of docs for IndigoJS APIs.

## VSCode user?

For your convenience, you can start `sbt` in the project root and when you get to the SBT shell you can just type `code` to launch VSCode int he right place.

## Clean setup

Note that I'm doing this on a mac! Any other platform, all bets are off! It should still basically work, you may just need to massage a few things here and there.

This is for when there is a new local version of indigo you need to build against. Really important to do step one since SBT, Metals, and Bloop cache local snapshots to speed things up...

1. Open `~/.ivy2/local/` and remove the indigo folder, if you have one.
1. In your terminal, go to `(..)/<indigo git root>/indigo`
1. `bash localpublish.sh` - this will take a while...*
1. Navigate to `(..)/<indigo git root>/jsapi`
1. Type `sbt` to enter the shell, then:
   1. `clean`
   1. `update`
   1. `compile`

You should be ready.

* A note on the indigo build. It's a bit chicken and egg because the main Indigo build needs the indigo sbt plugin, but all the code for the sbt-indigo plugin lives with the indigo build. So! Since SBT is recursive, the plugin is actually built using a "meta" build inside the `project` folder. The bash file that does the build knows this, but if you're having trouble you can try and do what it does, manually. Don't be scared, it's only one command called in the right directory.

## General information

The `indigojs` project contains all the "Delegate" classes that ..delegate to Indigo's real class types. We then export the delegate versions to JavaScript using [Scala.js annotations](https://www.scala-js.org/doc/interoperability/export-to-javascript.html). This gives us lots of control over the JS API at the cost of minor additional memory allocation at runtime (booooo ...but I don't have any better ideas at the moment). I've used a few different methods for doing this, examples of the main two patterns I've landed on are:

1. DiceDelegate - Dice are more or less a read only thing, so it's a proper delegate pointing to an internal instance.
2. PointDelegate - Point's can be constructed, so we give you a construction interface and use a "scala only" `toInternal` method that constructs does what it says on the tin.

The interface we're exporting is a sort of lean, cut down version of the full API, and we can be a bit creative about naming and so on because it's all just a facade. I'm not skimping, some of the APIs in the Scala version of Indigo make absolutely no sense in JavaScript - what we're really doing is converting idomatic Scala APIs into idomatic JS APIs.

## Generating the JS Interface

1. Type `sbt` to enter the shell, then:
   1. `indigojs/clean`
   1. `indigojs/fastOptJS`

This generates the unminified lib containing the entire Indigo engine and exposing the JS API.

The output is in `indigojs/target/scala-2.12/indigojs-fastopt.js`.

If you swap `fastOptJS` with `fullOptJS` ...and wait a while ... you get the minified version (uses Google closure compiler).

The output is in `indigojs/target/scala-2.12/indigojs-opt.js`.

## The Test Harness

Exploring this project is left as an exercise for the reader, but an Indigo project is described in `index.js`.

Having performed the `fastOptJS` export above, from the project root:

1. `cd testharness`
1. `npm install`
1. `npm start`
1. Open you project in a browser, usually at `http://localhost:1234`

In your browser window you'll see a simple scene. If you open up the console you'll see all the standard Indigo startup output followed by some logging being performed by the js example.

## Generating the docs (WIP!!)

The doc generation is a work in progress. I didn't get far!

The way the `apigen` sub project works (at least in theory) is by trawling the `indigojs` project for markers, yanking out the information and converting it to JSON. e.g.

`indigojs/src/main/scala/indigojs/delegates/TintDelegate.scala`

The idea is that we can then take the JSON and use it to generate other interfaces for things like TypeScript and PureScript, and also documentation markup. None of that bit has been started.

To generate the docs:

1. Type `sbt` to enter the shell, then:
   1. `apigen/clean`
   1. `apigen/run`

That's it. The output will say something like:

```txt
indigojs/src/main/scala/indigojs/delegates/SceneUpdateFragmentDelegate.scala
indigojs/src/main/scala/indigojs/delegates/ScreenEffectsDelegate.scala
indigojs/src/main/scala/indigojs/delegates/StartUpDelegate.scala
indigojs/src/main/scala/indigojs/delegates/TintDelegate.scala
indigojs/src/main/scala/indigojs/delegates/TrackDelegate.scala
indigojs/src/main/scala/indigojs/delegates/VolumeDelegate.scala
indigojs/src/main/scala/indigojs/IndigoJS.scala
indigojs/src/main/scala/indigojs/IndigoJSException.scala
Found indigodocs to process in: ClearColorDelegate
...done
Found indigodocs to process in: TintDelegate
...done
```

The output is located in `(..)/<indigo git root>/target/jsdocs/`.
