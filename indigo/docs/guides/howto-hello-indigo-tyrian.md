---
id: howto-responsive-ui-with-tyrian
title: How to make a responsive UI using Tyrian
---

## Responsive UI Using Tyrian

There tend to be two primary types of UI in games: In-game UI (such as health over a
player), and overlay (such as a HUD or dialog box). In-game UI tends not to
worry too much about the screen size, and is dealt with primarily by the
magnification configuration within Indigo. But what about overlays and HUD's?
These tend to scale with the size of the screen and the position of such
elements may change depending on the capabilities of the display. Many game
engines provide a way to create overlay UI elements that scale in some way to
the space they're in using a separate UI system. In Indigo we're exporting to
JavaScript and a browser (even in Electron), so depending on how we want the UI
to look, one option is to employ more standard web technologies to do this work
for us, i.e. HTML and CSS.

Using Tyrian we can utilise HTML and CSS to ensure our overlay UI scales
no matter what screen size we use.

In this guide we'll take the basic `hello-indigo` example and add a button and
a counter that scales with the size of the screen. It's a pretty basic example,
but it should give you an idea of what can be achieved.

## Overview

This is a fairly involved guide with many parts to it that need to be implemented
in order to get everything working. However the concept is pretty simple - we'll
be using the Tyrian Bridge as a communication layer between Indigo and our
`index.html` (which is controlled by Tyrian).

These are the steps we're going to take:

