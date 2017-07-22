#Framework

What the heck is this?

Well, indigo is an FRP inspired engine designed for FP (Scala) programmers who want to make browser games.

FRP does not sit comfortable along side visual editor tools (philosophically) however, and the mass audiences understand tools like Unity and the ECS system.

This "framework" then, converts our lovely FRP engine into one that behaves like a dirty ECS one, so that we can build a visual game editor.

##How are we going to do this?
The plan for the first attempt is:
1. Dynamically load game config
2. Dynamically load a list of assets to load
3. On initialisation, load a game definition (GD) file.
4. The GD contains information about view assets than can be pre-prepared, for the views to render efficiently.
5. The GD contains the "model" which has been hijacked for use as a generic game structure state machine e.g. scenes
6. The model state machine is initialised and the first scene is rendered and the game begins...

That should get us to "a configured game being rendered" but of course it doesn't do anything useful.

The next things to do are attach a script to an element in the view (JavaScript) and use that to do something useful like swap and asset or play an animation.

We would then hope to move on to building a super rough editor to generate all the above files for us to load.

Then hey presto, we have a working, if naff, PoC.



