# GameTime

The most important thing to know is what time it is now! This can by found on the [frame context](/03-gameloop/frame-context.md), which provides two important different time representations:

1. `running` - which is the time the game has been running for in total.
2. `delta` - which is how long has passed since the last update, which is important for smooth updates and animations.

Both are represented as `Second`s.

The `Scene` version of frame context objects additionally provide information about the running time of the particular scene you are in, and when the scene was last changed.

