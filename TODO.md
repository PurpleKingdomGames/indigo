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

## Current TODOs
- IO Monad
  - The idea is to describe the runtime (initially JS, later native and JVM?) and control execution
  - Initially pretty dumb:
    - Lazy evaluation
    - Catch exceptions and log, bail out gracefully.
    - Record stage metrics?
- ViewModel
  - To avoid polluting the model with view details
  - Add between update model and update view, it's another frame by frame piece of state just like the real model.
  - Make Automata (and other pre-made features) make use of it.

## TODO
- indigoBuild to depend on fastOptJS by default
- See TODO's in Minesweeper game

Indigo SBT Plugin
- Ability to config some aspects of the project
  - Mouse hide
  - Windowing options - size - border - bg colour
  - Game title
  - Metadata?
- Generate html for game
- Open a browser window and run
  - With console open?
  - Is there a way to capture logs and print to terminal? Maybe via Cordova?
- Front onto Cordova for cross platform publish to:
  - Browser
  - Mac
  - PC
  - ???
- Include whole lib so that you only need one sbt import?

Bugs:
- Lights don't work on a black background... should they?
  - Should we always fill with a real colour?
- Should rendering text/font white space cost a draw call?

TODO
- Static objects (see below)
- Asset tags so that related image assets are grouped on the same atlas where possible

Renderer
- Layer effects (Hue, saturation, tint, Blur, Bloom.. anything else?)
- object effects (Blur, outline, drop shadow, innershadow, glow.. anything else?)
- Real lights
- Normal map support
  - Possibly with bump to normal conversion tool for ease of use?

Game Engine
- Full window size - when the plugin is generating the html.
- Full screen - when the plugin is generating the html. 
- Particle systems, which are advanced automata

Maybe add the idea of a 'registers' hook during game setup, allows initialisation of registers for:
1. Fonts // Done
2. Static precomputed assets
3. Automata // Not needed?

Game
- Make game.

Optimisations:
- Offload more onto the GPU - matrix calculations for example.
- I think Scalajs is downloading script files on load, can they be local?
- Performance enhancement: We do some CPU side sorting, which generally will be ok, but if there are thousands of tiles
  and most of them never change, it would be nice to declare that somehow and only have to sort them once. (statics)
- Performance enhancement: Static objects. Everything is drawn in one call but large collections of unchanging scene
  items could be marked as safe to cache to avoid recalculating all the things. Thinking backgrounds.
- Flat objects: For large numbers of tiles all at the same level, they could be marked as "flat" and pre-renderer to a
  framebuffer to be drawn at a fixed depth (subject to the usual sorting)
- Rendering / updating layers at different rates. If the game layer only runs at 10FPS on purpose because of the
  animation style then we can render at that rate rather than 30FPS, you literally just have to skip frames since it will
  remain in the framebuffer.

Need for Speed
**************

Static collections of objects (Won't help perf test results, wrong kind of geometry)
- A static collection might need to be declared up front
- The collection is preprocessed heavily to result in maximum performance.
- Essentially this is either:
  a) A group of polys all at the same depth flattened into one rectangle with a preprocessed texture OR
  b) A collection of triangles all using the same texture atlas, uploaded to their own buffer for quick rendering.
     This option has the advantage of not needed all items to be on the same depth.
- These would be referenced in the view using a new type.

Prefabs (Won't help perf tests and we can almost do this already - and do using Aseprite objects)
- May not be needed if the work above is good enough... also needs more thought. Don't want to programmer to be worrying
  about things like object pools.
- Indigo will allow you to declare prefabs.
- These are identical to normal objects but in the view you just point at the prefab.
- Prefabs are prototypes, each prefab is copied in and is allowed minor adjustments before rendering.
