---
id: motivation-and-constraints
title: Motivation & Constraints
---

## Motivation

There were two questions driving Indigo's development:

1. Building games is hard, and testing games is harder... but does it have to be?
2. Can we build a game engine for functional programmers that is fun, productive, and reasonably easy to pick up?

The reason testing games is hard is the perception that they are random, and their behavior is non-deterministic by default, but this doesn't have to be the case.

Indigo encodes the idea of a frame update into one single, pure, stateless, immutable function. The new frame is always predictably the direct outcome of the values it was supplied at the beginning of the update. Even apparently random elements are in fact pseudo-random.

> Of course, this depends on the game programmer! If they put a `Random` in the middle there isn't much we can do about it! (Use a propagated `Dice` instance instead!)

To further increase reliability and code correctness, Indigo is written in Scala in order to take full advantage of it's advanced type checker.

Indigo is not an FRP engine, and does not force a particular programming model on the developer. A game programmer could write "Scala-Java" or as close to pure FP code as Scala allows. To further empower the developer, the engine has very few dependencies, so mixing in a library like Cats should be no problem at all.

Further more, the framework API "entry points" you get by default are just sugar on top of the `FrameProcessor` - you can [write your own](https://github.com/PurpleKingdomGames/indigo/blob/master/indigo/indigo/src/main/scala/indigo/IndigoSandbox.scala)!

## Creative Constraints

Why is indigo the way it is? Why isn't there proper font rendering for example?

Building a game engine is hard, and takes a long time. There is a piece of general wisdom in the gaming community that can be summarised as:

> Never build your own game engine.

Why not? Well a lot of people who decide to build game engines actually start out intending to build a game. They then get bogged down in building an engine instead, because it takes absolutely ages, and never get around to building their game.

There is only one reason to build a game engine that people seem to agree on: There isn't already another one out there that does the very _very_ specific thing you wanted an engine to do!

So in order to make building Indigo possible (4 years and counting...), we cut the scope. What is the smallest amount of functionality we'd need to build an engine we'd be happy to use? A few of those constraints included:

1. Scala only - A full FP language with a Mac / Linux friendly ecosystem.
2. 2D only - with pixel art as a first class citizen specifically.
3. Browser only - adding platforms adds complexity, lets just make it work first.
4. No fonts - simplified text support only.
5. No custom shaders - nice to have, but we can do without them.

Some of the initial constraints were relaxed, and some features that might be considered unnecessary were added just for fun, but if you look at indigo and wonder why something that might be standard in other engines isn't present, it is probably just because it wasn't considered part of a minimal spec!

## The influence of Pixel Art

Indigo is a 2D game engine (you have go HD if you want to!), but is particularly aimed at Pixel Art. Why Pixel Art?

Pixel Art is a relatively accessible art form. Like any other artistic medium you could choose, it can be done poorly and look cheap, or it can be done brilliantly and be every bit as expressive and evocative as any other style of art.

The difference is that the barrier to entry is lower. Amazing and low cost tools like [Aseprite](https://www.aseprite.org/) and [Tiled](https://www.mapeditor.org/) help put good game art within the reach of the many, not the few.

Pixel Art is commercially viable too! There are many many commercially successful pixel art games released every year! Games like [Moonlighter](https://store.steampowered.com/app/606150/Moonlighter/), [Celeste](https://store.steampowered.com/app/504230/Celeste/), and [Dead Cells](https://store.steampowered.com/app/588650/Dead_Cells/) to name but a few.

If you want to make a AAA title with photo-realistic graphics or even a super-slick 2D games and you have the resources to do that, there are better tools out there for the job! Pixel art requires us to build games that are _good games to play_ in spite of a limited visual style, games that draw people in because they are engaging, not because you can see every wrinkle on a characters face.

Not unlike board games. Board games are fun, aren't they?

Forcing the engine to be aimed at pixel art, however, has influenced how it works:

1. Magnification. You design and code it to work as if it was on a 1 to 1 pixel scale, increase the magnification and everything goes with it. For instance, mouse positions and clicks are rescaled to remain accurate to your graphics. You can even dynamically set different magnifications per game layer, allowing for instance, an HD UI over a chunky pixel game.
2. Pixel perfect rendering. The whole engine works on whole pixels, and the shaders are written to render beautifully crisp, whole pixels at any scale. You don't have to do anything clever to get perfect results.
