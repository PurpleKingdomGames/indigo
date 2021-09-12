---
id: howto-parcel
title: How to bundle up an Indigo game with Parcel.js
---

## Motivation

Right out of the box, Indigo's plugin allows you to output a minimal website to run your game, and will also extend that website to run on desktop via Electron, or on mobile using Cordova.

All of those export types are supposed to be quick and throw away, nothing more than a reference starting point. As soon as you really want to publish your game somewhere, you will need and indeed will want to roll your sleeves up to specify exactly how that works. For example:

When bundling up a website using `indigoBuild` (or `indigoBuildFull`), any changes you've previously made to the generated HTML page will be over written, so if you want custom styles or a page layout arranging the game, then you need assets and links between them that persist between builds.

In the world of frontend development, you would solve this problem with a bundler. A popular choice is Webpack, in this tutorial we'll be using parcel.js.

What we're going to do, is convert the sbt version of the "hello, indigo!" game to work with Parcel.js.
