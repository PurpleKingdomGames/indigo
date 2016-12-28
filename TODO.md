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

Renderer
- Share resources for maximum use of instancing
- Magnification
- Image effects
  - Tint
  - Alpha
- Flip (horz, vert)
- Remove update routine (move to game engine)
- Draw is a function that takes a list of renderable items (typeclass)
- Renderer is instantiated with config and a list of already preloaded textures (referenced by name by game engine)
  - Set up the rectangle vertex buffer
  - Setup the shaders
  - Setup each texture
- A way to do animation frames


Game Engine
- Game loop with fold interface?
- Preloader for textures
- Scene graph -> flat list of renderable items
- Model management
- Tie into renderer
- Expose tint, alpha, flip etc.
- Spritesheet management
  - Define animation frames
  - Define animation cycles (frame ranges)
- Tilemap loading?
- Layer management?
- Parallax?
- Backgrounds?

Game
- Initialise game engine with list of paths to textures
- Make game.