* [Set up the environment](#setting-up-the-environment) - e.g. clone `hello-indigo` and set up ParcelJS
* [Modify our build files](#setting-up-the-build) - e.g. update our `build.sbt` or `build.sc` files
* [Create our static files](#setting-up-the-html-and-parceljs) - e.ge create our html, js, css and other static content
* [Update Indigo with a new subsystem](#update-helloindigoscala) and [Initialise our Tyrian app](#create-a-tyrian-app)
* [Get Tyrian publishing messages to Indigo](#tyrian-to-indigo-communication) (and Indigo to subscribe to them)
* [Get Indigo publishing messages to Tyrian](#indigo-to-tyrian-communication) (and Tyrian to subscribe to them)
* And finally [creating a responsive UI in CSS](#responsive-ui)

## Setup

### Setting up the Environment

For this guide we'll be using the `hello-indigo` example in either
[mill](https://github.com/PurpleKingdomGames/hello-indigo) or
[sbt](https://github.com/PurpleKingdomGames/hello-indigo-sbt).
In the existing implementation clicking on the dots will create a new dot
that then rotates around the canvas. We'll modify this so that clicking a button
does this job instead.

You'll want to upgrade `hello-indigo` to use `IndigoGame`. To do this either
follow [this guide](howto-indigo-game.md)
or replace `HelloIndigo.scala` with
[this](https://gist.github.com/hobnob/c24f00936e91a7b7e5d644d19e4f1b32)

Although Indigo builds and exports to Electron natively, for this project
we'll export directly to HTML and use Yarn to run our web server, with
ParcelJS to copy and package up our HTML, JS, and CSS.

You can use NPM to install Yarn with the command `npm install yarn`, and then
install ParcelJS with `yarn add parcel --dev`.

### Setting up the Build

Next you'll need to update either your
`build.sc` (if using mill) to  with this:

```diff
   def buildGame() = T.command {
     T {
       compile()
       fastOpt()
-      indigoBuild()()
     }
   }

-  def runGame() = T.command {
-    T {
-      compile()
-      fastOpt()
-      indigoRun()()
-    }
-  }
-
   val indigoVersion = "0.12.1"

   def ivyDeps =
    Agg(
+     ivy"io.indigoengine::tyrian::0.3.1",
+     ivy"io.indigoengine::tyrian-indigo-bridge::0.3.1",
      ivy"io.indigoengine::indigo-json-circe::$indigoVersion",
      ivy"io.indigoengine::indigo::$indigoVersion",
      ivy"io.indigoengine::indigo-extras::$indigoVersion"
    )
```

 or `build.sbt` (if using sbt) to this:

 ```diff
 libraryDependencies ++= Seq(
   "io.indigoengine" %%% "indigo"            % "0.12.1",
+  "io.indigoengine" %%% "tyrian"               % "0.3.1",
+  "io.indigoengine" %%% "tyrian-indigo-bridge" % "0.3.1",
 )
 ```

What we've done here is add Tyrian and the Tyrian Indigo Bridge to our build.
Tyrian will deal with all of our HTML and the Indigo Bridge will deal with
communication between Tyrian and Indigo. We've also removed `runGame` as this
would usually run Electron.

### Setting up the HTML and ParcelJS

Usually Indigo will generate all of our HTML for us and run it through Electron,
but for this example we're going to generate our own HTML so that we can
inject CSS.

Firstly we'll create an `app.js` file that simply loads Tyrian and then launches
it for our page:

```js
import {
    TyrianApp
} from './out/HelloIndigo/fastOpt.dest/out.js';

TyrianApp.launch("main");
```

We'll also need an `index.html` to hold our basic HTML data:

```html
<!DOCTYPE html>
<html>
  <head>
      <meta charset="UTF-8">
      <title>Hello Indigo</title>
      <link rel="stylesheet" href="css/main.css" />
      <script type="module" defer src="app.js"></script>
  </head>
  <body>
      <div id="main"></div>
  </body>
</html>
```

Tyrian currently uses `snabbdom` for adding and removing elements in a web page.
To do this we will need to add a dependency to `snabbdom` by running
`yarn add snabbdom`.

You'll notice we're using the direct JS output from our build here, which may
feel odd. What will happen when we run ParcelJS through Yarn is that the HTML
will be copied to a build directory along with the JS, CSS and any dependant
static files (such as images) that may be needed, and everything will be correctly
linked by Parcel.

Now generate an empty `css/main.css`.

Next we'll need to update the `package.json` so that ParcelJS will run when yarn
starts and copies the static files for Indigo to use. To do this add the
following to the top of `package.json`:

```json
"scripts": {
  "start": "parcel index.html --open --no-cache"
}
```

We'll need to add to tell ParcelJS about our static files by adding the
following to `package.json`:

```json
"staticFiles": {
  "staticPath": "assets",
  "staticOutPath": "assets",
  "watcherGlob": "**"
}
```

We need to add the dependency for static files by running
`yarn add parcel-reporter-static-files-copy --dev` from the command line.

Finally, we need to configure ParcelJS so that it knows how to copy our static
files. Add a `.parcelrc` file and add the following:

```json
{
  "extends": ["@parcel/config-default"],
  "reporters":  ["...", "parcel-reporter-static-files-copy"]
}
```

### Update HelloIndigo.scala

We'll be using the
[`TyrianSubSystem`](https://tyrian.indigoengine.io/concepts/tyrian-indigo-bridge/)
as a way of communicating between Indigo and Tyrian. Add
`import tyrian.TyrianSubSystem` to the imports in `HelloIndigo.scala`, and then
update the `object` to be a `case class` so that we can pass in the subsystem as an argument. We can also remove the top level export, as we'll no longer need it:

```diff
- import scala.scalajs.js.annotation.JSExportTopLevel
+ import tyrian.TyrianSubSystem

- @JSExportTopLevel("IndigoGame")
- object HelloIndigo extends IndigoGame[Unit, Unit, Model, Unit] {
+ final case class HelloIndigo(tyrianSubSystem: TyrianSubSystem[Int]) extends IndigoGame[Unit, Unit, Model, Unit] {
```

We'll also need to tell Indigo to use the new sub-system, which we can do by
adding an entry to the `boot` method like so:

```diff
   def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
     Outcome(
       BootResult
         .noData(config)
         .withAssets(assets)
+        .withSubSystems(tyrianSubSystem)
     )
```

## Create a Tyrian App

We'll now develop our initial Tyrian app, which will consist simply of
a canvas (for Indigo), a counter, and a button, wrapped in a few `div` elements.
Create a new file called `HelloTyrian.scala` inside the `helloindigo/src` folder
and add the following contents found
[here](https://gist.github.com/hobnob/436318b3ae5eed5891ba2b18bb8c264b).

You can now build the project in mill or sbt and then run `yarn start` to see the HelloIndigo demo running inside a Tyrian website. This is great if you want a
website surrounding your game with no interaction, but it would be much more
useful to get them both talking to each other... this is what we're doing next!

## Tyrian to Indigo Communication

The first thing we'll do is get Tyrian to request that Indigo add a new dot. To
do this we'll need to add an `OnClick` event to our button, which will tell
Indigo what to do.

In `HelloTyrian.scala`, we'll need a new message type. Add a new message type called AddDot like so:

```diff
   enum Msg:
     case StartIndigo extends Msg
+    case AddDot      extends Msg
```.

Next we'll need to deal with that message in Tyrian, by adding a case to `update`
like this:

```diff
     msg match
+      case Msg.AddDot =>
+        (model, model.bridge.publish(IndigoGameId(gameDivId), 0))
       case Msg.StartIndigo =>
```

The final part on the Tyrian side is to hook the button up to fire the `AddDot`
message. To do this add an `OnClick` attribute to our button like so:

```diff
       div(`class` := "btn")(
-        button()("Click me")
+        button(onClick(Msg.AddDot))("Click me")
       )
```

This will now send an event to Indigo with an integer message, which in this case
we've set to zero. Now we just need to modify Indigo to receive and
process that message. To do this change the `MouseEvent.Click` line in
`HelloIndigo.scala` with the following:

```diff
   ): GlobalEvent => Outcome[Model] = {
-    case MouseEvent.Click(pt) =>
+    case tyrianSubSystem.TyrianEvent.Receive(msg) =>
+      val pt               = Point(100, 100)
       val adjustedPosition = pt - model.center
```

We've also added a line below that to set a `pt` variable as we now longer have
a mouse position. For now, we've set that to 100,100 like so which is just a
fixed point for the dots to rotate around.

Once more build the project in mill or sbt and then run `yarn start`. This time
you'll notice that clicking the game doesn't do anything, but clicking the
button adds a new rotating dot.

We've now successfully gotten Tyrian to talk to Indigo. In the next part we'll
be doing the opposite, and adding a counter for the number of dots.

## Indigo to Tyrian Communication

In this part we're going to store a count of the number of dots that Indigo is
currently displaying. To do this, first update the model in `HelloTyrian.scala`
so that it has a `count` property which is initialised to zero, like so:

```diff
-final case class TyrianModel(bridge: TyrianIndigoBridge[Int])
+final case class TyrianModel(bridge: TyrianIndigoBridge[Int], count: Int)
object TyrianModel:
-  val init: TyrianModel
+  val init: TyrianModel =
+    TyrianModel(TyrianIndigoBridge(), 0)
```

We'll need to display that count on the website. To do this, we simply modify our
counting `div` to display what's in the `model` like so

```diff
-      div(`class` := "counter")(),
+      div(``class`` := "counter")(model.count.toString)
```

A new message type is also required as before, so we'll add a new one like so:

```diff
   enum Msg:
-    case StartIndigo extends Msg
+    case StartIndigo             extends Msg
-    case AddDot      extends Msg
+    case AddDot                  extends Msg
+    case IndigoReceive(msg: Int) extends Msg
```

We'll need to subscribe to incoming Indigo messages so that we can act on them.
This can be done using the bridge subscriptions like so:

```diff
   def subscriptions(model: TyrianModel): Sub[Msg] =
-    Sub.Empty
+    model.bridge.subscribe { case msg =>
+      Some(Msg.IndigoReceive(msg))
+    }
```

The final part on the Tyrian side is then to update the model once we get a
message from Indigo. Indigo will send an Integer message back to Tyrian, which
we can use directly in our model To do this we simply add the following to our
`update` pattern:

```diff
     msg match
       case Msg.AddDot =>
         (model, model.bridge.publish(IndigoGameId(gameDivId), 0))
+      case Msg.IndigoReceive(msg) =>
+        (model.copy(count = msg), Cmd.Empty)
       case Msg.StartIndigo =>
```

For the Indigo side we need to modify the `updateModel` so that the new model is
assigned to a variable for later use. We can then add a global event through
the Tyrian Subsystem letting the website know how many dots we have. This is done
with the following:

```diff
     case tyrianSubSystem.TyrianEvent.Receive(msg) =>
       val pt               = Point(100, 100)
       val adjustedPosition = pt - model.center
-
-      Outcome(
-        model.addDot(
-          Dot(
-            Point.distanceBetween(model.center, pt).toInt,
-            Radians(
-              Math.atan2(
-                adjustedPosition.x.toDouble,
-                adjustedPosition.y.toDouble
-              )
-            )
-          )
-        )
-      )
+      val newModel = model.addDot(
+        Dot(
+          Point.distanceBetween(model.center, pt).toInt,
+          Radians(
+            Math.atan2(
+              adjustedPosition.x.toDouble,
+              adjustedPosition.y.toDouble
+            )
+          )
+        )
+      )
+
+      Outcome(newModel)
+        .addGlobalEvents(tyrianSubSystem.send(newModel.dots.length))
```

The important part here is the `addGlobalEvents` which sends a subsystem event
to Tyrian.

Now whenever you press the button on the website, the counter will increase by
one! This is the basics of communication to and from Tyrian, but we can go
further with a little CSS magic. In the next part we'll be dealing purely with
CSS to show how we can make a responsive UI.

## Responsive UI

One of the benefits of adding UI via Tyrian is that you get full use of the
power of CSS and HTML for creating a game that supports all sorts of screen
sizes. To do this, we'll be making use of
[Flexbox](https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Flexbox)
and [Media Queries](https://developer.mozilla.org/en-US/docs/Web/CSS/Media_Queries/Using_media_queries).

We'll gloss over the initial setup as it's a lot of boiler-plate. Replace your
`css/main.css` with
[this](https://gist.github.com/hobnob/10a193a167813b95c690a35cccd9bfc1).
All of the work in this part takes place in `main.css`.

On a desktop that looks pretty good, so we're going to change the layout for
mobiles. To do this, we'll first make a media query CSS rule at the end of our
CSS like so:

```css
@media (max-width: 767px) {
}
```

All of the next few parts will now take place within the curly braces of that
media query. Firstly we'll change the button to be 100% of the screen width,
and increase it's height so it's easier to click on a mobile:

```css
.btn {
  align-self: flex-end;
  padding-bottom: 0;
}

.btn button {
  width: 100vw;
  height: 7rem;
}
```

Now we'll make the counter bigger, so it's easier to read:

```css
.counter {
  font-size: 5rem;
}
```

And finally, we'll reduce the size of the Indigo canvas so that it will fit on
a smaller device sizes. It's worth noting that, to date, Indigo won't deal
particularly well with mouse positions when the canvas is scaled like this. For
our purposes though, this will be fine:

```css
canvas {
  transform: scale(0.75);
}
```

Once more run `yarn start` and you'll now be able to scale your browser window
bigger and smaller to see the effects of our changes.

## Further Work

Now you know the basics of responsive UI using Tyrian and Indigo you can
experiment with some more advanced features. You could, for example, fire an
event to Indigo on a canvas resize that will change the size Indigo renders at
(which is more efficient that the transform we used here). We could also use
CSS to show a dialog box, preventing the player from interacting with the game
until they've given feedback.
