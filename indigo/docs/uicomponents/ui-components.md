---
id: ui-components
title: Overview
---

> UI Components live in the "Indigo Extras" library, since they are built on top of Indigo itself and require no special machinery to work.

UI components are generally the kinds of elements your expect to see in any web input form, or game options menu.

Indigo does not provide a large suite of UI Components out of the box although we hope to expand, [see issue for progress](https://github.com/PurpleKingdomGames/indigo/issues/41). This is because _basic_ UI components are not terribly complicated to build on top of Indigo by aspiring game devs, and so have been pushed down the priority list in favor of more fundamental / specialized pieces of functionality. Help is welcome!

## The Pattern

The components Indigo does provide (with the exception of hit areas) follow a very specific pattern. The idea is that while the values UI components temporarily represent are interesting, and should be stored in the model (e.g. the players name for their character), the UI Components themselves are not interesting and should be somewhat ephemeral. In other words, you'd want to save the characters name, but not the state of an input field. Therefore in their current design, UI Components are only supposed to live in the view model and the view.

The pattern UI components currently follow then, is as follows:

1. User provided assets - you need to provide information about what Indigo should use to draw your components.
2. An entry in the View Model - UI components hold a small amount of state, and it is designed to be stored in the view model.
3. Presentation - pulling the assets, state, and relevant events together to draw the component.

The main thing to be aware of is that UI Components are not magic. In an OO game engine, you could expect the add a button and for it to be a self contained entity that at least renders itself without a lot of wiring. In Indigo - like everything in Indigo! - you have to stitch them into the relevant processes, i.e. the button won't mysteriously draw itself if it isn't included in your view logic.
