---
id: model-viewmodel-view
title: Model, ViewModel, & View
---

The standard entry point traits define functions that talk about a `Model`, a `ViewModel`, and a "view" (in the form of a `present` function that returns a `SceneUpdateFragment`).

These are terms that will be very familiar to anyone that has worked with any of the many permutations of the MVC pattern, but the definitions can vary a bit.

## Separation of concerns

Indigo keeps the model and view quite separate (we'll come on to the view model later), the aim is to strictly decouple your game's structures and logic from its presentation. How strictly this is adhered to is entirely up to the game developer, but we recommend keeping them as separate as posisble.

## Seeing is believing

> The world the player sees and interacts with, is not the game.

What do server-side programming and Wolfenstein 3D have in common?

If you've ever worked in server-side programming, you will have already come across this idea. The way you represent data in a database is most likely - except in very simple cases - not how it appears when it is accessed by an API. Why not? Because they serve different purposes. One models your data in the most accurate, useful, and domain appropriate way possible, and the other presents it in the format most appropriate for consumption. Fields essential to data storage and business logic calculations are omitted, merged, or have their representations changed and massaged before being served up to the requester.

Another great example of this is the legendary game [Wolfenstein 3D](https://en.wikipedia.org/wiki/Wolfenstein_3D). Wolfenstein, precursor to Doom, was one of the first really famous "3D" game, filled with the high adrenaline action and an immersive (for its time) experience.

Except... "Wolfenstein 3D" is a 2D game.

If you could see a mini-map in the corner, it would show a flat blocky level with dots representing baddies moving about, and that is the real game. The mini-map is the model, the mini-map is the game, and the 3D corridors and bad guys filling your viewport are just an amazingly clever bit of presentation work.

If you'd like to know more, [Fabien Sanglard's book](https://fabiensanglard.net/gebbwolf3d/) goes into every tiny detail.

## Working definitions

Here are the definitions Indigo uses:

- **Model** - A pure and abstract representation of your game. Data that models your game's state with no regard for how it will be presented.
- **ViewModel** - Presentation state that is inessential to the game, but improves visual experience.
- **View** - An as-simple-as-possible translation of the model and view model into a visual experience, handling little to no game logic.

## More on the `ViewModel`

We've talked a bit about the model and the view, as they are the two most cleanly separated of the three elements.

The last element is the view model, and arguably it is superfluous. The view model is state used to help produce a visual representation, and could easily be kept as some sub-section of the Model data. Indeed, `SubSystem`s make no distinction as they assumed to be small and simple enough to be manageable. The point of the view model is that it represents data the matters to the view, but not to the state of the game, and thus helps you to keep your game model clean.

Example use of a `ViewModel`:

You've built a pinball machine game!

The model stores the players running score as a simple number, and in your prototype, that score is extracted from the model and drawn on the screen by the `present` function that generates the view.

Works fine but lacks excitement. On real pinball machines, as you score more and more points, the scores are multiplied and big numbers start rolling onto the board. That "rolling" is literal, it's a mechanical process trying to keep up with the massive scores being achieved and its noisy and exciting!

In order to simulate that, at minimum we really need:

1. The actual current score that the player has achieved - the most important thing.
2. The score currently on the board (which is behind the real score)
3. A value describing how much to increment the score on the board by on each frame tick until it catches up with the actual score.

Point 1 is clearly an essential business value. If you needed to send a highscore to a server for a global leaderboard - that is the only value you'd care about. As such, it clearly belongs in your games model.

Points 2 and 3 though are inessential in terms of the integrity of the games inner state representation. The view needs them to draw the effect of the mechanical scoreboard, but they are transient, and putting them in the model itself is an act of coupling or polluting the model with presentation information.

Instead, we put those two values in the `ViewModel`. The effect can be rendered, but the model is unaware of the details of how it's presented.
