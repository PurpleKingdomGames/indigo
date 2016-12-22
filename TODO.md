# Building a 2D pixel art game engine
This kind of engine has some interesting properties.
There are no shaders to speak of.
There is no rotation.
Everything snaps to a pixel so all positions are absolute.
There might be physics, but then again, that could be built on top if it's needed at all.

The aim is to create layers starting with mutable ugly WebGL and leading up to Purely funcational code.

The layers:
1. The WebGL abstraction. (mutable)
2. The game engine. (pure)
3. The game.

## TODO

- Load and apply a texture
- Make the rectangle move at a framerate
- Render in pixels...

