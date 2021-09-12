---
id: game-entry-points
title: Game Entry Points
---

Entry points are typically traits that you extend in order to write your game, and they provide the basic structure that you need to adhere to, to make a game that Indigo understands. What they actually do underneath is provide a user friendly way to build the frame processor and start the engine that does all of the work.

## Built in entry points

There are currently three recommended ways to make an indigo game, three entry points traits to extend from listed here in order of increasing complexity and power:

1. `IndigoSandbox[StartUpData, Model]`
2. `IndigoDemo[BootData, StartUpData, Model, ViewModel]`
3. `IndigoGame[BootData, StartUpData, Model, ViewModel]`

`IndigoSandbox` is for experiments and very simple games that require only the core facilities to work. This interface has the benefit of being very clean and clear, but you'll quickly need to upgrade if you need to do anything like embedding it in your own website. The sandbox is also great place to start learning Indigo since all of the core principles carry through to the other entry points.

`IndigoDemo` is the next step up in complexity. With Indigo demo you can theoretically do anything, but it lacks built in scene management. You might choose this entry point for demos or games that still aren't too big but require more functionality / control than sandbox affords, such as boot flags and subsystems.

`IndigoGame` has all the functionality of `IndigoDemo`, but it adds scene management to help you describe games that have many screens and levels and so on. There is another complexity bump here that makes it less friendly for beginners but that quickly becomes worth it when building bigger projects.

## Roll Your Own

All of the entry points previously described are provided with Indigo, but they are abstractions and not _actually_ required.

Indigo really only needs two things to run, a `FrameProcessor` and an instance of the `GameEngine`.

If you know a tiny bit of Scala.js and you are so inclined, you can see how they are built / run by looking at the code for, say, `IndigoSandbox` (it's only one class!). Dig a little deeper and you'll notice that things like the scene manager only really exist superficially, the core of Indigo doesn't know anything about scenes.

If you have a great idea for a better way to program games - perhaps a better interface built on some shiny new popular library - why not see if you can build a new entry point for it!
