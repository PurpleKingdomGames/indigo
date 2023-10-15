# Glossary

## A

### Asset(s)

Data to be loaded into you game, such as images, test, sounds, and animations.

### Automata

A type of subsystem used to manage simple autonomous particles.

## B

### Batch

The `Batch` type is an efficent `List`-like data structure used on most Indigo APIs.

### Bitmap (Material)

A `Bitmap` material simply draws textures as-is, according to the needs of the primitive using the material.

### Blending

Blending is the process of compositing one layer onto another, and is a combination of the blend mode, and a blend shader that tells the graphics card how to combine the pixels.

### BlendMode

The blend mode is the mode that the graphics card should use when compositing on pixel/fragment on top of another.

### BlendShader

A shader used for blending one layer onto another.

### BootData

The initial data provided to the game, used to 'boot strap' the application.

### Bounds

Typically refers to the area that the primitive occupies on the screen.

### BoundaryLocator

The `BoundaryLocator` is a tool used to accurately compute the on-screen size of an element. The calculation can be expensive depending on the primitive type. 

## C

### Clip

A simple animation primitve that loops and animation over time without holding state.

### Clone

A cheap copy of a primitive that can be quickly and efficiently drawn to the screen.

### CloneBlank

A clone blank is a normal primitive, like a graphic, to be used as the reference data for many clones / copies.

### CloneBatch

The concrete method used to draw many copies of a clone blank to the screen. Limitiations are that you cannot change the details, like materials, only the transformation.

### CloneTile

A specialised version of cloning that allows you to draw many instances of tiles from a tilemap.

### Collider

A rigid body used in a physics simulation.

## D

### Depth

Depth from the camera / player, where 0 is closest and increasing positive values are further away.

### Dice

A pseudo-random number generator seeded on the current frames running time to facilitate testing and issue reproduction.

## E

### Entity

The base type of anything that can be drawn onto the screen, essentially represents an area of the screen.

### EntityShader

A shader used for rendering entities onto a layer.

### Entry point

The main interface an Indigo game developer is exposed to when trying to write a game. It is a user friendly game template that constructs the Frame Processor that Indigo needs to run your game, and starts the game engine.

### Event

A description of an action that is emitted during a frame and made available to the subsequent frame, such as a keyboard event.

## F

### Fill

Describes how a shape, say, should be filled. With a solid colour or a gradient, for example.

### FontInfo

A data type that describes how font information is layed out on a sprite sheet for use with `Text` entities.

### Fragment Shader

A shader that works out what colour value each pixel of a rendered entity should be.

### FrameContext

Contains general information about the current frame, such as the running time and a time delta since the previous frame.

### Frame Independence

Movement that is adjusted to account for the time elapsed between frames in order to proceed at a consistent rate.

## G

### GameTime

A sample of the current time provided to every frame. All computations are assumed to happen instantly at the time given.

### Generators

Part of Indigo plugins, generators are tools for generating code at build time that represents your game config, assets, or other data sets.

### GLSL

### Graphic

## H

### HitArea

## I

### ImageEffects (Material)

### InputState

The current state of input devices like the keyboard, mouse or game pad.

## J

## K

## L

### Layer

### Line

### LineSegment

## M

### Magnification

### Material

### Matrix3/4

### Mutants

A mutant is a twist on a `Clone`. Clones are carbon copies of their original that have be repositioned. Mutant's on the other had do not change any spatial information, instead they modify only the shader - which in fact means you can change/mutate them in strange new ways from the original (including spatially, by way of the vertex shader).

## N

### Noise

A mathematical function that appears to create randomness, but is usually psuedo random, and so predicatable / repeatable.

## O

### Outcome

Used to gather state and events that were the result of a frame update.

## P

### Pixel-art

The style of art Indigo was made for. Pixel art was, in the late 80's - early 90's, the only art style available, and went out of fashion in favour of vector and 3D graphics, as the industry persued ever higher graphical fidelity.

Pixel art can back into fashion in the 00's as a retro kitsch nostaligic throwback, and was largely dismissed again shortly there after.

Modern pixel art is a style in it's own right. It takes advantage of up to date computer graphics rendering hardware and techniques, such as dynamic lighting and a wide colour pallette, while it's blocky nature lends itself to a sort of impressionism, leaving the viewer to fill in the gaps.

### Pre-multiplied Alpha

## Q

### QuadTree

A spatial data structure that can be used to find things by location.

## R

### Referential Transparency

The ability to reliably replace a function call and arguments, with the result type and observe no change.

## S

### Scene

A part of your game that represents a particular stage like a screen or a level.

### SceneUpdateFragment

A piece of the final scene to be presented that can be combined with other pieces to form the final scene.

### Shader

A pair of small programs written in GLSL (a C-like language) comprised of a vertex program that tells a graphics card where to place an entity on the screen, and a fragment program that specifies what color to draw each pixel.

### Signal

A function that produces a value for given time. Used in procedural animation.

### Signal Function

A combinator used to manipulate, process, and compose Signals.

### Sprite

A primitive type for rendering animations, such as characters.

### Spritesheet

An image / texture that contains all the frames of one or many animations.

### Stroke

The line around a shape, has variable thickness.

### SubSystem

A small, well encapsulated game that can be combined with the main game. Used to organise sections of you game and to manages tasks and processes in the background.

## T

### Text

A primitive type that renders text as a series of individual entities. In contrast to `TextBox`, text renders well at any scale, but is relatively expensive.

### TextBox

A text rendering approach that draws letters to the browsers canvas. This has the advantage of supporting any web font, but the draw backs of being slow and forcibly anti-aliased (smoothed). This means the font looks good at a magnificaiton of 1, but poor at higher game magnifications.

### Time Varying Value

A value that is updated automatically based on some behavior and the time delta between frames.

### Track

An audio track, with Volume.

### Transform

A transform is the combination of translation (movement), rotation, and scaling, that can be applied to an entity.

## U

### Ultraviolet

A Scala 3 to GLSL transpiler

## V

### Vector2/3/4

Data type that represents position and magnitude as a 2D coordinate, the magnitude being the distance to the origin (0, 0).

### Vertex

An (x, y) coordinate that represents a position on a graph. Vertices make up the points at the corners of triangles, for example.

### Vertex Shader

A shader program that arranges the location an entity will be drawn at on the screen.

### Viewport

The viewable screen area.

## W

### WebGL 1.0/2.0

The web based 3d rendering technology that Indigo is built upon.

### World

Contains all the colliders and other elements of a physics simulation.

## X

## Y

## Z
