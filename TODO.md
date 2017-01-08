# Building a 2D pixel art game engine
This kind of engine has some interesting properties.
There are no shaders to speak of.
There is no rotation.
Everything snaps to a pixel so all positions are absolute.
There might be physics, but then again, that could be built on top if it's needed at all.

The aim is to create layers starting with mutable ugly WebGL and leading up to Purely functional code.

The layers:
1. The Renderer
2. The Game Engine.
3. The game.

## TODO

Bugs:

Renderer

Game Engine
- Revisit API
  -  SceneGraph should be like a DOM, sprites, buttons, animated sprites, static images, parallax images etc.
- Sound
- Full window size
- Full screen
- Fonts

Game
- Make game.


Much later...
- Tilemap loading?
- Lighting
  - Ambient (global tint)
  - Lighting effects
- Perf enhancement: Render at actual size to a buffer and scale up.
- Perf enhancement: We do some CPU side sorting, which generally will be ok, but if there are thousands of tiles and most of them never change, it would be nice to declare that somehow and only have to sort them once.
