---
title: Post-Mortem: "RoguelikeDev Does The Complete Roguelike Tutorial" in Scala
author: Dave Smith
authorURL: http://twitter.com/davidjamessmith
---

!["A early screenshot of a roguelike made with Indigo"](https://raw.githubusercontent.com/davesmith00000/roguelike-tutorial/main/part3/roguelike-part3_2.gif)

**This is a brief post-mortem of my attempt to do the ["RoguelikeDev Does The Complete Roguelike Tutorial"](https://www.reddit.com/r/roguelikedev/comments/o5x585/roguelikedev_does_the_complete_roguelike_tutorial/) in Scala.**

I've been asked on several occasions if it would be possible to build a roguelike using Indigo?

My answer has always been that it should be possible, but that it would probably be challenging because Indigo isn't built for rendering lots of text.

About a month before this year's annual "RoguelikeDev Does The Complete Roguelike Tutorial" I was asked again, and decided it was time to find out if it really was possible or not!

<!--truncate-->

## What are roguelikes?

[Roguelike](https://en.wikipedia.org/wiki/Roguelike)'s are a type of game that get their name because they are ...wait for it ..._like_ an 80s game called _Rogue_!

They typically use ASCII art for graphics, generated levels / dungeons and feature things like perma-death.

## Starting at the finish

### Where can I play it?

[All completed parts of the roguelike tutorials](http://rogueliketutorials.com/tutorials/tcod/v2/) can be found in the following repo, along with playable links so that you can try out it's progression for yourself:

[https://github.com/davesmith00000/roguelike-tutorial](https://github.com/davesmith00000/roguelike-tutorial)

### What should I expect?

[The final playable version of the "game"](https://davesmith00000.github.io/roguelike-tutorial/part13/) is surprisingly playable.. for a while at least. :-)

Sure, the lack of game balance is quickly apparent and after about level 6 or 7 you'll run out of new things to do, but I'm really quite pleased with it. There's exploration, discovery, spells, potions, equipment, monsters, levels, an inventory, and menus - all the things in the tutorial, in fact! And considering I spent _no time at all_ selecting colors or which tileset to go with, it actually looks quite nice!

The code quality is ok.

When I began the project I had some lofty ideas about writing lovely clean code that would be easy to follow, but as you can see if you look at the later stages of the code base, by the end I was just pleased to be crossing the finishing line at the end of the marathon.

Maybe I'll refactor it next year..

## The tutorial

The tutorial is very well written and the "RoguelikeDev Does The Complete Roguelike Tutorial" follow-along is well paced, taking me two to three evenings a week.

The difficulty with the tutorial is that it is aimed at Python developers, and as they say in the follow-along's description, if you're not using Python the expectation is that you'll blaze your own trail.

There are two problems with not using Python.

The first is simply distilling the tutorial parts into the intended deliverables. The tutorials are written for Python developers, and they have a lovely conversational style, building a narrative as they go along. The authors take great care to go over code from previous chapters - refactoring code to be ready for the next section. Of course, if you're not a Python developer then the result is - in places - _a lot_ of text / code to sift through to find the information you need. Luckily, the screenshots the authors included were a massive help. In the chapter on saving and loading I basically just looked at the screenshots and reverse engineered suitable requirements. This was because most of the tutorial chapter was involved in disk IO while my solution ran in a browser, and is side effect free!

The second problem is the lack of ready-made tooling.

## DIY Tooling

The Python version uses a library called [tcod](https://python-tcod.readthedocs.io/en/latest/) which essentially gives you all of the functionality you need to build a roguelike, and your job in the tutorial is to build the game logic and data structures.

Choosing to follow along in another language means that you need to fend for yourself. In my case I ended up building a [roguelike starter kit](https://github.com/PurpleKingdomGames/indigo-roguelike-starterkit) to fill in some of the critical gaps.

The [README](https://github.com/PurpleKingdomGames/indigo-roguelike-starterkit/blob/main/README.md) on the starter kit's repo explains most of the functionality that the starter kit provides in a fair amount of detail, but the main things it does is:

1. Give you easy access to Dwarf Fortress assets.
2. Provide two ways of rendering coloured terminal-like text.

Dwarf Fortress is a very famous roguelike with [many different tilesets available](https://dwarffortresswiki.org/Tileset_repository). They are all based on the "IBM Code Page 437" or "Extended ASCII" table, and what you get is an image of a grid of characters and symbols. The starter kit uses a little compile time script to convert the tileset of your choice, into pre-baked classes containing all of the character information for use with either Indigo's `Text` primitive using the `TerminalText` material, or the `TerminalEntity`/`TerminalEmulator`.

It wasn't terribly difficult, but this little bit of tool sharpening was very satisfying to do and made the rest of the build much more fun.

## Lessons Learned

### What went well?

Taking the time to build the starter kit paid itself back many times over. Once I got into the game building proper, I basically never had to think about how to do the rendering again.

Converting the tutorial to pure functions and Indigo's uni-directional data flow was challenging at times, but made following the code easy, and in the few places where I really needed tests it was easy to set them up. I didn't write very many tests however, because most of the game was just plumbing that required little checking beyond making the compiler happy. Testing came into it's own wherever something non-trivial needed to be verified, such as the path finding code.

The tutorial descriptions of topics like procedural dungeon generation were really great and easy to follow.

### Where did I get lucky?

For reasons best known to someone else, early in the development of the starter kit I set it up to work with [Parcel.js](https://parceljs.org/). This turned out to be a great idea because it made publishing the playable tutorial parts to github pages a piece of cake!

### What went wrong?

I repeatedly underestimated the size of the tutorial!

Each chapter often does more than one thing and I was regularly halfway through a section when I realised I was going to have to do something much more complicated than I anticipated, or that I was fundamentally missing some piece of functionality / tooling.

Another unexpected problem was that the tutorials rely on a feature of game engines that Indigo considers a defect, and fixes by default. In Indigo you cannot (easily / idiomatically) modify the state of another actor / entity during the current frame. To put it another way, there is no "first mover" advantage to having the good fortune of being the first entity updated. Consider the following:

1. The Player attacks an Orc
2. The Orc is killed
3. The Orc cannot attack the Player because it is dead.

Seems reasonable.

In Python this translates easily:

1. On update, the Player attacks an Orc, directly calling it's damage method.
2. The Orc's health is reduced below zero and it is killed.
3. ... that's it.

This works because the Player's actions are always evaluated first and carried out immediately. In a fair fight with two evenly matched players, the first player would always win.

In Indigo things are not so simple because Indigo hugely discourages first-mover advantage by decoupling action from outcome. In reality the combatants effectively both attacked at the same time in the same instant! Like this:

1. On update, the Player attacks the Orc, _emitting an event with the amount of damage to be inflicted_.
2. The Orc's health has not yet been reduced, so the Orc is still alive during this update.
3. The Orc attacks! It doesn't know it's dead yet! It gets one last gasping attempt at killing the Player! _The Orc also emits an event saying the Player has been damaged_.
4. On the next frame, the relevant damage is inflicted on both parties.
5. The Orc is killed as before... but the Player has been unexpectedly injured or killed too!

The solution is to separate the players turn from the enemies turn, creating a turn based game. This effectively mimics the original behavior but it's now part of the game's design rather than a happy accident.

### What would I do differently next time?

My main regret is that the terminal emulator is rather heavy and can't be refreshed at 60 fps. For the purposes of the tutorial this isn't a problem since you only have to update on key press, but if you wanted to do any smoother effects it wouldn't be able to keep up. The other way this problem rears it's head is if someone is running a low power system of some kind, since it requires the ability to allocate some fairly chunky arrays on to the GPU as UBO data (uniform buffer object).

There are lots of ways to resolve this problem, I would have liked to have had the time to make it better, maybe next time.

Additionally I'd like to have wrapped up the starter kit into a library of some sort for ease of use by others.

Finally, a small thing, but I wish I'd spent a little bit longer on the build process. When you build for web you have to emit a different module kind than if you're running locally, and that's a manual change in the `build.sbt` file. It would have been nice to do that properly. ([The process is described in the starter kit's README.](https://github.com/PurpleKingdomGames/indigo-roguelike-starterkit#how-to-run-and-package-up-this-game))

## Final thoughts

Following the tutorial in a language other than Python added a lot of complexity. Converting the tutorial to a purely functional language / approach on top of that often meant completely ignoring what the tutorial was saying, and trying to achieve the same outcome by totally different means. This would be very difficult if you didn't know your engine well in advance.

That said, following the tutorial was great fun and I would highly recommend it. Hopefully I'll be able to have another crack at it next year! In the meantime, perhaps the version I've pulled together (along with the starter kit) will help the next adventurous Scala soul that decides to delve into the depths of a roguelike!
