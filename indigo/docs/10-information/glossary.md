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

## D

### Depth

### Dice

A pseudo-random number generator seeded on the current frames running time to facilitate testing and issue reproduction.

## E

### Entity

### EntityShader

### Entry point

The main interface an Indigo game developer is exposed to when trying to write a game. It is a user friendly game template that constructs the Frame Processor that Indigo needs to run your game, and starts the game engine.

### Event

A description of an action that is emitted during a frame and made available to the subsequent frame, such as a keyboard event.

## F

### FontInfo

### Fill

### FrameContext

### Frame Independence

Movement that is adjusted to account for the time elapsed between frames in order to proceed at a consistent rate.

## G

### GameTime

A sample of the current time provided to every frame. All computations are assumed to happen instantly at the time given.

### Generators

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

### Material

### Matrix3/4

### Mutants

## N

### Noise

## O

### Outcome

Used to gather state and events that were the result of a frame update.

## P

### Pixel-art

### Pre-multiplied Alpha

## Q

### QuadTree

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

### Spritesheet

### Stroke

### SubSystem

A small, well encapsulated game that can be combined with the main game. Used to organise sections of you game and to manages tasks and processes in the background.

## T

### Text

### TextBox

### TileMap

### Time Varying Value

A value that is updated automatically based on some behavior and the time delta between frames.

### Track

### Transform

## U

### Ultraviolet

## V

### Vector2/3/4

### Vertex

### Viewport

## W

### WebGL

### World

## X

## Y

## Z
