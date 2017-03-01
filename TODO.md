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

Immediate
- Clear stages for asset load, validation, then start with the ability to fail with a nice error.
- Unify Asset manager with one ADT

Redesign
- Move to Typeclass view construction
- View function becomes some sort up view update maybe?
- A way to bind models that can be turned into renderables to view objects so that their state can be persisted
- Persist view state

Bugs:

Renderer
- Alternative to lighting: Blend modes.
 I was thinking about different ways of doing lights and the obvious thing is to use real
 lights but we don't need most of the things that come with them like shadows.
 Better would be deferred lighting when we render the scene to a texture, render the
 lighting to a texture, and then blend them together.
 Importantly, I was thinking about doing lights with textures (like the flashlight in Doom 3)
 for easy lighting effects.
 Then I thought: Would it be easier (given that I have layers and stuff) to just do blend modes
 in the first place? People could do lighting... without doing lighting.
- Simple point lighting
- Simple ambient lighting

Game Engine
- Frame numbers?
- Animation controls like play, stop etc.
- Remove all defaulted arguments.
- Consider the process, we don't want to throw exceptions or deal with optional values we actually insist on having.
  Validate. Fail early, fail fast.
- Nice error message when failed to load asset
- I think Scalajs is downloading script files on load, can they be local?
- Revisit API
  - SceneGraph should be like a DOM, sprites, buttons, animated sprites, static images, parallax images etc.
  - Typeclass: ToSceneGraph?
- Sound
- Full window size
- Full screen

Game
- Make game.


Much later...
- Tilemap loading?
- Performance enhancement: Render at actual size to a buffer and scale up.
- Performance enhancement: We do some CPU side sorting, which generally will be ok, but if there are thousands of tiles and most of them never change, it would be nice to declare that somehow and only have to sort them once.
- Performance enhancement: Static objects. If you have a large group of rectangles that make up one big object - like level platforms - and they never move in relation to each other, we should be able to flatten them into one special object that can be drawn with a single call.
- Screen Effects: Would be awesome to have a bloom fliter for highlights?
