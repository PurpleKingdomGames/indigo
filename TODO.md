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
- Lights don't work on a black background... should they?
- Should rendering white space cost a draw call?

Indigo SBT Plugin
- Build one. The aim should be to speed up development and make testing simpler.

Renderer
- Layer effects (Hue, saturation, tint, Blur, Bloom.. anything else? )
- Pixel effects e.g. animate flood fill disolve

Game Engine
- Consider merging public and internal scenegraph classes and presenting a good old fashioned interface instead.
- Simpler ViewEvents. Need a way to add onClick to `this`.
- Config setting for hide mouse cursor
- Sound
- Full window size
- Full screen
- Tilemap loading
- Find a way to reduce requestAnimationFrame calls

Game
- Make game.

Optimisations:
- I think Scalajs is downloading script files on load, can they be local?
- Performance enhancement: Render at actual size to a buffer and scale up.
- Performance enhancement: We do some CPU side sorting, which generally will be ok, but if there are thousands of tiles
  and most of them never change, it would be nice to declare that somehow and only have to sort them once.
- Performance enhancement: Static objects. If you have a large group of rectangles that make up one big object - like
  level platforms - and they never move in relation to each other, we should be able to flatten them into one special
  object that can be drawn with a single call.
