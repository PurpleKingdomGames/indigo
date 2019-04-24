# Prior Art

There are lots of good references on each of these subjects, here they are merely briefly enumerated as areas you can dig into if you're interested.

## Functional Reactive Programming

You can't move in the Function Programming for Games space without taking note of Function Reactive Programming (FRP), which is seem as the way to tackle this problem.

FRP, and in particular Arrowized FRP which uses reactive combinators for a point free style of programming, is all about modelling time seriously. Events are either discrete or continuous, and are associated one way or another with a sampled point in time, which is fed as input through a series of functions to generate a rendered view.

It's used in interactive applications and other forms of stream processes.

## FRAN

The Functional Reactive ANimation system known as FRAN, came out of Microsoft research with a paper in 1997. FRAN is cited as an inspiration for Elm, and indeed led to the development of FRP.

## Synchronous Reactive Programming

SRP is not a functional approach to reactive programming. The magic of SRP, and it's main influence on Indigo, is one key idea:

> Everything is assumed to happen instantly.

At the beginning of an update / tick / frame, time is sampled. This is the time that every single part of the code about to be executed will receive. No part of the code should be doing it's own system time look up.

This is a massive step forward in being able to reason about, and write test cases for your code, because everything can be calculated from a specific moment in time.

## Elm

Elm has been the inspiration for many modern frontend advances, and Indigo is no exception. Any current resemblance to Elm is coincidental, but early versions of Elm were pivotal in shaping how the problem of building a modern FP based game engine has been considered.

Elm uses a very similar Model -> Update -> View style immutable architecture, and is well worth learning. There is an active Elm game dev community, but Elm is designed for frontend web, and the we wanted more from Indigo!

## Yampa

Yampa is a Haskell based game engine that has been at the forefront of much of the development of Arrowized FRP. The papers around Yampa are both interesting and inspirational.
