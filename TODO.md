# Building a 2D pixel art game engine
This kind of engine has some interesting properties.
There are no shaders to speak of.
There is no rotation.
Everything snaps to a pixel so all positions are absolute.
There might be physics, but then again, that could be built on top if it's needed at all.

The aim is to create layers starting with mutable ugly WebGL and leading up to Purely funcational code.

The layers:
1. The Renderer
2. The Game Engine.
3. The game.

## TODO

Bugs:

Renderer
- Flip (horz, vert)
- A way to do animation frames

Game Engine
- Revisit API
  -  SceneGraph should be like a DOM, sprites, buttons, animated sprites, static images, parallax images etc.
- Input events from mouse and keyboard
- Spritesheet management
  - Define animation frames
  - Define animation cycles (frame ranges)
  - Accept JSON data with spritesheet?
- Tilemap loading?
- Layer management (just a z-index would be ok?)

Game
- Make game.


Much later...
- Lighting
  - Ambient (global tint)
  - Lighting effects
- Perf enhancement: Render at actual size to a buffer and scale up.
