---
id: cross-platform-publishing
title: Cross Platform Publishing
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Indigo supports basic publishing to/building for the following platforms:

- Browser
- Desktop
- Mobile

Using combinations of three supporting technologies:

1. HTML5
2. Electron
3. Cordova

## Installation

You will need Electron and Cordova installed globally to follow along, via npm they can be installed with `npm install -g electron` and `npm install -g cordova` respectively.

## A Friendly Word of Caution

Indigo is not designed to be an all singing all dancing cross platform game publishing framework. That is a big maintenance overhead that we don't have the man power or hardware to fully support (sponsorship welcome!) - the testing alone is a serious undertaking.

What Indigo provides is a starting point: Some basic and probably very naive templates to get you going.

It is possible to publishing Indigo games to pretty much any device you like (we think...), but you will have to roll up your sleeves, set up testing rigs, read the relevant docs, and invest time learning how to do it.

## Foundations

Indigo has two commands, `indigoBuild` and `indigoBuildFull` (for the optimized version)`, that will generate a simple static site of your game. Everything is built on top of that.

## Publishing for the web

You can run the `indigoBuild` version from a local web server (such as npm's `http-server`) and view it in a browser.

The fancier way to do this is via Cordova. After doing a normal Scala.js compile, you run the `indigoCordovaBuild` (or `indigoCordovaBuildFull`) task, and navigate to the folder of the same name in the output directory, e.g. the [Snake demo](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/demos/snake) would be at `(..)/indigo-examples/demos/snake/out/snake/indigoCordovaBuild/dest` (note the `/dest` folder is there because it's a Mill build and wouldn't be present in SBT).

You then set up the Cordova project as follows:

1. `cordova platform add browser`
2. `cordova prepare`
3. `cordova run browser`

This will start a web server and open a browser tab with your game running in it.

The benefits of doing this over just running `http-server` are limited. It does support dynamic reloading when assets change, though we believe this would only really work for the surrounding files (HTML, CSS and non-generated JS files) rather than the game itself, and even that may need additional Cordova configuration.

## Publishing for Desktop

Desktop publishing can be achieved using Electron as a standalone, or Electron running in Cordova.

When you invoke the `indigoRun` command, what that actually does is build an electron template in a folder in the output directory called `indigoRun`, same at the task name. It then runs the electron game using the `electron .` command from within that folder. You can use that as a starting point directly.

Alternatively, you can publish via Electron-Cordova:

1. Run the `indigoCordovaBuild` task
2. Navigate to the `indigoCordovaBuild` output folder
3. `cordova platform add electron`
4. `cordova prepare`
5. `cordova run electron`

Both Cordova and Electron come with packaging and publishing options, please see their respective documentation.

> Cordova also technically allows for the building of native applications using, for example, the `osx` platform. However this does not appear to work with Indigo since it loads a static view rather than one served from a web server as Electron and iOS do.

## Publishing for Mobile

The Indigo plugin currently only generates configuration for iOS apps. There is no reason Android can't be added, you just need to read the Cordova documentation.

> Android is not included by default for the reason that we can't adequately test it (boring logistical reasons), we'll try to add it in a future release...

Publishing for mobile is slightly more involved as you'll need to have the relevant development kits installed, Xcode for iOS and Android SDK/Android Studio for Android.

Once you have your environment set up, the process is very similar:

1. Run the `indigoCordovaBuild` task
2. Navigate to the `indigoCordovaBuild` output folder
3. `cordova platform add ios`
4. `cordova prepare`
5. `cordova run ios`

Where the process will eventually differ is that you will probably want to run different simulators, and open the output directory in Xcode or Android Studio in order to take advantage of their development and testing environments.
