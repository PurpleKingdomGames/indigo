---
id: prior-art
title: Prior Art
---

Indigo was not built in a vacuum, it's the conflation of lots of different borrowed ideas, mostly from the places listed below. There are lots of good references on each of these subjects, but here they are briefly enumerated as areas you can dig into if you're interested.

## Functional Reactive Programming

You can't move in the "Functional Programming for Games" space without hearing about [Functional Reactive Programming](https://en.wikipedia.org/wiki/Functional_reactive_programming) ([FRP](https://wiki.haskell.org/Functional_Reactive_Programming)), which is seen as the way to tackle this problem. Indigo is _NOT_ an FRP system, but ideas have been borrorwed.

FRP, and in particular Arrowized FRP which uses reactive combinators for a point free style of programming, is all about modelling time seriously. Events are either discrete or continuous, and are associated one way or another with a sampled point in time, which is fed as input through a series of functions to generate a rendered view.

It's used in interactive applications and also other forms for things like stream processing.

Evan Czaplicki, the author of [Elm](https://elm-lang.org/), gives a good summary in his talk ["Controlling Time and Space: understanding the many formulations of FRP"](https://www.youtube.com/watch?v=Agu6jipKfYw)

## FRAN

The Functional Reactive ANimation system known as FRAN, came out of Microsoft research with a [paper](http://conal.net/papers/icfp97/) in 1997. FRAN is cited as an inspiration for Elm, and led to the development of FRP. Some of the high-levels ideas from FRAN turn up in the time based animation approach found in places like Indigo's automata subsystem, but Indigo is not intended to only follow that mode of operation.

## Synchronous Reactive Programming (SRP)

[SRP](https://en.wikipedia.org/wiki/Synchronous_programming_language) is not a functional (as in, FP) approach to reactive programming. The magic of SRP, and it's main influence on Indigo, is one key idea: Everything is assumed to happen instantly.

>"The synchronous abstraction makes reasoning about time in a synchronous program a lot easier, thanks to the notion of logical ticks: a synchronous program reacts to its environment in a sequence of ticks, and computations within a tick are assumed to be instantaneous, ..."

At the beginning of an update / tick / frame, time is sampled. This is the time that every single part of the code about to be executed will receive. No part of the code should be doing it's own system time look up.

This is a massive step forward in being able to reason about and write test cases for your code, because everything can be calculated from a specific moment in time.

## Elm

[Elm](https://elm-lang.org/) has been the inspiration for many modern frontend advances, and Indigo is no exception. Any current resemblance to Elm is coincidental, but experiences with early versions of Elm and original paper ["Elm: Concurrent FRP for Functional GUIs"](https://elm-lang.org/assets/papers/concurrent-frp.pdf) were important influences in shaping how the problem of building a FP based game engine has been considered.

Elm uses a very similar Model -> Update -> View style immutable architecture, and is well worth learning. There is also an active [Elm game dev community](https://github.com/rofrol/awesome-elm-gamedev).

## Yampa

[Yampa](https://wiki.haskell.org/Yampa) is a Haskell based Arrowized FRP system that has been used for [game engine development](https://wiki.haskell.org/Yampa/game_engine). Some of the papers around Yampa have been both interesting and inspirational in Indigo's development, particularly ["Testing and Debugging Functional Reactive Programming"](http://www.cs.nott.ac.uk/~psxip1/papers/2017-ICFP-Perez-Nilsson-TestingAndDebuggingFRP-latest.pdf). Indigo includes a limited but very useful `Signal` and `SignalFunction` suite, but doesn't go anywhere near as far as Yampa, and shies away from stateful signals completely (to the authors limited understanding).
